package com.hyphenate.easeui.widget.chatrow;


import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;

/**
 * 撤回类型的消息的 ItemView
 */
public class EaseChatRowRecall extends EaseChatRow {

    private TextView contentView;

    public EaseChatRowRecall(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(R.layout.ease_row_recall_message, this);

    }

    @Override
    protected void onFindViewById() {
        contentView = (TextView) findViewById(R.id.text_content);
    }

    @Override
    protected void onUpdateView() {

    }

    @Override
    protected void onSetUpView() {

        // 设置显示内容
        String messageStr = null;
        if (message.direct() == EMMessage.Direct.SEND) {
            messageStr = String.format(context.getString(R.string.msg_recall_by_self));
            Log.e("textContent", messageStr);
        } else {
            messageStr = String.format(context.getString(R.string.msg_recall_by_user), message.getUserName());
        }
        Log.e("textContent", messageStr + message.toString());
        contentView.setText(messageStr);
    }

    @Override
    protected void onBubbleClick() {

    }
}

