package com.hyphenate.easeui.modules.chat.interfaces;

import com.hyphenate.chat.EMMessage;

public interface OnRecallMessageResultListener {
    /**
     * 撤回成功
     * @param message
     */
    void recallSuccess(EMMessage message);

    /**
     * 撤回失败
     * @param code
     * @param errorMsg
     */
    void recallFail(int code, String errorMsg);
}
