package com.hyphenate.easeui.widget.chatrow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseDateUtils;
import com.hyphenate.easeui.utils.EaseImageCache;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.util.DateUtils;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import com.hyphenate.util.TextFormater;

import java.io.File;
import java.io.IOException;

public class EaseChatRowVideo extends EaseChatRowFile {
    private static final String TAG = EaseChatRowVideo.class.getSimpleName();

    private ImageView imageView;
    private TextView sizeView;
    private TextView timeLengthView;
    private ImageView playView;

    public EaseChatRowVideo(Context context, boolean isSender) {
        super(context, isSender);
    }

    public EaseChatRowVideo(Context context, EMMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

	@Override
	protected void onInflateView() {
		inflater.inflate(!showSenderType ? R.layout.ease_row_received_video
                : R.layout.ease_row_sent_video, this);
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
        if(bubbleLayout != null) {
            bubbleLayout.setBackground(null);
        }
        EMVideoMessageBody videoBody = (EMVideoMessageBody) message.getBody();

        if (videoBody.getDuration() > 0) {
            String time = EaseDateUtils.toTime(videoBody.getDuration());
            timeLengthView.setText(time);
        }

        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (videoBody.getVideoFileLength() > 0) {
                String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
                sizeView.setText(size);
            }
        } else {
            long videoFileLength = videoBody.getVideoFileLength();
            sizeView.setText(TextFormater.getDataSize(videoFileLength));
        }

        EMLog.d(TAG,  "video thumbnailStatus:" + videoBody.thumbnailDownloadStatus());
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING) {
                imageView.setImageResource(R.drawable.ease_default_image);
            } else {
                // System.err.println("!!!! not back receive, show image directly");
                imageView.setImageResource(R.drawable.ease_default_image);
                showVideoThumbView(message);
            }
        }else{
            if (videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING ||
                    videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.FAILED) {
                if(progressBar != null) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                if(percentageView != null) {
                    percentageView.setVisibility(View.INVISIBLE);
                }
                if(videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
                    showVideoThumbView(message);
                }else {
                    imageView.setImageResource(R.drawable.ease_default_image);
                }
            } else {
                if(progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if(percentageView != null) {
                    percentageView.setVisibility(View.GONE);
                }
                imageView.setImageResource(R.drawable.ease_default_image);
                showVideoThumbView(message);
            }
        }
	}

    /**
     * show video thumbnails
     * @param message
     */
    @SuppressLint("StaticFieldLeak")
    private void showVideoThumbView(final EMMessage message) {
        ViewGroup.LayoutParams params = EaseImageUtils.showVideoThumb(context, imageView, message);
        setBubbleView(params.width, params.height);
    }

    private void setBubbleView(int width, int height) {
        ViewGroup.LayoutParams params = bubbleLayout.getLayoutParams();
        params.width = width;
        params.height = height;
    }

}
