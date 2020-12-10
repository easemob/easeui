package com.hyphenate.easeui.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.hyphenate.easeui.R;
import com.hyphenate.util.EMLog;

import androidx.core.content.ContextCompat;



public class EaseEditTextUtils {

    public static void changePwdDrawableRight(EditText editText, Drawable eyeOpen , Drawable eyeClose, Drawable left, Drawable top, Drawable bottom) {
        //标识密码是否能被看见
        final boolean[] canBeSeen = {false};
        editText.setOnTouchListener((v, event) -> {

            Drawable drawable = editText.getCompoundDrawables()[2];
            //如果右边没有图片，不再处理
            if (drawable == null)
                return false;
            //如果不是按下事件，不再处理
            if (event.getAction() != MotionEvent.ACTION_UP)
                return false;
            if (event.getX() > editText.getWidth()
                    - editText.getPaddingRight()
                    - drawable.getIntrinsicWidth())
            {

                if (canBeSeen[0])
                {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(left, top, eyeOpen, bottom);
                    canBeSeen[0] = false;
                } else
                {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);

                    editText.setCompoundDrawablesWithIntrinsicBounds(left, top, eyeClose, bottom);
                    canBeSeen[0] = true;
                }
                editText.setSelection(editText.getText().toString().length());

                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();

                return true;
            }
            return false;
        });

    }

    public static void showRightDrawable(EditText editText, Drawable right) {
        String content = editText.getText().toString().trim();
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, TextUtils.isEmpty(content) ? null : right, null);
    }

    public static void clearEditTextListener(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            Drawable drawable = editText.getCompoundDrawables()[2];
            //如果右边没有图片，不再处理
            if (drawable == null)
                return false;
            //如果不是按下事件，不再处理
            if (event.getAction() != MotionEvent.ACTION_UP)
                return false;
            if (event.getX() > editText.getWidth()
                    - editText.getPaddingRight()
                    - drawable.getIntrinsicWidth()) {
                editText.setText("");
                return true;
            }
            return false;
        });
    }

    /**
     * 单行，根据关键字确定省略号的不同位置
     * @param textView
     * @param str
     * @param keyword
     * @param width
     * @return
     */
    public static String ellipsizeString(TextView textView, String str, String keyword, int width) {
        if(TextUtils.isEmpty(keyword)) {
            return str;
        }
        Paint paint = textView.getPaint();
        if(paint.measureText(str) < width) {
            return str;
        }
        int count = paint.breakText(str, 0, str.length(), true, width, null);
        int index = str.indexOf(keyword);
        //如果关键字在第一行,末尾显示省略号
        if(index + keyword.length() < count) {
            return str;
        }
        //如果关键字在最后，则起始位置显示省略号
        if(str.length() - index <= count - 3) {
            String end = str.substring(str.length() - count);
            end = "..." + end.substring(3);
            return end;
        }
        //如果是在中部的话，首尾显示省略号
        int subCount = (count - keyword.length()) / 2;
        String middle = str.substring(index - subCount, index + keyword.length() + subCount);
        middle = "..." + middle.substring(3);
        middle = middle.substring(0, middle.length() - 3) + "...";
        return middle;
    }

    public static SpannableStringBuilder highLightKeyword(Context context, String str, String keyword) {
        if(TextUtils.isEmpty(str) || TextUtils.isEmpty(keyword) || !str.contains(keyword)) {
            return null;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.em_color_brand)), str.indexOf(keyword), str.indexOf(keyword) + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    /**
     * 设置最多显示行数及保留末尾的文本类型
     * @param textView
     * @param str
     * @param num
     * @param width
     * @return
     */
    public static String ellipsizeMiddleString(TextView textView, String str, int num, int width) {
        //设置为最大显示行数及为中间省略
        textView.setMaxLines(num);
        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        TextPaint paint = textView.getPaint();
        if(TextUtils.isEmpty(str) || width <= 0 || paint.measureText(str) < width) {
            return str;
        }
        //检查是否需要进行省略
        int startIndex = 0;
        int maxNum = 0;
        for(int i = 0; i < num; i++) {
            if(startIndex < str.length()) {
                maxNum += paint.breakText(str, startIndex, str.length(), true, width, null);
                startIndex = maxNum - 1;
            }
        }
        if(str.length() < maxNum) {
            return str;
        }
        //获取第num行占据的字符数目
        int maxCount = 0;
        try {
            maxCount = textView.getLayout().getLineEnd(num - 1);
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
        //如果不满num行
        if(str.length() < maxCount) {
            return str;
        }
        //如果文件
        if(str.contains(".")) {
            int lastIndex = str.lastIndexOf(".");
            String suffix = "..." + str.substring(lastIndex - 5);
            float requestWidth = paint.measureText(suffix);
            //对str取反
            String reverse = new StringBuilder(str.substring(0, maxCount)).reverse().toString();
            int takeUpCount = paint.breakText(reverse, 0, reverse.length(), true, requestWidth, null);
            takeUpCount = getTakeUpCount(paint, reverse, takeUpCount, requestWidth);
            str = str.substring(0, maxCount - takeUpCount) + suffix;
            EMLog.i("EaseEditTextUtils", "last str = "+str);
        }
        return str;
    }

    /**
     * 检查保证可以展示文件类型
     * @param paint
     * @param reverse
     * @param takeUpCount
     * @param requestWidth
     * @return
     */
    private static int getTakeUpCount(Paint paint, String reverse, int takeUpCount, float requestWidth) {
        float measureWidth = paint.measureText(reverse.substring(0, takeUpCount));
        if(measureWidth <= requestWidth) {
            return getTakeUpCount(paint, reverse, takeUpCount + 1, requestWidth);
        }
        return takeUpCount + 1;
    }
}
