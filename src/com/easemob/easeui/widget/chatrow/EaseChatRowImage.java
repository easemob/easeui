package com.easemob.easeui.widget.chatrow;

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.ImageMessageBody;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseMessageAdapter;
import com.easemob.easeui.model.EaseImageCache;
import com.easemob.easeui.ui.EaseShowBigImageActivity;
import com.easemob.easeui.utils.EaseACKUtil;
import com.easemob.easeui.utils.EaseBlurUtils;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.easeui.utils.EaseImageUtils;
import com.easemob.exceptions.EaseMobException;

public class EaseChatRowImage extends EaseChatRowFile {

    protected ImageView imageView;
    private ImageMessageBody imgBody;

    public EaseChatRowImage(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ? R.layout.ease_row_received_picture
                : R.layout.ease_row_sent_picture, this);
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }

    @Override
    protected void onSetUpView() {
        imgBody = (ImageMessageBody) message.getBody();
        // 接收方向的消息
        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.status == EMMessage.Status.INPROGRESS) {
                imageView.setImageResource(R.drawable.ease_default_image);
                setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.GONE);
                percentageView.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.ease_default_image);
                if (imgBody.getLocalUrl() != null) {
                    String remotePath = imgBody.getRemoteUrl();
                    String filePath = EaseImageUtils.getImagePath(remotePath);
                    String thumbRemoteUrl = imgBody.getThumbnailUrl();
                    String thumbnailPath = EaseImageUtils.getThumbnailImagePath(thumbRemoteUrl);
                    showImageView(thumbnailPath, imageView, filePath, message);
                }
            }
            return;
        }

        String filePath = imgBody.getLocalUrl();
        if (filePath != null) {
            showImageView(EaseImageUtils.getThumbnailImagePath(filePath), imageView, filePath, message);
        }
        handleSendMessage();
    }

    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }

    @Override
    protected void onBubbleClick() {
        Intent intent = new Intent(context, EaseShowBigImageActivity.class);
        File file = new File(imgBody.getLocalUrl());
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            intent.putExtra("uri", uri);
            intent.putExtra(EaseConstant.EASE_ATTR_REVOKE_MSG_ID, message.getMsgId());
        } else {
            // The local full size pic does not exist yet.
            // ShowBigImage needs to download it from the server
            // first
            intent.putExtra("secret", imgBody.getSecret());
            intent.putExtra("remotepath", imgBody.getRemoteUrl());
            // 这里把当前消息的id传递过去，是为了实现查看大图之后销毁这条消息
            intent.putExtra(EaseConstant.EASE_ATTR_REVOKE_MSG_ID, message.getMsgId());
        }
        if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
        		&& message.getChatType() == ChatType.Chat) {
            sendACKMessage();
        }
        context.startActivity(intent);
    }

    /**
     * ACK 消息的发送，根据是否发送成功做些相应的操作，这里是把发送失败的消息id和username保存在序列化类中
     */
    private void sendACKMessage() {
        try {
            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
            message.isAcked = true;
        } catch (EaseMobException e) {
            e.printStackTrace();
            EaseACKUtil.getInstance(context).saveACKDataId(message.getMsgId(), message.getFrom());
        } finally {
        	if(message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
                    && message.direct == Direct.RECEIVE){
        		EMChatManager.getInstance().getConversation(message.getFrom()).removeMessage(message.getMsgId());
        		onUpdateView();
        	}
        }
    }
    /**
     * load image into image view
     * 
     * @param thumbernailPath
     * @param iv
     * @param position
     * @return the image exists or not
     */
    private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath,
            final EMMessage message) {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = EaseImageCache.getInstance().get(thumbernailPath);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            // 加上当前图片是否是阅后即焚类型的判断，如果是 则模糊图片在设置给imageView控件
            if (message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
                    && message.direct == Direct.RECEIVE) {
                imageView.setImageBitmap(EaseBlurUtils.blurBitmap(bitmap));
            } else {
                iv.setImageBitmap(bitmap);
            }
            return true;
        } else {
            new AsyncTask<Object, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Object... args) {
                    File file = new File(thumbernailPath);
                    if (file.exists()) {
                        return EaseImageUtils.decodeScaleImage(thumbernailPath, 160, 160);
                    } else {
                        if (message.direct == EMMessage.Direct.SEND) {
                            return EaseImageUtils.decodeScaleImage(localFullSizePath, 160, 160);
                        } else {
                            return null;
                        }
                    }
                }

                protected void onPostExecute(Bitmap image) {
                    if (image != null) {
                        // 加上当前图片是否是阅后即焚类型的判断，如果是 则模糊图片在设置给imageView控件
                        if (message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
                                && message.direct == Direct.RECEIVE) {
                            imageView.setImageBitmap(EaseBlurUtils.blurBitmap(image));
                        } else {
                            iv.setImageBitmap(image);
                        }
                        EaseImageCache.getInstance().put(thumbernailPath, image);
                    } else {
                        if (message.status == EMMessage.Status.FAIL) {
                            if (EaseCommonUtils.isNetWorkConnected(activity)) {
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        EMChatManager.getInstance().asyncFetchMessage(message);
                                    }
                                }).start();
                            }
                        }

                    }
                }
            }.execute();
            return true;
        }
    }
}
