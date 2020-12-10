package com.hyphenate.easeui.modules.conversation.model;


import com.hyphenate.easeui.modules.EaseBaseSetStyle;

public class EaseConversationSetStyle extends EaseBaseSetStyle {
    private int titleTextColor;
    private float titleTextSize;
    private int contentTextColor;
    private float contentTextSize;
    private int dateTextColor;
    private float dateTextSize;
    private int mentionTextColor;
    private float mentionTextSize;
    private boolean hideUnreadDot;
    private UnreadDotPosition unreadDotPosition;
    private boolean showSystemMessage;

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

    public int getContentTextColor() {
        return contentTextColor;
    }

    public void setContentTextColor(int contentTextColor) {
        this.contentTextColor = contentTextColor;
    }

    public float getContentTextSize() {
        return contentTextSize;
    }

    public void setContentTextSize(float contentTextSize) {
        this.contentTextSize = contentTextSize;
    }

    public int getDateTextColor() {
        return dateTextColor;
    }

    public void setDateTextColor(int dateTextColor) {
        this.dateTextColor = dateTextColor;
    }

    public float getDateTextSize() {
        return dateTextSize;
    }

    public void setDateTextSize(float dateTextSize) {
        this.dateTextSize = dateTextSize;
    }

    public int getMentionTextColor() {
        return mentionTextColor;
    }

    public void setMentionTextColor(int mentionTextColor) {
        this.mentionTextColor = mentionTextColor;
    }

    public float getMentionTextSize() {
        return mentionTextSize;
    }

    public void setMentionTextSize(float mentionTextSize) {
        this.mentionTextSize = mentionTextSize;
    }

    public boolean isHideUnreadDot() {
        return hideUnreadDot;
    }

    public void setHideUnreadDot(boolean hideUnreadDot) {
        this.hideUnreadDot = hideUnreadDot;
    }

    public UnreadDotPosition getUnreadDotPosition() {
        return unreadDotPosition;
    }

    public void setUnreadDotPosition(UnreadDotPosition unreadDotPosition) {
        this.unreadDotPosition = unreadDotPosition;
    }

    public boolean isShowSystemMessage() {
        return showSystemMessage;
    }

    public void setShowSystemMessage(boolean showSystemMessage) {
        this.showSystemMessage = showSystemMessage;
    }

    public enum UnreadDotPosition {
        LEFT, RIGHT
    }
}

