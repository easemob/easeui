package com.easemob.easeui.widget.chatrow;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.NormalFileMessageBody;
import com.easemob.easeui.R;
import com.easemob.easeui.ui.EaseShowNormalFileActivity;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.FileUtils;
import com.easemob.util.TextFormater;

public class EaseChatRowFile extends EaseChatRow{

    protected TextView fileNameView;
	protected TextView fileSizeView;
    protected TextView fileStateView;
    
    protected EMCallBack sendfileCallBack;
    
    protected boolean isNotifyProcessed;
    private NormalFileMessageBody fileMessageBody;

    public EaseChatRowFile(Context context, EMMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflatView() {
	    inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ? 
	            R.layout.ease_row_received_file : R.layout.ease_row_sent_file, this);
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
	    fileMessageBody = (NormalFileMessageBody) message.getBody();
        String filePath = fileMessageBody.getLocalUrl();
        fileNameView.setText(fileMessageBody.getFileName());
        fileSizeView.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
        if (message.direct == EMMessage.Direct.RECEIVE) { // 接收的消息
            File file = new File(filePath);
            if (file != null && file.exists()) {
                fileStateView.setText(R.string.Have_downloaded);
            } else {
                fileStateView.setText(R.string.Did_not_download);
            }
            return;
        }

        // until here, deal with send voice msg
        handleSendMessage();
	}

	/**
	 * 处理发送消息
	 */
    protected void handleSendMessage() {
        setMessageSendCallback();
        switch (message.status) {
        case SUCCESS:
            progressBar.setVisibility(View.INVISIBLE);
            if(percentageView != null)
                percentageView.setVisibility(View.INVISIBLE);
            statusView.setVisibility(View.INVISIBLE);
            break;
        case FAIL:
            progressBar.setVisibility(View.INVISIBLE);
            if(percentageView != null)
                percentageView.setVisibility(View.INVISIBLE);
            statusView.setVisibility(View.VISIBLE);
            break;
        case INPROGRESS:
            progressBar.setVisibility(View.VISIBLE);
            if(percentageView != null){
                percentageView.setVisibility(View.VISIBLE);
                percentageView.setText(message.progress + "%");
            }
            statusView.setVisibility(View.INVISIBLE);
            break;
        default:
            progressBar.setVisibility(View.VISIBLE);
            if(percentageView != null){
                percentageView.setVisibility(View.VISIBLE);
                percentageView.setText(message.progress + "%");
            }
            statusView.setVisibility(View.INVISIBLE);
            break;
        }
    }
	

	@Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        String filePath = fileMessageBody.getLocalUrl();
        File file = new File(filePath);
        if (file != null && file.exists()) {
            // 文件存在，直接打开
            FileUtils.openFile(file, (Activity) context);
        } else {
            // 下载
            context.startActivity(new Intent(context, EaseShowNormalFileActivity.class).putExtra("msgbody", message.getBody()));
        }
        if (message.direct == EMMessage.Direct.RECEIVE && !message.isAcked) {
            try {
                EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                message.isAcked = true;
            } catch (EaseMobException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
}
