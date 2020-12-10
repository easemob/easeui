package com.hyphenate.easeui.modules.conversation.interfaces;

import androidx.annotation.ColorInt;

interface IConversationTextStyle {
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
     * 设置内容文字大小
     * @param textSize
     */
    void setContentTextSize(int textSize);

    /**
     * 设置内容文字颜色
     * @param textColor
     */
    void setContentTextColor(@ColorInt int textColor);

    /**
     * 设置日期文字大小
     * @param textSize
     */
    void setDateTextSize(int textSize);

    /**
     * 设置日期文字颜色
     * @param textColor
     */
    void setDateTextColor(@ColorInt int textColor);
}
