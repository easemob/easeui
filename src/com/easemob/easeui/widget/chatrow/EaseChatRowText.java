package com.easemob.easeui.widget.chatrow;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
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
import com.easemob.easeui.utils.EaseACKUtil;
import com.easemob.easeui.utils.EaseSmileUtils;
import com.easemob.easeui.widget.EaseAlertDialog;
import com.easemob.exceptions.EaseMobException;

public class EaseChatRowText extends EaseChatRow {

    private TextView contentView;

    public EaseChatRowText(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ? R.layout.ease_row_received_message
                : R.layout.ease_row_sent_message, this);
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
        if (message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false) && message.direct == Direct.RECEIVE) {
            contentView.setText(
                    String.format(context.getString(R.string.readfire_message_content), txtBody.getMessage().length()));
        } else {
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
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                // 发送消息
                // sendMsgInBackground(message);
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
        } else {
            if (!message.isAcked() && message.getChatType() == ChatType.Chat
                    && !message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)) {
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
        // 这里必须进行强转一下然后调用adapter的 refresh方法，否则在text类型的消息是阅后即焚时，删除后界面不会刷新
        if (adapter instanceof EaseMessageAdapter) {
            ((EaseMessageAdapter) adapter).refresh();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onBubbleClick() {
        // 只有当消息是阅后即焚类型时，实现消息框的点击事件，弹出查看消息内容的对话框，当关闭对话框时销毁消息，否则跳过
        if (!message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false) || message.direct == Direct.SEND) {
            return;
        }
        EaseAlertDialog dialog = new EaseAlertDialog(context, context.getString(R.string.readfire_message_title),
                ((TextMessageBody) message.getBody()).getMessage(), null, new EaseAlertDialog.AlertDialogUser() {
                    @Override
                    public void onResult(boolean confirmed, Bundle bundle) {
                        sendACKMessage();
                    }
                }, false);
        // 设置触摸对话框外围不触发事件，防止误触碰
        dialog.setCanceledOnTouchOutside(false);
        // 这里是为了在不是点击OK按钮的情况下关闭对话框的操作
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!message.isAcked()) {
                    sendACKMessage();
                }
            }
        });
        dialog.show();
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
            EMChatManager.getInstance().getConversation(message.getFrom()).removeMessage(message.getMsgId());
            onUpdateView();
        }
    }
}
