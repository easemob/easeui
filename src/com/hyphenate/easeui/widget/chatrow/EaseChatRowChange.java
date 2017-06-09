package com.hyphenate.easeui.widget.chatrow;


import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.R;

/**
 * 撤回类型的消息的 ItemView
 */
public class EaseChatRowChange extends EaseChatRow {

    private TextView contentView;

    public EaseChatRowChange(Context context, EMMessage message, int position, BaseAdapter adapter) {
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
        EMTextMessageBody body = (EMTextMessageBody) message.getBody();
        // 设置显示内容
        contentView.setText(body.getMessage());
    }

    @Override
    protected void onBubbleClick() {

    }
}

