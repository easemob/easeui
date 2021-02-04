package com.hyphenate.easeui.modules.chat.presenter;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.modules.EaseBasePresenter;
import com.hyphenate.easeui.modules.ILoadDataView;
import com.hyphenate.easeui.utils.EaseCommonUtils;

public abstract class EaseHandleMessagePresenter extends EaseBasePresenter {
    protected IHandleMessageView mView;
    protected int chatType;
    protected String toChatUsername;
    protected EMConversation conversation;

    @Override
    public void attachView(ILoadDataView view) {
        mView = (IHandleMessageView) view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachView();
    }

    /**
     * 绑定发送方id
     * @param chatType
     * @param toChatUsername
     */
    public void setupWithToUser(int chatType, @NonNull String toChatUsername) {
        this.chatType = chatType;
        this.toChatUsername = toChatUsername;
        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
    }

    /**
     * 发送文本消息
     * @param content
     */
    public abstract void sendTextMessage(String content);

    /**
     * 发送文本消息
     * @param content
     * @param isNeedGroupAck 需要需要群回执
     */
    public abstract void sendTextMessage(String content, boolean isNeedGroupAck);

    /**
     * 发送@消息
     * @param content
     */
    public abstract void sendAtMessage(String content);

    /**
     * 发送大表情消息
     * @param name
     * @param identityCode
     */
    public abstract void sendBigExpressionMessage(String name, String identityCode);

    /**
     * 发送语音消息
     * @param filePath
     * @param length
     */
    public abstract void sendVoiceMessage(Uri filePath, int length);

    /**
     * 发送图片消息
     * @param imageUri
     */
    public abstract void sendImageMessage(Uri imageUri);

    /**
     * 发送图片消息
     * @param imageUri
     * @param sendOriginalImage
     */
    public abstract void sendImageMessage(Uri imageUri, boolean sendOriginalImage);

    /**
     * 发送定位消息
     * @param latitude
     * @param longitude
     * @param locationAddress
     */
    public abstract void sendLocationMessage(double latitude, double longitude, String locationAddress);

    /**
     * 发送视频消息
     * @param videoUri
     * @param videoLength
     */
    public abstract void sendVideoMessage(Uri videoUri, int videoLength);

    /**
     * 发送文件消息
     * @param fileUri
     */
    public abstract void sendFileMessage(Uri fileUri);

    /**
     * 为消息添加扩展字段
     * @param message
     */
    public abstract void addMessageAttributes(EMMessage message);

    /**
     * 发送消息
     * @param message
     */
    public abstract void sendMessage(EMMessage message);

    /**
     * 发送cmd消息
     * @param action
     */
    public abstract void sendCmdMessage(String action);

    /**
     * 重新发送消息
     * @param message
     */
    public abstract void resendMessage(EMMessage message);

    /**
     * 删除消息
     * @param message
     */
    public abstract void deleteMessage(EMMessage message);

    /**
     * 撤回消息
     * @param message
     */
    public abstract void recallMessage(EMMessage message);

    /**
     * 是否是群聊
     * @return
     */
    public boolean isGroupChat() {
        return chatType == EaseConstant.CHATTYPE_GROUP;
    }
}

