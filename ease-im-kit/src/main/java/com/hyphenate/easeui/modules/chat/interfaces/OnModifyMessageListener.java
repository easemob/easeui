package com.hyphenate.easeui.modules.chat.interfaces;


/**
 * 用户监听编辑消息是否成功
 */
public interface OnModifyMessageListener {
    /**
     * 变更消息成功
     * @param messageId
     */
    void onModifyMessageSuccess(String messageId);

    /**
     * 变更消息失败
     * @param messageId
     * @param code
     * @param error
     */
    void onModifyMessageFailure(String messageId, int code, String error);
}
