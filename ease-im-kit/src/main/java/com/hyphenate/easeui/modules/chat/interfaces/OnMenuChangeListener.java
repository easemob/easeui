package com.hyphenate.easeui.modules.chat.interfaces;

import android.widget.PopupWindow;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper;
import com.hyphenate.easeui.modules.menu.MenuItemBean;

/**
 * {@link com.hyphenate.easeui.modules.menu.EasePopupWindowHelper}中的条目点击事件
 */
public interface OnMenuChangeListener {
    /**
     * 展示Menu之前
     * @param helper
     * @param message
     */
    void onPreMenu(EasePopupWindowHelper helper, EMMessage message);

    /**
     * 点击条目
     * @param item
     * @param message
     */
    boolean onMenuItemClick(MenuItemBean item, EMMessage message);

    /**
     * 消失
     * @param menu
     */
    default void onDismiss(PopupWindow menu) {}
}