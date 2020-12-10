package com.hyphenate.easeui.modules.interfaces;

import com.hyphenate.easeui.modules.chat.interfaces.OnMenuChangeListener;
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper;
import com.hyphenate.easeui.modules.menu.MenuItemBean;

public interface IPopupWindow {
    /**
     * 是否展示默认的条目菜单
     * @param showDefault
     */
    void showItemDefaultMenu(boolean showDefault);

    /**
     * 清除所有菜单项
     */
    void clearMenu();

    /**
     * 添加菜单项
     * @param item
     */
    void addItemMenu(MenuItemBean item);

    /**
     * 添加菜单项
     * @param groupId
     * @param itemId
     * @param order
     * @param title
     */
    void addItemMenu(int groupId, int itemId, int order, String title);

    /**
     * 查找菜单对象，如果id不存在则返回null
     * @param id
     * @return
     */
    MenuItemBean findItem(int id);


    /**
     * 设置菜单项可见性
     * @param id
     * @param visible
     */
    void findItemVisible(int id, boolean visible);

    /**
     * 设置菜单条目监听
     * @param listener
     */
    void setOnPopupWindowItemClickListener(OnMenuChangeListener listener);

    /**
     * 返回菜单帮助类
     * @return
     */
    EasePopupWindowHelper getMenuHelper();
}
