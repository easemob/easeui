package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseEditTextUtils;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.TextFormater;

import java.io.File;

/**
 * file for row
 */
public class EaseChatRowFile extends EaseChatRow {
    private static final String TAG = EaseChatRowFile.class.getSimpleName();
    /**
     * file name
     */
    protected TextView fileNameView;
    /**
     * file's size
     */
	protected TextView fileSizeView;
    /**
     * file state
     */
    protected TextView fileStateView;
    private EMNormalFileMessageBody fileMessageBody;

    public EaseChatRowFile(Context context, boolean isSender) {
        super(context, isSender);
    }

    public EaseChatRowFile(Context context, EMMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
	protected void onInflateView() {
	    inflater.inflate(!showSenderType ? R.layout.ease_row_received_file
                : R.layout.ease_row_sent_file, this);
	}

	@Override
	protected void onFindViewById() {
	    fileNameView = (TextView) findViewById(R.id.tv_file_name);
        fileSizeView = (TextView) findViewById(R.id.tv_file_size);
        fileStateView = (TextView) findViewById(R.id.tv_file_state);
        percentageView = (TextView) findViewById(R.id.percentage);
	}

	@Override
	protected void onSetUpView() {
	    fileMessageBody = (EMNormalFileMessageBody) message.getBody();
        Uri filePath = fileMessageBody.getLocalUri();
        fileNameView.setText(fileMessageBody.getFileName());
        fileNameView.post(()-> {
            String content = EaseEditTextUtils.ellipsizeMiddleString(fileNameView,
                        fileMessageBody.getFileName(),
                        fileNameView.getMaxLines(),
                        fileNameView.getWidth() - fileNameView.getPaddingLeft() - fileNameView.getPaddingRight());
            fileNameView.setText(content);
        });
        fileSizeView.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
        if (message.direct() == EMMessage.Direct.SEND){
            if (EaseFileUtils.isFileExistByUri(context, filePath)
                    && message.status() == EMMessage.Status.SUCCESS) {
                fileStateView.setText(R.string.have_uploaded);
            }else {
                fileStateView.setText("");
            }
        }
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (EaseFileUtils.isFileExistByUri(context, filePath)) {
                fileStateView.setText(R.string.have_downloaded);
            } else {
                fileStateView.setText(R.string.did_not_download);
            }
        }
	}

    @Override
    protected void onMessageCreate() {
        super.onMessageCreate();
        progressBar.setVisibility(View.VISIBLE);
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onMessageSuccess() {
        super.onMessageSuccess();
        progressBar.setVisibility(View.INVISIBLE);
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
        if (message.direct() == EMMessage.Direct.SEND)
            if(fileStateView != null) {
                fileStateView.setText(R.string.have_uploaded);
            }
    }

    @Override
    protected void onMessageError() {
        super.onMessageError();
        progressBar.setVisibility(View.INVISIBLE);
        if (percentageView != null)
            percentageView.setVisibility(View.INVISIBLE);
        if (statusView != null)
            statusView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onMessageInProgress() {
        super.onMessageInProgress();
        if(progressBar.getVisibility() != VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (percentageView != null) {
            percentageView.setVisibility(View.VISIBLE);
            percentageView.setText(message.progress() + "%");
        }
        if (statusView != null)
            statusView.setVisibility(View.INVISIBLE);
    }
}
