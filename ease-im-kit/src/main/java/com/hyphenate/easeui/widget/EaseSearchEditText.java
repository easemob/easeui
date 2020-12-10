package com.hyphenate.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import com.hyphenate.easeui.R;

import androidx.appcompat.widget.AppCompatEditText;

public class EaseSearchEditText extends EditText {
    private float mLeftHeight;
    private float mLeftWidth;
    private float mRightHeight;
    private float mRightWidth;

    public EaseSearchEditText(Context context) {
        this(context, null);
    }

    public EaseSearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseSearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if(attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseSearchEditText);
            mLeftHeight = ta.getDimension(R.styleable.EaseSearchEditText_search_edit_drawable_left_height, 0);
            mLeftWidth = ta.getDimension(R.styleable.EaseSearchEditText_search_edit_drawable_left_width, 0);
            mRightHeight = ta.getDimension(R.styleable.EaseSearchEditText_search_edit_drawable_right_height, 0);
            mRightWidth = ta.getDimension(R.styleable.EaseSearchEditText_search_edit_drawable_right_width, 0);
            ta.recycle();
        }
        requestFocus();
        setDrawables();
    }

    private void setDrawables() {
        // No relative drawables, so just set any compat drawables
        Drawable[] existingAbs = getCompoundDrawables();
        Drawable left = existingAbs[0];
        Drawable right = existingAbs[2];
        if(left != null && (mLeftWidth != 0 && mLeftHeight != 0)) {
            left.setBounds(0, 0, (int)mLeftWidth, (int)mLeftHeight);
        }
        if(right != null) {
            // 暂时不显示
            right.setBounds(0, 0, 0, 0);
        }
        setCompoundDrawables(
                left != null ? left : existingAbs[0],
                existingAbs[1],
                right != null ? right : existingAbs[2],
                existingAbs[3]
        );
    }

}
