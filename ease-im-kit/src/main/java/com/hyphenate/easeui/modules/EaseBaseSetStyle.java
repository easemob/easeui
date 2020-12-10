package com.hyphenate.easeui.modules;

import android.graphics.drawable.Drawable;

import com.hyphenate.easeui.widget.EaseImageView;

public class EaseBaseSetStyle {
    private float avatarSize;
    private EaseImageView.ShapeType shapeType;
    private float avatarRadius;
    private float borderWidth;
    private int borderColor;
    private float itemHeight;
    private Drawable bgDrawable;

    public float getAvatarSize() {
        return avatarSize;
    }

    public void setAvatarSize(float avatarSize) {
        this.avatarSize = avatarSize;
    }

    public EaseImageView.ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(EaseImageView.ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public void setShapeType(int shapeType) {
        if(shapeType == 1) {
            this.shapeType = EaseImageView.ShapeType.ROUND;
        }else if(shapeType == 2) {
            this.shapeType = EaseImageView.ShapeType.RECTANGLE;
        }else {
            if(shapeType >= 0) {
                this.shapeType = EaseImageView.ShapeType.NONE;
            }
        }
    }

    public float getAvatarRadius() {
        return avatarRadius;
    }

    public void setAvatarRadius(float avatarRadius) {
        this.avatarRadius = avatarRadius;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public float getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(float itemHeight) {
        this.itemHeight = itemHeight;
    }

    public Drawable getBgDrawable() {
        return bgDrawable;
    }

    public void setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
    }
}

