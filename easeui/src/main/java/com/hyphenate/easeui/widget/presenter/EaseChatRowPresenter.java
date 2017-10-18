package com.hyphenate.easeui.widget.presenter;

import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

/**
 * Created by zhangsong on 17-10-12.
 */

public abstract class EaseChatRowPresenter implements EaseChatRow.EaseChatRowActionCallback{
    private EaseChatRow chatRow;

    private Context context;
    private EMMessage message;
    private int position;

    @Override
    public boolean onResendClick(EMMessage message) {
        message.setStatus(EMMessage.Status.CREATE);
        handleSendMessage(message);
        return true;
    }

    @Override
    public boolean onBubbleClick(EMMessage message) {
        return false;
    }

    public EaseChatRow createChatRow(Context cxt, EMMessage message, int position, BaseAdapter adapter) {
        this.context = cxt;
        chatRow = onCreateChatRow(cxt, message, position, adapter);
        return chatRow;
    }

    public void setup(EMMessage msg, int position,
                      EaseChatMessageList.MessageListItemClickListener itemClickListener,
                      EaseMessageListItemStyle itemStyle) {
        this.message = msg;
        this.position = position;

        chatRow.setUpView(message, position, itemClickListener, this, itemStyle);

        handleMessage();
    }

    protected void handleSendMessage(final EMMessage message) {
        EMMessage.Status status = message.status();

        // Update the view according to the message current status.
        getChatRow().updateView(message);

        if (status == EMMessage.Status.SUCCESS || status == EMMessage.Status.FAIL) {
            return;
        }

        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                getChatRow().updateView(message);
            }

            @Override
            public void onError(int code, String error) {
                Log.i("EaseChatRowPresenter", "onError: " + code + ", error: " + error);
                getChatRow().updateView(message);
            }

            @Override
            public void onProgress(int progress, String status) {
                getChatRow().updateView(message);
            }
        });

        // Already in progress, do not send again
        if (status == EMMessage.Status.INPROGRESS) {
            return;
        }

        // Send the message
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    protected void handleReceiveMessage(EMMessage message) {
    }

    protected abstract EaseChatRow onCreateChatRow(Context cxt, EMMessage message, int position, BaseAdapter adapter);

    protected EaseChatRow getChatRow() {
        return chatRow;
    }

    protected Context getContext() {
        return context;
    }

    protected EMMessage getMessage() {
        return message;
    }

    protected int getPosition() {
        return position;
    }

    private void handleMessage() {
        if (message.direct() == EMMessage.Direct.SEND) {
            handleSendMessage(message);
        } else if (message.direct() == EMMessage.Direct.RECEIVE) {
            handleReceiveMessage(message);
        }
    }
}
