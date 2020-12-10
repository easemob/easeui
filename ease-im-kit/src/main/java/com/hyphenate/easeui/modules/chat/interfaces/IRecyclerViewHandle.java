package com.hyphenate.easeui.modules.chat.interfaces;

import com.hyphenate.chat.EMMessage;

public interface IRecyclerViewHandle {
    /**
     * 是否可以使用默认的刷新
     * @param canUseRefresh
     */
    void canUseDefaultRefresh(boolean canUseRefresh);

    /**
     * 刷新数据
     */
    void refreshMessages();

    /**
     * 刷新并移动到最新的一条数据
     */
    void refreshToLatest();

    /**
     * 刷新单条数据
     * @param message
     */
    void refreshMessage(EMMessage message);

    /**
     * 删除单条数据
     * @param message
     */
    void removeMessage(EMMessage message);

    /**
     * 移动到指定position的位置
     * @param position
     */
    void moveToPosition(int position);
}

