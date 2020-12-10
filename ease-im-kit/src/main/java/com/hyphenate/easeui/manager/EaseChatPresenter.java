package com.hyphenate.easeui.manager;

import android.content.Context;
import android.util.Log;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroupReadAck;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.model.EaseNotifier;

import java.util.List;

public class EaseChatPresenter implements EMMessageListener {
    private static final String TAG = EaseChatPresenter.class.getSimpleName();
    public Context context;

    public EaseChatPresenter() {
        EMClient.getInstance().chatManager().addMessageListener(this);
    }


    public void attachApp(Context context) {
        this.context = context;
    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        Log.e(TAG, "EaseChatPresenter onMessageReceived messages.size = "+messages.size());
        EaseAtMessageHelper.get().parseMessages(messages);
    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {

    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageRead(List<EMMessage> messages) {

    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageDelivered(List<EMMessage> messages) {

    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageRecalled(List<EMMessage> messages) {
        // 撤回消息的回调
    }

    /**
     * EMMessageListener
     * @param message
     * @param change
     */
    @Override
    public void onMessageChanged(EMMessage message, Object change) {

    }

    @Override
    public void onGroupMessageRead(List<EMGroupReadAck> groupReadAcks) {
        for (EMGroupReadAck ack : groupReadAcks) {
            EaseDingMessageHelper.get().handleGroupReadAck(ack);
        }
    }

    public EaseNotifier getNotifier() {
        return EaseIM.getInstance().getNotifier();
    }
}
