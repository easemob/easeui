package com.hyphenate.easeui.modules.contact.interfaces;

import android.graphics.drawable.Drawable;

import com.hyphenate.easeui.modules.interfaces.IAvatarSet;

public interface IContactListStyle extends IAvatarSet, IContactTextStyle {
    /**
     * 设置条目背景
     * @param backGround
     */
    void setItemBackGround(Drawable backGround);

    /**
     * 设置条目高度
     * @param height
     */
    void setItemHeight(int height);

    /**
     * 设置header的背景
     * @param backGround
     */
    void setHeaderBackGround(Drawable backGround);
}
