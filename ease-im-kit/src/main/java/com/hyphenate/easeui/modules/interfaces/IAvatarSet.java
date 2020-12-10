package com.hyphenate.easeui.modules.interfaces;

import android.graphics.drawable.Drawable;

import com.hyphenate.easeui.widget.EaseImageView;

public interface IAvatarSet {
    /**
     * 设置默认头像
     * @param src
     */
    default void setAvatarDefaultSrc(Drawable src){}

    /**
     * 设置头像大小，长和宽是相同的
     * @param avatarSize
     */
    void setAvatarSize(float avatarSize);

    /**
     * 设置头像样式
     * @param shapeType
     */
    void setAvatarShapeType(EaseImageView.ShapeType shapeType);

    /**
     * 设置头像半径
     * @param radius
     */
    void setAvatarRadius(int radius);

    /**
     * 设置外边框宽度
     * @param borderWidth
     */
    void setAvatarBorderWidth(int borderWidth);

    /**
     * 设置外边框颜色
     * @param borderColor
     */
    void setAvatarBorderColor(int borderColor);
}

