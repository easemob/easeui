package com.hyphenate.easeui.modules.chat.interfaces;

import android.view.MotionEvent;
import android.view.View;

public interface ChatInputMenuListener {
    /**
     * when typing on the edit-text layout.
     */
    void onTyping(CharSequence s, int start, int before, int count);

    /**
     * when send message button pressed
     *
     * @param content
     *            message content
     */
    void onSendMessage(String content);

    /**
     * when big icon pressed
     * @param emojicon
     */
    void onExpressionClicked(Object emojicon);

    /**
     * when speak button is touched
     * @param v
     * @param event
     * @return
     */
    boolean onPressToSpeakBtnTouch(View v, MotionEvent event);

    /**
     * when click the item of extend menu
     * @param itemId
     * @param view
     */
    void onChatExtendMenuItemClick(int itemId, View view);
}
