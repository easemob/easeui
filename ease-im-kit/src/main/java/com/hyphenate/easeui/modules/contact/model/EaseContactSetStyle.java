package com.hyphenate.easeui.modules.contact.model;

import android.graphics.drawable.Drawable;

import com.hyphenate.easeui.modules.EaseBaseSetStyle;

public class EaseContactSetStyle extends EaseBaseSetStyle {
    private int titleTextColor;
    private float titleTextSize;
    private int headerTextColor;
    private float headerTextSize;
    private Drawable headerBgDrawable;
    private Drawable avatarDefaultSrc;
    private boolean showItemHeader;

    public int getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
    }

    public float getTitleTextSize() {
        return titleTextSize;
    }

    public void setTitleTextSize(float titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public int getHeaderTextColor() {
        return headerTextColor;
    }

    public void setHeaderTextColor(int headerTextColor) {
        this.headerTextColor = headerTextColor;
    }

    public float getHeaderTextSize() {
        return headerTextSize;
    }

    public void setHeaderTextSize(float headerTextSize) {
        this.headerTextSize = headerTextSize;
    }

    public Drawable getHeaderBgDrawable() {
        return headerBgDrawable;
    }

    public void setHeaderBgDrawable(Drawable headerBgDrawable) {
        this.headerBgDrawable = headerBgDrawable;
    }

    public Drawable getAvatarDefaultSrc() {
        return avatarDefaultSrc;
    }

    public void setAvatarDefaultSrc(Drawable avatarDefaultSrc) {
        this.avatarDefaultSrc = avatarDefaultSrc;
    }

    public boolean isShowItemHeader() {
        return showItemHeader;
    }

    public void setShowItemHeader(boolean showItemHeader) {
        this.showItemHeader = showItemHeader;
    }
}

