package com.hyphenate.easeui.viewholder;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

import java.util.List;


public class EaseChatRowViewHolder extends EaseMessageAdapter.ViewHolder<EMMessage> implements EaseChatRow.EaseChatRowActionCallback {
    private static final String TAG = EaseChatRowViewHolder.class.getSimpleName();
    private Context context;
    private EaseChatRow chatRow;
    private EMMessage message;
    private MessageListItemClickListener mItemClickListener;

    public EaseChatRowViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView);
        // 解决view宽和高不显示的问题
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(params);
        this.context = itemView.getContext();
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public void initView(View itemView) {
        this.chatRow = (EaseChatRow) itemView;
    }

    @Override
    public void setData(EMMessage item, int position) {
        chatRow.resetViewState();
        message = item;
        chatRow.setUpView(item, position, mItemClickListener, this);
        handleMessage();
    }

    @Override
    public void setDataList(List<EMMessage> data, int position) {
        super.setDataList(data, position);
        chatRow.setTimestamp(position == 0 ? null : data.get(position - 1));
    }

    @Override
    public void onResendClick(EMMessage message) {

    }

    @Override
    public void onBubbleClick(EMMessage message) {

    }

    @Override
    public void onDetachedFromWindow() {

    }

    private void handleMessage() {
        if (message.direct() == EMMessage.Direct.SEND) {
            handleSendMessage(message);
        } else if (message.direct() == EMMessage.Direct.RECEIVE) {
            handleReceiveMessage(message);
        }
    }

    /**
     * send message
     * @param message
     */
    protected void handleSendMessage(final EMMessage message) {
        // Update the view according to the message current status.
        getChatRow().updateView(message);
    }

    /**
     * receive message
     * @param message
     */
    protected void handleReceiveMessage(EMMessage message) {

    }

    public Context getContext() {
        return context;
    }

    public EaseChatRow getChatRow() {
        return chatRow;
    }
}
