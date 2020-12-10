package com.hyphenate.easeui.modules.conversation.interfaces;

public interface OnConversationChangeListener {
    /**
     * 通知单条变化
     * @param position
     */
    void notifyItemChange(int position);

    /**
     * 通知所有数据变化
     */
    void notifyAllChange();

    /**
     * 通知移除单条
     * @param position
     */
    void notifyItemRemove(int position);
}
