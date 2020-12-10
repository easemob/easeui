package com.hyphenate.easeui.modules.chat.interfaces;

import android.graphics.drawable.Drawable;
import android.widget.EditText;

import com.hyphenate.easeui.modules.chat.EaseInputMenuStyle;

public interface IChatPrimaryMenu {
    /**
     * 菜单展示类型
     * @param style
     */
    void setMenuShowType(EaseInputMenuStyle style);

    /**
     * 常规模式
     */
    void showNormalStatus();

    /**
     * 文本输入模式
     */
    void showTextStatus();

    /**
     * 语音输入模式
     */
    void showVoiceStatus();

    /**
     * 表情输入模式
     */
    void showEmojiconStatus();

    /**
     * 更多模式
     */
    void showMoreStatus();

    /**
     * 隐藏扩展区模式
     */
    void hideExtendStatus();

    /**
     * 隐藏软键盘
     */
    void hideSoftKeyboard();

   /**
     * 输入表情
     * @param emojiContent
     */
    void onEmojiconInputEvent(CharSequence emojiContent);

    /**
     * 删除表情
     */
    void onEmojiconDeleteEvent();

    /**
     * 输入文本
     * @param text
     */
    void onTextInsert(CharSequence text);

    /**
     * 获取EditText
     * @return
     */
    EditText getEditText();

    /**
     * 设置输入框背景
     * @param bg
     */
    void setMenuBackground(Drawable bg);

    /**
     * 设置发送按钮背景
     * @param bg
     */
    void setSendButtonBackground(Drawable bg);

    /**
     * 设置监听
     * @param listener
     */
    void setEaseChatPrimaryMenuListener(EaseChatPrimaryMenuListener listener);
}
