package com.hyphenate.easeui.interfaces;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMMessage;

public abstract class EaseMessageCallback implements EMCallBack {

    @Override
    public void onSuccess() {

    }

    public abstract void onSuccess(EMMessage message, int position);

    @Override
    public void onError(int code, String error) {

    }

    @Override
    public void onProgress(int progress, String status) {

    }
}
