package com.easemob.easeui.widget;

import com.bumptech.glide.load.resource.transcode.BitmapBytesTranscoder;
import com.bumptech.glide.request.target.SquaringDrawable;
import com.easemob.analytics.EMCollectorUtils;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.util.DensityUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.BitmapCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by lzan13 on 2015/4/30. 自定义 ImageView 控件，实现了圆角和边框，以及按下变色
 */
public class EaseImageView extends ImageView {
	// 图片按下的画笔
	private Paint mPressPaint;
	// 图片的宽高
	private int mWidth;
	private int mHeight;
	// 边框宽度
	private int mBorderWidth;
	// 边框颜色
	private int mBorderColor;
	// 按下的透明度
	private int mPressAlpha;
	// 按下的颜色
	private int mPressColor;
	// 圆角半径
	private int mRadius;
	// 图片类型（矩形，圆形）
	private int mShapeType;

	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
	private static final int COLORDRAWABLE_DIMENSION = 1;

	public EaseImageView(Context context) {
		super(context);
		init(context, null);
	}

	public EaseImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public EaseImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	/**
	 * 初始化自定义控件，这里会初始化控件的一些属性 依次是默认值，然后是通过EaseUI设置的值，最后是xml布局文件定义的值
	 * EaseUI设置的会覆盖默认值， 布局文件设置的值会覆盖EaseUI设置的值
	 * 
	 * @param context
	 * @param attrs
	 */
	private void init(Context context, AttributeSet attrs) {
		// 初始化默认值
		mBorderWidth = 2;
		mBorderColor = 0xddffffff;
		mPressAlpha = 128;
		mPressColor = 0x42000000;
		mRadius = 8;
		mShapeType = 1;

		// 获取通过调用 EaseUI 设置的全局 EaseImageView 控件的属性
		SharedPreferences sp = context.getSharedPreferences(EaseConstant.EASEUI_SHARED_NAME, Context.MODE_PRIVATE);
		mBorderWidth = sp.getInt(EaseConstant.EASEUI_IMAGEVIEW_BORDER_WIDTH, mBorderWidth);
		mBorderColor = sp.getInt(EaseConstant.EASEUI_IMAGEVIEW_BORDER_COLOR, mBorderColor);
		mRadius = sp.getInt(EaseConstant.EASEUI_IMAGEVIEW_RADIUS, mRadius);
		mShapeType = sp.getInt(EaseConstant.EASEUI_IMAGEVIEW_SHAPE_TYPE, mShapeType);

		// 获取通过布局文件设置的控件的属性值
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EaseImageView);
			mBorderWidth = array.getDimensionPixelOffset(R.styleable.EaseImageView_border_width, mBorderWidth);
			mBorderColor = array.getColor(R.styleable.EaseImageView_border_color, mBorderColor);
			mPressAlpha = array.getInteger(R.styleable.EaseImageView_press_alpha, mPressAlpha);
			mPressColor = array.getColor(R.styleable.EaseImageView_press_color, mPressColor);
			mRadius = array.getDimensionPixelSize(R.styleable.EaseImageView_radius, mRadius);
			mShapeType = array.getInteger(R.styleable.EaseImageView_shape_type, mShapeType);
			array.recycle();
		}

		// 这里调用sdk中封装的dp 转 px方法，为了保证在界面上显示的一致
		mBorderWidth = DensityUtil.dip2px(context, mBorderWidth);
		mRadius = DensityUtil.dip2px(context, mRadius);

		// 按下的画笔设置
		mPressPaint = new Paint();
		mPressPaint.setAntiAlias(true);
		mPressPaint.setStyle(Paint.Style.FILL);
		mPressPaint.setColor(mPressColor);
		mPressPaint.setAlpha(0);
		mPressPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

		setClickable(true);
		setDrawingCacheEnabled(true);
		setWillNotDraw(false);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		// 获取当前控件的 drawable
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}
		// 这里 get 回来的宽度和高度是当前控件相对应的宽度和高度（在 xml 设置）
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		// 获取 bitmap，即传入 imageview 的 bitmap
		// Bitmap bitmap = ((BitmapDrawable) ((SquaringDrawable)
		// drawable).getCurrent()).getBitmap();
		// 这里参考赵鹏的获取 bitmap 方式，因为上边的获取会导致 Glide 加载的drawable 强转为 BitmapDrawable
		// 出错
		Bitmap bitmap = getBitmapFromDrawable(drawable);

		drawDrawable(canvas, bitmap);

		// 绘制按下的效果
		drawPress(canvas);
		// 绘制边框
		drawBorder(canvas);
	}

	/**
	 * 实现圆角的绘制
	 *
	 * @param canvas
	 * @param bitmap
	 */
	private void drawDrawable(Canvas canvas, Bitmap bitmap) {
		// 画笔
		Paint paint = new Paint();
		// 颜色设置
		paint.setColor(0xffffffff);
		// 抗锯齿
		paint.setAntiAlias(true);
		// Paint 的 Xfermode，PorterDuff.Mode.SRC_IN 取两层图像的交集部门, 只显示上层图像。
		PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
		// 标志
		int saveFlags = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
				| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
		canvas.saveLayer(0, 0, mWidth, mHeight, null, saveFlags);

		if (mShapeType == 0) {
			// 画遮罩，画出来就是一个和空间大小相匹配的圆（这里在半径上 -2 是为了在有边框是不至于让图片超出边框）
			canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - 2, paint);
		} else {
			// 当ShapeType = 1 时 图片为圆角矩形  （这里在宽度上 -1 是为了在有边框是不至于让图片超出边框）
			RectF rectf = new RectF(1, 1, getWidth() - 1, getHeight() - 1);
			canvas.drawRoundRect(rectf, mRadius, mRadius, paint);
		}

		paint.setXfermode(xfermode);

		// 空间的大小 / bitmap 的大小 = bitmap 缩放的倍数 
		float scaleWidth = ((float) getWidth()) / bitmap.getWidth();
		float scaleHeight = ((float) getHeight()) / bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// bitmap 缩放
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		// draw 上去
		canvas.drawBitmap(bitmap, 0, 0, paint);
		canvas.restore();
	}

	/**
	 * 绘制控件的按下效果
	 *
	 * @param canvas
	 */
	private void drawPress(Canvas canvas) {

		// 这里根据类型判断绘制的效果是圆形还是矩形
		if (mShapeType == 0) {
			canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mPressPaint);
		} else if (mShapeType == 1) {
			RectF rectF = new RectF(0, 0, mWidth, mHeight);
			canvas.drawRoundRect(rectF, mRadius, mRadius, mPressPaint);
		}
	}

	/**
	 * 绘制自定义控件边框
	 *
	 * @param canvas
	 */
	private void drawBorder(Canvas canvas) {
		if (mBorderWidth > 0) {
			Paint paint = new Paint();
			paint.setStrokeWidth(mBorderWidth);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(mBorderColor);
			paint.setAntiAlias(true);
			// 根据控件类型的属性去绘制圆形或者矩形
			if (mShapeType == 0) {
				canvas.drawCircle(mWidth / 2, mHeight / 2, (mWidth - mBorderWidth) / 2, paint);
			} else {
				// 当ShapeType = 1 时 图片为圆角矩形
				RectF rectf = new RectF(mBorderWidth / 2, mBorderWidth / 2, getWidth() - mBorderWidth / 2,
						getHeight() - mBorderWidth / 2);
				canvas.drawRoundRect(rectf, mRadius, mRadius, paint);
			}
		}
	}

	/**
	 * 重写父类的 onSizeChanged 方法，检测控件宽高的变化
	 *
	 * @param w
	 * @param h
	 * @param oldw
	 * @param oldh
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
	}

	/**
	 * 重写 onTouchEvent 监听方法，用来监听自定义控件是否被触摸
	 *
	 * @param event
	 * @return
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPressPaint.setAlpha(mPressAlpha);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			mPressPaint.setAlpha(0);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:

			break;
		default:
			mPressPaint.setAlpha(0);
			invalidate();
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 这里是复制的赵鹏写的获取Bitmap内容的方法， 之前是因为没有考虑到 Glide 加载的图片 导致drawable 类型是属于
	 * SquaringDrawable 类型，导致强转失败 这里是通过drawable不同的类型来进行获取Bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	private Bitmap getBitmapFromDrawable(Drawable drawable) {
		try {
			Bitmap bitmap;
			if (drawable instanceof BitmapDrawable) {
				return ((BitmapDrawable) drawable).getBitmap();
			} else if (drawable instanceof ColorDrawable) {
				bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
			} else {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
						BITMAP_CONFIG);
			}
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

}
