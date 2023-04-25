package com.hyphenate.easeui.modules.chat.interfaces;

import com.hyphenate.easeui.modules.menu.EaseChatFinishReason;

/**
 * 用于监听，由于群组被销毁，被群管理员移出群，聊天室被销毁以及被聊天室管理员移出等导致的退出聊天页面行为的监听。
 */
public interface OnChatFinishListener {
    /**
     * \~english
     * The callback that chat is finished.
     * @param reason
     * @param id
     */
    void onChatFinish(EaseChatFinishReason reason, String id);
}
