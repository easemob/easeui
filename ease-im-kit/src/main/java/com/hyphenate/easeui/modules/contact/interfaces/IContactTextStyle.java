package com.hyphenate.easeui.modules.contact.interfaces;

import androidx.annotation.ColorInt;

interface IContactTextStyle {
    /**
     * 设置标题文字大小
     * @param textSize
     */
    void setTitleTextSize(int textSize);

    /**
     * 设置标题文字颜色
     * @param textColor
     */
    void setTitleTextColor(@ColorInt int textColor);

    /**
     * 设置header的字体大小
     * @param textSize
     */
    void setHeaderTextSize(int textSize);

    /**
     * 设置header的字体颜色
     * @param textColor
     */
    void setHeaderTextColor(@ColorInt int textColor);

}
