package com.hyphenate.easeui.modules.contact.interfaces;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hyphenate.easeui.modules.contact.EaseContactListLayout;

public interface IContactLayout {
    /**
     * 返回联系人列表布局
     * @return
     */
    EaseContactListLayout getContactList();

    /**
     * 获取下拉刷新控件
     * @return
     */
    SwipeRefreshLayout getSwipeRefreshLayout();

    /**
     * 展示简洁模式
     */
    void showSimple();

    /**
     * 展示常规模式
     */
    void showNormal();

    /**
     * 是否可以下拉刷新
     * @param canUseRefresh
     */
    void canUseRefresh(boolean canUseRefresh);
}
