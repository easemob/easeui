package com.hyphenate.easeui.widget;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class EaseChatEditText extends EditText{

    public EaseChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EaseChatEditText(Context context) {
        super(context);
    }

    
    @Override
    public boolean onTextContextMenuItem(int id) {
        if(id == android.R.id.paste){
            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText("atmessage:" + clipboardManager.getText());
        }
        return super.onTextContextMenuItem(id);
    }
    
}
