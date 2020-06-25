package com.hyphenate.easeui.viewholder;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseAdapter;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.hyphenate.util.EMLog;

import java.security.MessageDigest;
import java.util.List;


public class EaseChatRowViewHolder extends EaseMessageAdapter.ViewHolder<EMMessage> implements EaseChatRow.EaseChatRowActionCallback {
    private static final String TAG = EaseChatRowViewHolder.class.getSimpleName();
    private Context context;
    private EaseChatRow chatRow;
    private EMMessage message;
    private MessageListItemClickListener mItemClickListener;
    private EaseMessageListItemStyle mItemStyle;

    public EaseChatRowViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener,
                                 EaseMessageListItemStyle itemStyle) {
        super(itemView);
        // 解决view宽和高不显示的问题
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(params);
        this.context = itemView.getContext();
        this.mItemClickListener = itemClickListener;
        this.mItemStyle = itemStyle;
    }

    @Override
    public void initView(View itemView) {
        this.chatRow = (EaseChatRow) itemView;
    }

    @Override
    public void setData(EMMessage item, int position) {
        message = item;
        chatRow.setUpView(item, position, mItemClickListener, this, mItemStyle);
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

        if (message.status() == EMMessage.Status.INPROGRESS) {
            EMLog.i("handleSendMessage", "Message is INPROGRESS");
            if (this.mItemClickListener != null) {
                this.mItemClickListener.onMessageInProgress(message);
            }
        }
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
