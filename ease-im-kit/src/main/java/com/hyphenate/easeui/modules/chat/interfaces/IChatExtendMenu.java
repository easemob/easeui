package com.hyphenate.easeui.modules.chat.interfaces;

public interface IChatExtendMenu {
    /**
     * 清理扩展功能
     */
    void clear();

    /**
     * 设置条目的排序
     * @param itemId
     * @param order
     */
    void setMenuOrder(int itemId, int order);

    /**
     * 添加新的扩展功能
     * @param name
     * @param drawableRes
     * @param itemId
     */
    void registerMenuItem(String name, int drawableRes, int itemId);

    /**
     * 添加新的扩展功能
     * @param name
     * @param drawableRes
     * @param itemId
     * @param order
     */
    void registerMenuItem(String name, int drawableRes, int itemId, int order);

    /**
     * 添加新的扩展功能
     * @param nameRes
     * @param drawableRes
     * @param itemId
     */
    void registerMenuItem(int nameRes, int drawableRes, int itemId);

    /**
     * 添加新的扩展功能
     * @param nameRes
     * @param drawableRes
     * @param itemId
     * @param order
     */
    void registerMenuItem(int nameRes, int drawableRes, int itemId, int order);

    /**
     * 设置条目监听
     * @param listener
     * @deprecated 已废弃。请用 {@link #addEaseChatExtendMenuItemClickListener} 代替。
     */
    void setEaseChatExtendMenuItemClickListener(EaseChatExtendMenuItemClickListener listener);

    /**
     * 添加条目监听
     * @param listener
     */
    default void addEaseChatExtendMenuItemClickListener(EaseChatExtendMenuItemClickListener listener){}

    /**
     * 移除条目监听
     * @param listener
     */
    default void removeEaseChatExtendMenuItemClickListener(EaseChatExtendMenuItemClickListener listener){}

    /**
     * 清除所有条目监听
     */
    default void clearEaseChatExtendMenuItemClickListener(){}
}
