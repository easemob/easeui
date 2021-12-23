package com.hyphenate.easeui.modules.chat.interfaces;

import android.view.View;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.model.EaseEvent;

/**
 * 用于监听{@link com.hyphenate.easeui.modules.chat.EaseChatLayout}中的变化
 */
public interface OnChatLayoutListener {

    /**
     * 点击消息bubble区域
     * @param message
     * @return
     */
    boolean onBubbleClick(EMMessage message);

    /**
     * 长按消息bubble区域
     * @param v
     * @param message
     * @return
     */
    boolean onBubbleLongClick(View v, EMMessage message);

    /**
     * 点击头像
     * @param username
     */
    void onUserAvatarClick(String username);

    /**
     * 长按头像
     * @param username
     */
    void onUserAvatarLongClick(String username);

    /**
     * 条目点击
     * @param view
     * @param itemId
     */
    void onChatExtendMenuItemClick(View view, int itemId);

    /**
     * EditText文本变化监听
     * @param s
     * @param start
     * @param before
     * @param count
     */
    void onTextChanged(CharSequence s, int start, int before, int count);

    /**
     * 发送消息成功后的回调
     * @param message
     */
    default void onChatSuccess(EMMessage message){}

    /**
     * 聊天中错误信息
     * @param code
     * @param errorMsg
     */
    void onChatError(int code, String errorMsg);

    /**
     * 用于监听其他人正在数据事件
     * @param action 输入事件 TypingBegin为开始 TypingEnd为结束
     */
    default void onOtherTyping(String action){}

}