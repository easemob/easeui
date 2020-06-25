package com.hyphenate.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.hyphenate.easeui.R;


/**
 * title bar
 *
 */
public class EaseTitleBar extends RelativeLayout implements View.OnClickListener {
    private Context context;
    private Toolbar toolbar;
    protected RelativeLayout leftLayout;
    protected ImageView leftImage;
    protected RelativeLayout rightLayout;
    protected ImageView rightImage;
    protected TextView titleView;
    protected RelativeLayout titleLayout;
    private TextView titleMenu;
    private OnBackPressListener mBackPressListener;
    private OnRightClickListener mOnRightClickListener;
    private int mArrowColorId;
    private int mArrowColor;
    private int mWidth;
    private int mHeight;
    private boolean mDisplayHomeAsUpEnabled;

    public EaseTitleBar(Context context) {
        this(context, null);
    }

    public EaseTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseTitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        initLayout();
    }

    private void initLayout() {
        ViewGroup.LayoutParams params = titleLayout.getLayoutParams();
        params.height = mHeight;
        params.width = mWidth;
        requestLayout();
    }

    private void init(Context context, AttributeSet attrs){
        LayoutInflater.from(context).inflate(R.layout.ease_widget_title_bar, this);
        toolbar = findViewById(R.id.toolbar);
        leftLayout = (RelativeLayout) findViewById(R.id.left_layout);
        leftImage = (ImageView) findViewById(R.id.left_image);
        rightLayout = (RelativeLayout) findViewById(R.id.right_layout);
        rightImage = (ImageView) findViewById(R.id.right_image);
        titleView = (TextView) findViewById(R.id.title);
        titleLayout = (RelativeLayout) findViewById(R.id.root);
        titleMenu = findViewById(R.id.right_menu);
        parseStyle(context, attrs);

        initToolbar();
    }

    private void parseStyle(Context context, AttributeSet attrs){
        if(attrs != null){
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseTitleBar);
            int titleId = ta.getResourceId(R.styleable.EaseTitleBar_titleBarTitle, -1);
            if(titleId != -1) {
                titleView.setText(titleId);
            }else {
                String title = ta.getString(R.styleable.EaseTitleBar_titleBarTitle);
                titleView.setText(title);
            }

            Drawable leftDrawable = ta.getDrawable(R.styleable.EaseTitleBar_titleBarLeftImage);
            if (null != leftDrawable) {
                leftImage.setImageDrawable(leftDrawable);
            }
            Drawable rightDrawable = ta.getDrawable(R.styleable.EaseTitleBar_titleBarRightImage);
            if (null != rightDrawable) {
                rightImage.setImageDrawable(rightDrawable);
            }

            int bgId = ta.getResourceId(R.styleable.EaseTitleBar_titleBarBackground, -1);
            if(bgId != -1) {
                titleLayout.setBackgroundResource(bgId);
            }else {
                int color = ta.getColor(R.styleable.EaseTitleBar_titleBarBackground, Color.TRANSPARENT);
                titleLayout.setBackgroundColor(color);
            }

            mArrowColorId = ta.getResourceId(R.styleable.EaseTitleBar_titleBarArrowColor, -1);
            mArrowColor = ta.getColor(R.styleable.EaseTitleBar_titleBarArrowColor, Color.BLACK);

            Drawable menuDrawable = ta.getDrawable(R.styleable.EaseTitleBar_titleBarMenuResource);
            if(menuDrawable != null) {
                toolbar.setOverflowIcon(menuDrawable);
            }

            int rightTitleId = ta.getResourceId(R.styleable.EaseTitleBar_titleBarRightTitle, -1);
            if(rightTitleId != -1) {
                titleMenu.setText(rightTitleId);
            }else {
                String rightTitle = ta.getString(R.styleable.EaseTitleBar_titleBarRightTitle);
                titleMenu.setText(rightTitle);
            }

            boolean rightVisible = ta.getBoolean(R.styleable.EaseTitleBar_titleBarRightVisible, false);
            rightLayout.setVisibility(rightVisible ? VISIBLE : GONE);

            mDisplayHomeAsUpEnabled = ta.getBoolean(R.styleable.EaseTitleBar_titleBarDisplayHomeAsUpEnabled, true);

            ta.recycle();
        }
    }

    private void initToolbar() {
        rightLayout.setOnClickListener(this);
        if(leftImage.getDrawable() != null) {
            leftImage.setVisibility(mDisplayHomeAsUpEnabled ? VISIBLE : GONE);
            leftLayout.setOnClickListener(this);
        }else {
            if(getContext() instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) getContext();
                activity.setSupportActionBar(toolbar);
                if(activity.getSupportActionBar() != null) {
                    // 显示返回按钮
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
                    // 不显示标题
                    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
                toolbar.setNavigationOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mBackPressListener != null) {
                            mBackPressListener.onBackPress(v);
                        }
                    }
                });
                if(mArrowColorId != -1) {
                    setToolbarCustomColor(mArrowColorId);
                }else {
                    setToolbarCustomColorDefault(mArrowColor);
                }
            }
        }
    }

    public void setToolbarCustomColor(@ColorRes int colorId) {
        setToolbarCustomColorDefault(ContextCompat.getColor(getContext(), colorId));
    }

    public void setToolbarCustomColorDefault(@ColorInt int colorId) {
        Drawable leftArrow = ContextCompat.getDrawable(getContext(), R.drawable.abc_ic_ab_back_material);
        if(leftArrow != null) {
            leftArrow.setColorFilter(colorId, PorterDuff.Mode.SRC_ATOP);
            if(getContext() instanceof AppCompatActivity) {
                if(((AppCompatActivity)getContext()).getSupportActionBar() != null) {
                    ((AppCompatActivity)getContext()).getSupportActionBar().setHomeAsUpIndicator(leftArrow);
                }
            }
        }
    }

    public void setLeftImageResource(int resId) {
        leftImage.setImageResource(resId);
    }
    
    public void setRightImageResource(int resId) {
        rightImage.setImageResource(resId);
        rightLayout.setVisibility(VISIBLE);
    }

    public void setRightTitleResource(@StringRes int title) {
        titleMenu.setText(getResources().getString(title));
        rightLayout.setVisibility(VISIBLE);
    }

    public void setRightTitle(String title) {
        if(!TextUtils.isEmpty(title)) {
            titleMenu.setText(title);
            rightLayout.setVisibility(VISIBLE);
        }
    }
    
    public void setLeftLayoutClickListener(OnClickListener listener){
        leftLayout.setOnClickListener(listener);
    }
    
    public void setRightLayoutClickListener(OnClickListener listener){
        rightLayout.setOnClickListener(listener);
    }
    
    public void setLeftLayoutVisibility(int visibility){
        leftLayout.setVisibility(visibility);
    }
    
    public void setRightLayoutVisibility(int visibility){
        rightLayout.setVisibility(visibility);
    }
    
    public void setTitle(String title){
        titleView.setText(title);
    }
    
    public void setBackgroundColor(int color){
        titleLayout.setBackgroundColor(color);
    }
    
    public RelativeLayout getLeftLayout(){
        return leftLayout;
    }
    
    public RelativeLayout getRightLayout(){
        return rightLayout;
    }

    public ImageView getRightImage() {
        return rightImage;
    }

    public TextView getRightText() {
        return titleMenu;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.left_layout) {
            if(mBackPressListener != null) {
                mBackPressListener.onBackPress(v);
            }
        }else if(v.getId() == R.id.right_layout) {
            if(mOnRightClickListener != null) {
                mOnRightClickListener.onRightClick(v);
            }
        }
    }

    /**
     * 设置返回按钮的点击事件
     * @param listener
     */
    public void setOnBackPressListener(OnBackPressListener listener) {
        this.mBackPressListener = listener;
    }

    /**
     * 设置右侧更多的点击事件
     * @param listener
     */
    public void setOnRightClickListener(OnRightClickListener listener) {
        this.mOnRightClickListener = listener;
    }

    /**
     * 点击返回按钮的监听
     */
    public interface OnBackPressListener {
        void onBackPress(View view);
    }

    /**
     * 设置右侧的点击事件
     */
    public interface OnRightClickListener {
        void onRightClick(View view);
    }
}
