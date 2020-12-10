package com.hyphenate.easeui.modules.conversation.interfaces;

import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;

import java.util.List;

/**
 * 会话列表加载数据的监听
 */
public interface OnConversationLoadListener {
    /**
     * 加载完成后回调
     * @param data
     */
    void loadDataFinish(List<EaseConversationInfo> data);

    /**
     * 加载数据失败后回调
     * @param message
     */
    default void loadDataFail(String message) {}

}
