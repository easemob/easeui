package com.hyphenate.easeui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * 为了解决出现“trying to use a recycled bitmap android.graphics.Bitmap@2d46e6b”的异常
 */
public class EImageView extends ImageView {
    public EImageView(Context context) {
        super(context);
    }

    public EImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

