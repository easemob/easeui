package com.hyphenate.easeui.modules.chat.model;


import android.graphics.drawable.Drawable;

public class EaseChatItemStyleHelper {
    private static EaseChatItemStyleHelper instance;
    private EaseChatSetStyle style;

    private EaseChatItemStyleHelper(){
        style = new EaseChatSetStyle();
        style.setShowAvatar(true);
        style.setShowNickname(false);
    }

    public static EaseChatItemStyleHelper getInstance() {
        if(instance == null) {
            synchronized (EaseChatItemStyleHelper.class) {
                if(instance == null) {
                    instance = new EaseChatItemStyleHelper();
                }
            }
        }
        return instance;
    }

    public EaseChatSetStyle getStyle() {
        return style;
    }

    public void clear() {
        style = null;
        instance = null;
    }

    public void setAvatarSize(float avatarSize) {
        style.setAvatarSize(avatarSize);
    }

    public void setShapeType(int shapeType) {
        style.setShapeType(shapeType);
    }

    public void setAvatarRadius(float avatarRadius) {
        style.setAvatarRadius(avatarRadius);
    }

    public void setBorderWidth(float borderWidth) {
        style.setBorderWidth(borderWidth);
    }

    public void setBorderColor(int borderColor) {
        style.setBorderColor(borderColor);
    }

    public void setItemHeight(float itemHeight) {
        style.setItemHeight(itemHeight);
    }

    public void setBgDrawable(Drawable bgDrawable) {
        style.setBgDrawable(bgDrawable);
    }

    public void setTextSize(int textSize) {
        style.setTextSize(textSize);
    }

    public void setTextColor(int textColor) {
        style.setTextColor(textColor);
    }

    public void setItemMinHeight(int itemMinHeight) {
        style.setItemMinHeight(itemMinHeight);
    }

    public void setTimeTextSize(int timeTextSize) {
        style.setTimeTextSize(timeTextSize);
    }

    public void setTimeTextColor(int timeTextColor) {
        style.setTimeTextColor(timeTextColor);
    }

    public void setTimeBgDrawable(Drawable timeBgDrawable) {
        style.setTimeBgDrawable(timeBgDrawable);
    }

    public void setAvatarDefaultSrc(Drawable avatarDefaultSrc) {
        style.setAvatarDefaultSrc(avatarDefaultSrc);
    }

    public void setShowNickname(boolean showNickname) {
        style.setShowNickname(showNickname);
    }

    public void setShowAvatar(boolean showAvatar) {
        style.setShowAvatar(showAvatar);
    }

    public void setReceiverBgDrawable(Drawable receiverBgDrawable) {
        style.setReceiverBgDrawable(receiverBgDrawable);
    }

    public void setSenderBgDrawable(Drawable senderBgDrawable) {
        style.setSenderBgDrawable(senderBgDrawable);
    }

    public void setItemShowType(int itemShowType) {
        style.setItemShowType(itemShowType);
    }
}

