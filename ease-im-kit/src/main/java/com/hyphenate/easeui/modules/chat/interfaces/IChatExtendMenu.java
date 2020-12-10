package com.hyphenate.easeui.modules.chat.interfaces;

public interface IChatExtendMenu {
    /**
     * 清理扩展功能
     */
    void clear();

    /**
     * 添加新的扩展功能
     * @param name
     * @param drawableRes
     * @param itemId
     */
    void registerMenuItem(String name, int drawableRes, int itemId);

    /**
     * 添加新的扩展功能
     * @param nameRes
     * @param drawableRes
     * @param itemId
     */
    void registerMenuItem(int nameRes, int drawableRes, int itemId);

    /**
     * 设置条目监听
     * @param listener
     */
    void setEaseChatExtendMenuItemClickListener(EaseChatExtendMenuItemClickListener listener);
}
