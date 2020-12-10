/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hyphenate.easeui.R;

/**
 * side bar
 */
public class EaseSidebar extends View{
	private Paint paint;
	private float ItemHeight;
	private Context context;
	private OnTouchEventListener mListener;
	private String[] sections = new String[]{"A","B","C","D","E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z","#"};
	private String topText;
	private int mTextColor;
	private static final String DEFAULT_COLOR = "#8C8C8C";
	private static final float DEFAULT_TEXT_SIZE = 10;
	private float mTextSize;
	private int mBgColor;
	private int mWidth, mHeight;
	private float mTextCoefficient = 1;

	public EaseSidebar(Context context) {
		this(context, null);
	}

	public EaseSidebar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EaseSidebar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		initAttrs(attrs);
		init();
	}

	private void initAttrs(AttributeSet attrs) {
		if(attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EaseSidebar);
			int topTextId = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_top_text, -1);
			if(topTextId != -1) {
				topText = context.getResources().getString(topTextId);
			}else {
				topText = a.getString(R.styleable.EaseSidebar_ease_side_bar_top_text);
			}
			int textColorId = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_text_color, -1);
			if(textColorId != -1) {
				mTextColor = ContextCompat.getColor(context, textColorId);
			}else {
				mTextColor = a.getColor(R.styleable.EaseSidebar_ease_side_bar_text_color, Color.parseColor(DEFAULT_COLOR));
			}
			mTextSize = a.getDimension(R.styleable.EaseSidebar_ease_side_bar_text_size, DEFAULT_TEXT_SIZE);
			int bgId = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_background, -1);
			if(bgId != -1) {
				mBgColor = ContextCompat.getColor(context, textColorId);
			}else {
				mBgColor = a.getColor(R.styleable.EaseSidebar_ease_side_bar_background, Color.TRANSPARENT);
			}
			int headArrays = a.getResourceId(R.styleable.EaseSidebar_ease_side_bar_head_arrays, -1);
			if(headArrays != -1) {
			    sections = getResources().getStringArray(headArrays);
			}else {
				sections = new String[]{"A","B","C","D","E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z","#"};
			}
		}
	}

	private void init(){
		if(sections.length > 27) {
		    if(!TextUtils.isEmpty(topText)) {
		        sections[0] = topText;
		    }
		}
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(mTextColor);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(mTextSize);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 获取view的高度
		mWidth = w;
		mHeight = h;
		checkTextSize();
	}

	/**
	 * 校验文字大小是否合适
	 */
	private void checkTextSize() {
		if(paint != null) {
			Paint.FontMetrics metrics = paint.getFontMetrics();
			float textItemHeight = metrics.bottom - metrics.top;
			if(sections.length * textItemHeight > mHeight) {
				mTextCoefficient = mHeight / (sections.length * textItemHeight);
				paint.setTextSize(paint.getTextSize() * mTextCoefficient);
			}else {
				paint.setTextSize(mTextSize);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mBgColor != Color.TRANSPARENT) {
			canvas.drawColor(mBgColor);
		}
		float center = getWidth() / 2;
		ItemHeight = getHeight() / sections.length;
		for (int i = sections.length - 1; i > -1; i--) {
			canvas.drawText(sections[i], center, ItemHeight * (i+1), paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pointer = sectionForPoint(event.getY());
		String section = sections[pointer];
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 提供对外的接口，进行操作
				if(mListener != null) {
					mListener.onActionDown(event, section);
				}
				return true;
			case MotionEvent.ACTION_MOVE:
				// 提供对外的接口，便于开发者操作
				if(mListener != null) {
					mListener.onActionMove(event, section);
				}
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if(mListener != null) {
					mListener.onActionUp(event);
				}
				return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 获取移动时的字符
	 * @param y
	 * @return
	 */
	private int sectionForPoint(float y) {
		int index = (int) (y / ItemHeight);
		if(index < 0) {
		    index = 0;
		}
		if(index > sections.length -1) {
		    index = sections.length - 1;
		}
		return index;
	}

	/**
	 * 绘制背景色
	 * @param color
	 */
	public void drawBackground(@ColorRes int color) {
		mBgColor = ContextCompat.getColor(context, color);
		postInvalidate();
	}

	public void drawBackgroundDrawable(@DrawableRes int drawableId) {
		setBackground(ContextCompat.getDrawable(context, drawableId));
	}

	public void drawBackgroundDrawable( Drawable drawable) {
		setBackground(drawable);
	}

	/**
	 * set touch event listener
	 * @param listener
	 */
	public void setOnTouchEventListener(OnTouchEventListener listener) {
		this.mListener = listener;
	}

	public interface OnTouchEventListener {
		/**
		 * 按下的监听
		 * @param event
		 * @param pointer
		 */
		void onActionDown(MotionEvent event, String pointer);

		/**
		 * 移动的监听
		 * @param event
		 * @param pointer
		 */
		void onActionMove(MotionEvent event, String pointer);

		/**
		 * 抬起的监听
		 * @param event
		 */
		void onActionUp(MotionEvent event);
	}

}
