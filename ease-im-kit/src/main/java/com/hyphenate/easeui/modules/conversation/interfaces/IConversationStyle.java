package com.hyphenate.easeui.modules.conversation.interfaces;

import android.graphics.drawable.Drawable;


import com.hyphenate.easeui.modules.conversation.model.EaseConversationSetStyle;
import com.hyphenate.easeui.modules.interfaces.IAvatarSet;

public interface IConversationStyle extends IAvatarSet, IConversationTextStyle {

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
     * 是否展示未读红点
     * @param hide
     */
    void hideUnreadDot(boolean hide);

    /**
     * 是否展示系统消息
     * @param show
     */
    void showSystemMessage(boolean show);

    /**
     * 未读数显示位置
     * 目前支持左侧和右侧两种
     * @param position
     */
    void showUnreadDotPosition(EaseConversationSetStyle.UnreadDotPosition position);
}
