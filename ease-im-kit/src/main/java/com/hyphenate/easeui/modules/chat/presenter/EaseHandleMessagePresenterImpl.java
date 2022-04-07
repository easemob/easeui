package com.hyphenate.easeui.modules.chat.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMTranslationResult;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.manager.EaseAtMessageHelper;
import com.hyphenate.easeui.modules.chat.EaseChatLayout;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import com.hyphenate.util.PathUtil;

import java.io.File;
import java.io.FileOutputStream;

public class EaseHandleMessagePresenterImpl extends EaseHandleMessagePresenter {
    private static final String TAG = EaseChatLayout.class.getSimpleName();

    @Override
    public void sendTextMessage(String content) {
        sendTextMessage(content, false);
    }

    @Override
    public void sendTextMessage(String content, boolean isNeedGroupAck) {
        if(EaseAtMessageHelper.get().containsAtUsername(content)) {
            sendAtMessage(content);
            return;
        }
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        message.setIsNeedGroupAck(isNeedGroupAck);
        sendMessage(message);
    }

    @Override
    public void sendAtMessage(String content) {
        if(!isGroupChat()){
            EMLog.e(TAG, "only support group chat message");
            if(isActive()) {
                runOnUI(()-> mView.sendMessageFail("only support group chat message"));
            }
            return;
        }
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
        if(EMClient.getInstance().getCurrentUser().equals(group.getOwner()) && EaseAtMessageHelper.get().containsAtAll(content)){
            message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL);
        }else {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG,
                    EaseAtMessageHelper.get().atListToJsonArray(EaseAtMessageHelper.get().getAtMessageUsernames(content)));
        }
        sendMessage(message);
    }

    @Override
    public void sendBigExpressionMessage(String name, String identityCode) {
        EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityCode);
        sendMessage(message);
    }

    @Override
    public void sendVoiceMessage(Uri filePath, int length) {
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
        sendMessage(message);
    }

    @Override
    public void sendImageMessage(Uri imageUri) {
        sendImageMessage(imageUri, false);
    }

    @Override
    public void sendImageMessage(Uri imageUri, boolean sendOriginalImage) {
        //Compatible with web and does not support heif image terminal
        //convert heif format to jpeg general image format
        imageUri = handleImageHeifToJpeg(imageUri);
        EMMessage message = EMMessage.createImageSendMessage(imageUri, sendOriginalImage, toChatUsername);
        sendMessage(message);
    }

    @Override
    public void sendLocationMessage(double latitude, double longitude, String locationAddress, String buildingName) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, buildingName, toChatUsername);
        EMLog.i(TAG, "current = "+EMClient.getInstance().getCurrentUser() + " to = "+toChatUsername);
        EMMessageBody body = message.getBody();
        String msgId = message.getMsgId();
        String from = message.getFrom();
        EMLog.i(TAG, "body = "+body);
        EMLog.i(TAG, "msgId = "+msgId + " from = "+from);
        sendMessage(message);
    }

    @Override
    public void sendVideoMessage(Uri videoUri, int videoLength) {
        String thumbPath = getThumbPath(videoUri);
        EMMessage message = EMMessage.createVideoSendMessage(videoUri, thumbPath, videoLength, toChatUsername);
        sendMessage(message);
    }

    @Override
    public void sendFileMessage(Uri fileUri) {
        EMMessage message = EMMessage.createFileSendMessage(fileUri, toChatUsername);
        sendMessage(message);
    }

    @Override
    public void addMessageAttributes(EMMessage message) {
        //可以添加一些自定义属性
        mView.addMsgAttrBeforeSend(message);
    }

    @Override
    public void sendMessage(EMMessage message) {
        if(message == null) {
            if(isActive()) {
                runOnUI(() -> mView.sendMessageFail("message is null!"));
            }
            return;
        }
        addMessageAttributes(message);
        if (chatType == EaseConstant.CHATTYPE_GROUP){
            message.setChatType(EMMessage.ChatType.GroupChat);
        }else if(chatType == EaseConstant.CHATTYPE_CHATROOM){
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                if(isActive()) {
                    runOnUI(()-> mView.onPresenterMessageSuccess(message));
                }
            }

            @Override
            public void onError(int code, String error) {
                if(isActive()) {
                    runOnUI(()-> mView.onPresenterMessageError(message, code, error));
                }
            }

            @Override
            public void onProgress(int progress, String status) {
                if(isActive()) {
                    runOnUI(()-> mView.onPresenterMessageInProgress(message, progress));
                }
            }
        });
        // send message
        EMClient.getInstance().chatManager().sendMessage(message);
        if(isActive()) {
            runOnUI(()-> mView.sendMessageFinish(message));
        }
    }

    @Override
    public void sendCmdMessage(String action) {
        EMMessage beginMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody body = new EMCmdMessageBody(action);
        // Only deliver this cmd msg to online users
        body.deliverOnlineOnly(true);
        beginMsg.addBody(body);
        beginMsg.setTo(toChatUsername);
        EMClient.getInstance().chatManager().sendMessage(beginMsg);
    }

    @Override
    public void resendMessage(EMMessage message) {
        message.setStatus(EMMessage.Status.CREATE);
        long currentTimeMillis = System.currentTimeMillis();
        message.setLocalTime(currentTimeMillis);
        message.setMsgTime(currentTimeMillis);
        EMClient.getInstance().chatManager().updateMessage(message);
        sendMessage(message);
    }

    @Override
    public void deleteMessage(EMMessage message) {
        conversation.removeMessage(message.getMsgId());
        if(isActive()) {
            runOnUI(()->mView.deleteLocalMessageSuccess(message));
        }
    }

    @Override
    public void recallMessage(EMMessage message) {
        try {
            EMMessage msgNotification = EMMessage.createSendMessage(EMMessage.Type.TXT);
            EMTextMessageBody txtBody = new EMTextMessageBody(mView.context().getResources().getString(R.string.msg_recall_by_self));
            msgNotification.addBody(txtBody);
            msgNotification.setTo(message.getTo());
            msgNotification.setDirection(message.direct());
            msgNotification.setMsgTime(message.getMsgTime());
            msgNotification.setLocalTime(message.getMsgTime());
            msgNotification.setAttribute(EaseConstant.MESSAGE_TYPE_RECALL, true);
            msgNotification.setAttribute(EaseConstant.MESSAGE_TYPE_RECALLER, EMClient.getInstance().getCurrentUser());
            msgNotification.setStatus(EMMessage.Status.SUCCESS);
            EMClient.getInstance().chatManager().recallMessage(message);
            EMClient.getInstance().chatManager().saveMessage(msgNotification);
            if(isActive()) {
                runOnUI(()->mView.recallMessageFinish(msgNotification));
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
            if(isActive()) {
                runOnUI(()->mView.recallMessageFail(e.getErrorCode(), e.getDescription()));
            }
        }
    }

    @Override
    public void translateMessage(EMMessage message, String languageCode, boolean isTranslation) {
        EMTextMessageBody body = (EMTextMessageBody) message.getBody();
        if(isTranslation) {
            EMTranslationResult result = EMClient.getInstance().translationManager().getTranslationResult(message.getMsgId());
            if (result != null) {
                result.setShowTranslation(true);
                EMClient.getInstance().translationManager().updateTranslationResult(result);
                if(isActive()) {
                    runOnUI(()->mView.translateMessageSuccess(message));
                }
                return;
            }
        }
        EMClient.getInstance().translationManager().translate(message.getMsgId(),
                message.conversationId(),
                body.getMessage(),
                languageCode,
                new EMValueCallBack<EMTranslationResult>() {
                    @Override
                    public void onSuccess(EMTranslationResult value) {
                        if(isActive()) {
                            runOnUI(()->mView.translateMessageSuccess(message));
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        if(isActive()) {
                            runOnUI(()->mView.translateMessageFail(message, error, errorMsg));
                        }
                    }
                });
    }

    @Override
    public void hideTranslate(EMMessage message) {
        EMTranslationResult result = EMClient.getInstance().translationManager().getTranslationResult(message.getMsgId());
        result.setShowTranslation(false);
        EMClient.getInstance().translationManager().updateTranslationResult(result);
    }

    /**
     * 获取视频封面
     * @param videoUri
     * @return
     */
    private String getThumbPath(Uri videoUri) {
        if(!EaseFileUtils.isFileExistByUri(mView.context(), videoUri)) {
            return "";
        }
        String filePath = EaseFileUtils.getFilePath(mView.context(), videoUri);
        File file = new File(PathUtil.getInstance().getVideoPath(), "thvideo" + System.currentTimeMillis()+".jpeg");
        boolean createSuccess = true;
        if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                Bitmap ThumbBitmap = ThumbnailUtils.createVideoThumbnail(filePath, 3);
                ThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                EMLog.e(TAG, e.getMessage());
                if(isActive()) {
                    runOnUI(() -> mView.createThumbFileFail(e.getMessage()));
                }
                createSuccess = false;
            }
        }else {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                MediaMetadataRetriever media = new MediaMetadataRetriever();
                media.setDataSource(mView.context(), videoUri);
                Bitmap frameAtTime = media.getFrameAtTime();
                frameAtTime.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                EMLog.e(TAG, e.getMessage());
                if(isActive()) {
                    runOnUI(() -> mView.createThumbFileFail(e.getMessage()));
                }
                createSuccess = false;
            }
        }
        return createSuccess ? file.getAbsolutePath() : "";
    }

    /**
     * 图片heif转jpeg
     *
     * @param imageUri 图片Uri
     * @return Uri
     */
    private Uri handleImageHeifToJpeg(Uri imageUri) {
        try {
            BitmapFactory.Options options;
            String filePath = EaseFileUtils.getFilePath(mView.context(), imageUri);
            if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                options = ImageUtils.getBitmapOptions(filePath);
            } else {
                options = ImageUtils.getBitmapOptions(mView.context(), imageUri);
            }
            if ("image/heif".equalsIgnoreCase(options.outMimeType)) {
                imageUri = EaseImageUtils.imageToJpeg(mView.context(), imageUri, new File(PathUtil.getInstance().getImagePath(), "image_message_temp.jpeg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUri;
    }
}

