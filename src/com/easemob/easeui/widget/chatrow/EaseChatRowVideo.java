package com.easemob.easeui.widget.chatrow;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.VideoMessageBody;
import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseImageCache;
import com.easemob.easeui.ui.EaseShowVideoActivity;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.util.DateUtils;
import com.easemob.util.EMLog;
import com.easemob.util.ImageUtils;
import com.easemob.util.TextFormater;

public class EaseChatRowVideo extends EaseChatRowFile{

	private ImageView imageView;
    private TextView sizeView;
    private TextView timeLengthView;
    private ImageView playView;

    public EaseChatRowVideo(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflatView() {
		inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
				R.layout.ease_row_received_video : R.layout.ease_row_sent_video, this);
	}

	@Override
	protected void onFindViewById() {
	    imageView = ((ImageView) findViewById(R.id.chatting_content_iv));
        sizeView = (TextView) findViewById(R.id.chatting_size_iv);
        timeLengthView = (TextView) findViewById(R.id.chatting_length_iv);
        playView = (ImageView) findViewById(R.id.chatting_status_btn);
        percentageView = (TextView) findViewById(R.id.percentage);
	}

	@Override
	protected void onSetUpView() {
	    VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
        // final File image=new File(PathUtil.getInstance().getVideoPath(),
        // videoBody.getFileName());
        String localThumb = videoBody.getLocalThumb();

        if (localThumb != null) {

            showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
        }
        if (videoBody.getLength() > 0) {
            String time = DateUtils.toTimeBySecond(videoBody.getLength());
            timeLengthView.setText(time);
        }
//        playView.setImageResource(R.drawable.video_play_btn_small_nor);

        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (videoBody.getVideoFileLength() > 0) {
                String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
                sizeView.setText(size);
            }
        } else {
            if (videoBody.getLocalUrl() != null && new File(videoBody.getLocalUrl()).exists()) {
                String size = TextFormater.getDataSize(new File(videoBody.getLocalUrl()).length());
                sizeView.setText(size);
            }
        }

        if (message.direct == EMMessage.Direct.RECEIVE) {

            if (message.status == EMMessage.Status.INPROGRESS) {
                imageView.setImageResource(R.drawable.ease_default_image);
                setMessageReceiveCallback();

            } else {
                // System.err.println("!!!! not back receive, show image directly");
                imageView.setImageResource(R.drawable.ease_default_image);
                if (localThumb != null) {
                    showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
                }

            }

            return;
        }
        //处理发送方消息
        handleSendMessage();
	}
	
	@Override
	protected void onBubbleClick() {
	    VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
        EMLog.d(TAG, "video view is on click");
        Intent intent = new Intent(context, EaseShowVideoActivity.class);
        intent.putExtra("localpath", videoBody.getLocalUrl());
        intent.putExtra("secret", videoBody.getSecret());
        intent.putExtra("remotepath", videoBody.getRemoteUrl());
        if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
                && message.getChatType() != ChatType.GroupChat) {
            message.isAcked = true;
            try {
                EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        activity.startActivity(intent);
	}
	
	/**
     * 展示视频缩略图
     * 
     * @param localThumb
     *            本地缩略图路径
     * @param iv
     * @param thumbnailUrl
     *            远程缩略图路径
     * @param message
     */
    private void showVideoThumbView(final String localThumb, final ImageView iv, String thumbnailUrl, final EMMessage message) {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = EaseImageCache.getInstance().get(localThumb);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);

        } else {
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... params) {
                    if (new File(localThumb).exists()) {
                        return ImageUtils.decodeScaleImage(localThumb, 160, 160);
                    } else {
                        return null;
                    }
                }
                
                @Override
                protected void onPostExecute(Bitmap result) {
                    super.onPostExecute(result);
                    if (result != null) {
                        EaseImageCache.getInstance().put(localThumb, result);
                        iv.setImageBitmap(result);

                    } else {
                        if (message.status == EMMessage.Status.FAIL) {
                            if (EaseCommonUtils.isNetWorkConnected(activity)) {
                                EMChatManager.getInstance().asyncFetchMessage(message);
                            }
                        }

                    }
                }
            }.execute();
        }
        
    }
    
    

}
