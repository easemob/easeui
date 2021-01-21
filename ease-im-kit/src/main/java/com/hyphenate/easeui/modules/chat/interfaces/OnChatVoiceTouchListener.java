package com.hyphenate.easeui.modules.chat.interfaces;

import android.view.MotionEvent;
import android.view.View;

public interface OnChatVoiceTouchListener {
    /**
     * 语音按压事件
     * @param v
     * @param event
     * @return
     */
    boolean onVoiceTouch(View v, MotionEvent event);
}
