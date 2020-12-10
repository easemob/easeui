package com.hyphenate.easeui.modules.chat;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class EaseInputEditText extends EditText implements View.OnKeyListener, TextView.OnEditorActionListener {
    private boolean ctrlPress = false;
    private OnEditTextChangeListener listener;

    public EaseInputEditText(Context context) {
        this(context, null);
    }

    public EaseInputEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnKeyListener(this);
        setOnEditorActionListener(this);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (listener != null) {
            listener.onEditTextHasFocus(focused);
        }
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                ctrlPress = true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                ctrlPress = false;
            }
        }
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN && ctrlPress)) {
            String s = getText().toString();
            if(listener != null) {
                setText("");
                listener.onClickKeyboardSendBtn(s);
            }
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * 设置监听
     * @param listener
     */
    public void setOnEditTextChangeListener(OnEditTextChangeListener listener) {
        this.listener = listener;
    }

    public interface OnEditTextChangeListener {

        /**
         * when send button clicked
         * @param content
         */
        void onClickKeyboardSendBtn(String content);

        /**
         * if edit text has focus
         */
        void onEditTextHasFocus(boolean hasFocus);
    }
}

