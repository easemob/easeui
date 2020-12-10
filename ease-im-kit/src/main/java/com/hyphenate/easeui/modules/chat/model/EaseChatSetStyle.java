package com.hyphenate.easeui.modules.chat.model;

import android.graphics.drawable.Drawable;

import com.hyphenate.easeui.modules.EaseBaseSetStyle;

public class EaseChatSetStyle extends EaseBaseSetStyle {
    private int textSize;
    private int textColor;
    private int itemMinHeight;
    private int timeTextSize;
    private int timeTextColor;
    private Drawable timeBgDrawable;
    private Drawable avatarDefaultSrc;
    private boolean showNickname;
    private boolean showAvatar;
    private Drawable receiverBgDrawable;
    private Drawable senderBgDrawable;
    private int itemShowType;

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getItemMinHeight() {
        return itemMinHeight;
    }

    public void setItemMinHeight(int itemMinHeight) {
        this.itemMinHeight = itemMinHeight;
    }

    public int getTimeTextSize() {
        return timeTextSize;
    }

    public void setTimeTextSize(int timeTextSize) {
        this.timeTextSize = timeTextSize;
    }

    public int getTimeTextColor() {
        return timeTextColor;
    }

    public void setTimeTextColor(int timeTextColor) {
        this.timeTextColor = timeTextColor;
    }

    public Drawable getTimeBgDrawable() {
        return timeBgDrawable;
    }

    public void setTimeBgDrawable(Drawable timeBgDrawable) {
        this.timeBgDrawable = timeBgDrawable;
    }

    public Drawable getAvatarDefaultSrc() {
        return avatarDefaultSrc;
    }

    public void setAvatarDefaultSrc(Drawable avatarDefaultSrc) {
        this.avatarDefaultSrc = avatarDefaultSrc;
    }

    public boolean isShowNickname() {
        return showNickname;
    }

    public void setShowNickname(boolean showNickname) {
        this.showNickname = showNickname;
    }

    public boolean isShowAvatar() {
        return showAvatar;
    }

    public void setShowAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
    }

    public Drawable getReceiverBgDrawable() {
        return receiverBgDrawable;
    }

    public void setReceiverBgDrawable(Drawable receiverBgDrawable) {
        this.receiverBgDrawable = receiverBgDrawable;
    }

    public Drawable getSenderBgDrawable() {
        return senderBgDrawable;
    }

    public void setSenderBgDrawable(Drawable senderBgDrawable) {
        this.senderBgDrawable = senderBgDrawable;
    }

    public int getItemShowType() {
        return itemShowType;
    }

    public void setItemShowType(int itemShowType) {
        this.itemShowType = itemShowType;
    }
}

