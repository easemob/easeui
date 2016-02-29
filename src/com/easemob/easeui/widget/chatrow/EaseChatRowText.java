package com.easemob.easeui.widget.chatrow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.Spannable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseMessageAdapter;
import com.easemob.easeui.utils.EaseSmileUtils;
import com.easemob.exceptions.EaseMobException;

public class EaseChatRowText extends EaseChatRow{

	private TextView contentView;

    public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_message : R.layout.ease_row_sent_message, this);
    }

    @Override
    protected void onFindViewById() {
        contentView = (TextView) findViewById(R.id.tv_chatcontent);
    }

    @Override
    public void onSetUpView() {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        Spannable span = EaseSmileUtils.getSmiledText(context, txtBody.getMessage());
        // 判断是不是阅后即焚的消息
        if(message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
                &&message.direct == Direct.RECEIVE){
            contentView.setText(String.format(context.getString(R.string.readfire_message_content),txtBody.getMessage().length()));
        }else{
            // 设置内容
            contentView.setText(span, BufferType.SPANNABLE);
        }
        handleTextMessage();
    }

    protected void handleTextMessage() {
        if (message.direct == EMMessage.Direct.SEND) {
            setMessageSendCallback();
            switch (message.status) {
            case CREATE: 
                progressBar.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                // 发送消息
//                sendMsgInBackground(message);
                break;
            case SUCCESS: // 发送成功
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.GONE);
                break;
            case FAIL: // 发送失败
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS: // 发送中
                progressBar.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                break;
            default:
               break;
            }
        }else{
            if(!message.isAcked() 
                    && message.getChatType() == ChatType.Chat
                    && !message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)){
                try {
                    EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                    message.isAcked = true;
                } catch (EaseMobException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        // 只有当消息是阅后即焚类型时，实现消息框的点击事件，弹出查看消息内容的对话框，当关闭对话框时销毁消息，否则跳过
        if(!message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
                || message.direct == Direct.SEND){
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(R.string.readfire_message_title);
        dialog.setMessage(((TextMessageBody) message.getBody()).getMessage());
        dialog.show();
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                EMChatManager.getInstance().getConversation(message.getFrom()).removeMessage(message.getMsgId());;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                            message.isAcked = true;
                            onUpdateView();
                        } catch (EaseMobException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }



}
