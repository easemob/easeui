package com.hyphenate.easeui.modules.interfaces;

import com.hyphenate.easeui.modules.menu.OnPopupMenuDismissListener;
import com.hyphenate.easeui.modules.menu.OnPopupMenuItemClickListener;
import com.hyphenate.easeui.modules.menu.EasePopupMenuHelper;
import com.hyphenate.easeui.modules.menu.OnPopupMenuPreShowListener;

public interface IPopupMenu {
    /**
     * 清除所有菜单项
     */
    void clearMenu();

    /**
     * 添加菜单项
     * @param groupId
     * @param itemId
     * @param order
     * @param title
     */
    void addItemMenu(int groupId, int itemId, int order, String title);

    /**
     * 设置菜单项可见性
     * @param id
     * @param visible
     */
    void findItemVisible(int id, boolean visible);

    /**
     * PopupMenu展示前的监听，可以对PopupMenu进行设置，如添加菜单项，隐藏或者显示菜单项
     * @param preShowListener
     */
    void setOnPopupMenuPreShowListener(OnPopupMenuPreShowListener preShowListener);

    /**
     * 设置菜单条目监听
     * @param listener
     */
    void setOnPopupMenuItemClickListener(OnPopupMenuItemClickListener listener);

    /**
     * 监听菜单dismiss事件
     * @param listener
     */
    void setOnPopupMenuDismissListener(OnPopupMenuDismissListener listener);

    /**
     * 返回菜单帮助类
     * @return
     */
    EasePopupMenuHelper getMenuHelper();
}
