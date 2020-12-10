package com.hyphenate.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.hyphenate.easeui.R;

import java.lang.reflect.Method;


/**
 * Canvas#save(int) has been removed from sdk-28, see detail from:
 * https://issuetracker.google.com/issues/110856542
 * so this helper classes uses reflection to access the API on older devices.
 */
@SuppressWarnings("JavaReflectionMemberAccess")
class CanvasLegacy {
    static final int MATRIX_SAVE_FLAG;
    static final int CLIP_SAVE_FLAG;
    static final int HAS_ALPHA_LAYER_SAVE_FLAG;
    static final int FULL_COLOR_LAYER_SAVE_FLAG;
    static final int CLIP_TO_LAYER_SAVE_FLAG;


    private static final Method SAVE;

    static {
        try {
            MATRIX_SAVE_FLAG = (int) Canvas.class.getField("MATRIX_SAVE_FLAG").get(null);
            CLIP_SAVE_FLAG = (int) Canvas.class.getField("CLIP_SAVE_FLAG").get(null);
            HAS_ALPHA_LAYER_SAVE_FLAG = (int) Canvas.class.getField("HAS_ALPHA_LAYER_SAVE_FLAG").get(null);
            FULL_COLOR_LAYER_SAVE_FLAG = (int) Canvas.class.getField("FULL_COLOR_LAYER_SAVE_FLAG").get(null);
            CLIP_TO_LAYER_SAVE_FLAG = (int) Canvas.class.getField("CLIP_TO_LAYER_SAVE_FLAG").get(null);

            SAVE = Canvas.class.getMethod("saveLayer", float.class, float.class, float.class, float.class, Paint.class, int.class);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    static void saveLayer(Canvas canvas, float left, float top, float right, float bottom, @Nullable Paint paint, int saveFlags) {
        try {
            SAVE.invoke(canvas, left, top, right, bottom, paint, saveFlags);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    private static RuntimeException sneakyThrow(Throwable t) {
        if (t == null) throw new NullPointerException("t");
        return CanvasLegacy.sneakyThrow0(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }
}


/**
 * Created by lzan13 on 2015/4/30.
 * customized ImageView，Rounded Rectangle and border is implemented, and change color when you press
 */
public class EaseImageView extends AppCompatImageView {
    // paint when user press
    private Paint pressPaint;
    private int width;
    private int height;

    // default bitmap config
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 1;

    // border color
    private int borderColor;
    // width of border
    private int borderWidth;
    // alpha when pressed
    private int pressAlpha;
    // color when pressed
    private int pressColor;
    // radius
    private int radius;
    // rectangle or round, 1 is circle, 2 is rectangle
    private int shapeType;

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

    private void init(Context context, AttributeSet attrs) {
        //init the value
        borderWidth = 0;
        borderColor = 0xddffffff;
        pressAlpha = 0x42;
        pressColor = 0x42000000;
        radius = 16;
        shapeType = 0;

        // get attribute of EaseImageView
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EaseImageView);
            borderColor = array.getColor(R.styleable.EaseImageView_ease_border_color, borderColor);
            borderWidth = array.getDimensionPixelOffset(R.styleable.EaseImageView_ease_border_width, borderWidth);
            pressAlpha = array.getInteger(R.styleable.EaseImageView_ease_press_alpha, pressAlpha);
            pressColor = array.getColor(R.styleable.EaseImageView_ease_press_color, pressColor);
            radius = array.getDimensionPixelOffset(R.styleable.EaseImageView_ease_radius, radius);
            shapeType = array.getInteger(R.styleable.EaseImageView_ease_shape_type, shapeType);
            array.recycle();
        }

        // set paint when pressed
        pressPaint = new Paint();
        pressPaint.setAntiAlias(true);
        pressPaint.setStyle(Paint.Style.FILL);
        pressPaint.setColor(pressColor);
        pressPaint.setAlpha(0);
        pressPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        setDrawingCacheEnabled(true);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (shapeType == 0) {
            super.onDraw(canvas);
            return;
        }
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        // the width and height is in xml file
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        drawDrawable(canvas, bitmap);

        if(isClickable()){
            drawPress(canvas);
        }
        drawBorder(canvas);
    }

    /**
     * draw Rounded Rectangle
     *
     * @param canvas
     * @param bitmap
     */
    private void drawDrawable(Canvas canvas, Bitmap bitmap) {
        Paint paint = new Paint();
        paint.setColor(0xffffffff);
        paint.setAntiAlias(true); //smooths out the edges of what is being drawn
        PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // set flags
            int saveFlags = CanvasLegacy.MATRIX_SAVE_FLAG
                    | CanvasLegacy.CLIP_SAVE_FLAG
                    | CanvasLegacy.HAS_ALPHA_LAYER_SAVE_FLAG
                    | CanvasLegacy.FULL_COLOR_LAYER_SAVE_FLAG
                    | CanvasLegacy.CLIP_TO_LAYER_SAVE_FLAG;
            CanvasLegacy.saveLayer(canvas,0, 0, width, height, null, saveFlags);
        } else {
            canvas.saveLayer(0, 0, width, height, null);
        }

        if (shapeType == 1) {
            canvas.drawCircle(width / 2, height / 2, width / 2 - 1, paint);
        } else if (shapeType == 2) {
            RectF rectf = new RectF(1, 1, getWidth() - 1, getHeight() - 1);
            canvas.drawRoundRect(rectf, radius + 1, radius + 1, paint);
        }

        paint.setXfermode(xfermode);

        float scaleWidth = ((float) getWidth()) / bitmap.getWidth();
        float scaleHeight = ((float) getHeight()) / bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        //bitmap scale
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.restore();
    }

    /**
     * draw the effect when pressed
     *
     * @param canvas
     */
    private void drawPress(Canvas canvas) {
        // check is rectangle or circle
        if (shapeType == 1) {
            canvas.drawCircle(width / 2, height / 2, width / 2 - 1, pressPaint);
        } else if (shapeType == 2) {
            RectF rectF = new RectF(1, 1, width - 1, height - 1);
            canvas.drawRoundRect(rectF, radius + 1, radius + 1, pressPaint);
        }
    }

    /**
     * draw customized border
     *
     * @param canvas
     */
    private void drawBorder(Canvas canvas) {
        if (borderWidth > 0) {
            Paint paint = new Paint();
            paint.setStrokeWidth(borderWidth);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(borderColor);
            paint.setAntiAlias(true);
            // // check is rectangle or circle
            if (shapeType == 1) {
                canvas.drawCircle(width / 2, height / 2, (width - borderWidth) / 2, paint);
            } else if (shapeType == 2) {
                RectF rectf = new RectF(borderWidth / 2, borderWidth / 2, getWidth() - borderWidth / 2,
                        getHeight() - borderWidth / 2);
                canvas.drawRoundRect(rectf, radius, radius, paint);
            }
        }
    }

    /**
     * monitor the size change
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    /**
     * monitor if touched
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressPaint.setAlpha(pressAlpha);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                pressPaint.setAlpha(0);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:

                break;
            default:
                pressPaint.setAlpha(0);
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     *
     * @param drawable
     * @return
     */
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap;
        int width = Math.max(drawable.getIntrinsicWidth(), 2);
        int height = Math.max(drawable.getIntrinsicHeight(), 2);
        try {
            bitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }

    /**
     * set border color
     *
     * @param borderColor
     */
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    /**
     * set border width
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * set alpha when pressed
     *
     * @param pressAlpha
     */
    public void setPressAlpha(int pressAlpha) {
        this.pressAlpha = pressAlpha;
    }

    /**
     * set color when pressed
     *
     * @param pressColor
     */
    public void setPressColor(int pressColor) {
        this.pressColor = pressColor;
    }

    /**
     * set radius
     *
     * @param radius
     */
    public void setRadius(int radius) {
        this.radius = radius;
        invalidate();
    }

    /**
     * set shape,1 is circle, 2 is rectangle
     *
     * @param shapeType
     */
    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
        invalidate();
    }

    /**
     * set shape type
     * @param shapeType
     */
    public void setShapeType(ShapeType shapeType) {
        if(shapeType == null) {
            return;
        }
        this.shapeType = shapeType.ordinal();
        invalidate();
    }

    /**
     * 图片形状
     */
    public enum ShapeType {
        NONE, ROUND, RECTANGLE
    }
}
