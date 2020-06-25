package com.hyphenate.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.hyphenate.easeui.R;

import java.lang.reflect.Method;

public class EaseRoundImageView extends AppCompatImageView {
    // default bitmap config
    private Context context;
    private Paint paint;
    private int roundWidth = 20;
    private int roundHeight = 20;
    private Paint paint2;


    public EaseRoundImageView(Context context) {
        this(context, null);
    }

    public EaseRoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseRoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        if(attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EaseRoundImageView);
//            borderColor = array.getColor(R.styleable.EaseRoundImageView_ease_round_border_color, borderColor);
//            borderWidth = array.getDimensionPixelOffset(R.styleable.EaseRoundImageView_ease_round_border_width, borderWidth);
//            pressAlpha = array.getInteger(R.styleable.EaseRoundImageView_ease_round_press_alpha, pressAlpha);
//            pressColor = array.getColor(R.styleable.EaseRoundImageView_ease_round_press_color, pressColor);
//            radius = array.getDimensionPixelOffset(R.styleable.EaseRoundImageView_ease_round_radius, radius);
//            showLeftUpRound = array.getBoolean(R.styleable.EaseRoundImageView_ease_round_show_left_up, true);
//            showLeftDownRound = array.getBoolean(R.styleable.EaseRoundImageView_ease_round_show_left_down, true);
//            showRightUpRound = array.getBoolean(R.styleable.EaseRoundImageView_ease_round_show_right_up, true);
//            showRightDownRound = array.getBoolean(R.styleable.EaseRoundImageView_ease_round_show_right_down, true);
//            shapeType = array.getInteger(R.styleable.EaseRoundImageView_ease_round_shape_type, shapeType);

            roundWidth = array.getDimensionPixelSize(R.styleable.EaseRoundImageView_roundWidth, roundWidth);
            roundHeight = array.getDimensionPixelSize(R.styleable.EaseRoundImageView_roundHeight, roundHeight);
            array.recycle();
        }

        // set paint when pressed
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        paint2 = new Paint();
        paint2.setXfermode(null);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap);
        super.draw(canvas2);
        drawLiftUp(canvas2);
        drawLiftDown(canvas2);
        drawRightUp(canvas2);
        drawRightDown(canvas2);
        canvas.drawBitmap(bitmap, 0, 0, paint2);
        bitmap.recycle();
    }

//    private void drawDrawable(Canvas canvas, Bitmap bitmap) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            // set flags
//            int saveFlags = CanvasLegacy.MATRIX_SAVE_FLAG
//                    | CanvasLegacy.CLIP_SAVE_FLAG
//                    | CanvasLegacy.HAS_ALPHA_LAYER_SAVE_FLAG
//                    | CanvasLegacy.FULL_COLOR_LAYER_SAVE_FLAG
//                    | CanvasLegacy.CLIP_TO_LAYER_SAVE_FLAG;
//            CanvasLegacy.saveLayer(canvas,0, 0, width, height, null, saveFlags);
//        } else {
//            canvas.saveLayer(0, 0, width, height, null);
//        }
//        drawLiftUp(canvas);
//        drawLiftDown(canvas);
//        drawRightUp(canvas);
//        drawRightDown(canvas);
//        canvas.drawBitmap(bitmap, 0, 0, paint2);
//
//
//    }

    private void drawLiftUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, roundHeight);
        path.lineTo(0, 0);
        path.lineTo(roundWidth, 0);
        path.arcTo(new RectF(0, 0, roundWidth * 2, roundHeight * 2), -90, -90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLiftDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, getHeight() - roundHeight);
        path.lineTo(0, getHeight());
        path.lineTo(roundWidth, getHeight());
        path.arcTo(new RectF(0, getHeight() - roundHeight * 2, roundWidth * 2, getHeight()), 90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth() - roundWidth, getHeight());
        path.lineTo(getWidth(), getHeight());
        path.lineTo(getWidth(), getHeight() - roundHeight);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, getHeight() - roundHeight * 2, getWidth(), getHeight()), -0, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth(), roundHeight);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth() - roundWidth, 0);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, 0, getWidth(), 0 + roundHeight * 2), -90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

//    private void drawLeftUp(Canvas canvas) {
//        path.moveTo(0, radius);
//        path.lineTo(0, 0);
//        path.lineTo(radius, 0);
//        path.arcTo(new RectF(0, 0, radius * 2, radius * 2), -90, -90);
//        path.close();
//        canvas.drawPath(path, paint);
//    }
//
//    private void drawLeftDown(Canvas canvas) {
//        path.moveTo(0, height - radius);
//        path.lineTo(0, height);
//        path.lineTo(radius, height);
//        path.arcTo(new RectF(0, height - radius * 2, radius * 2, height), 90, 90);
//        path.close();
//        canvas.drawPath(path, paint);
//    }
//
//    private void drawRightUp(Canvas canvas) {
//        path.moveTo(width, radius);
//        path.lineTo(width, 0);
//        path.lineTo(width - radius, 0);
//        path.arcTo(new RectF(width - radius * 2, 0, width, radius * 2), -90, 90);
//        path.close();
//        canvas.drawPath(path, paint);
//    }
//
//    private void drawRightDown(Canvas canvas) {
//        path.moveTo(width - radius, height);
//        path.lineTo(width, height);
//        path.lineTo(width, height - radius);
//        path.arcTo(new RectF(width - radius * 2, height - radius * 2, width, height), 0, 90);
//        path.close();
//        canvas.drawPath(path, paint);
//    }

//    private Bitmap getBitmapFromDrawable() {
//        Drawable drawable = getDrawable();
//        if (drawable == null) {
//            return null;
//        }
//        // the width and height is in xml file
//        if (getWidth() == 0 || getHeight() == 0) {
//            return null;
//        }
//        if (drawable instanceof BitmapDrawable) {
//            return ((BitmapDrawable) drawable).getBitmap();
//        }
//        Bitmap bitmap;
//        int width = Math.max(drawable.getIntrinsicWidth(), 2);
//        int height = Math.max(drawable.getIntrinsicHeight(), 2);
//        try {
//            bitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
//            Canvas canvas = new Canvas(bitmap);
//            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//            drawable.draw(canvas);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            bitmap = null;
//        }
//        return bitmap;
//    }

    /**
     * Canvas#save(int) has been removed from sdk-28, see detail from:
     * https://issuetracker.google.com/issues/110856542
     * so this helper classes uses reflection to access the API on older devices.
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static class CanvasLegacy {
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

}
