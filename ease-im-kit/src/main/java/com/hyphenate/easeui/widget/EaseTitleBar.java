package com.hyphenate.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
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
    private int mTitleTextColor;
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

            int titlePosition = ta.getInteger(R.styleable.EaseTitleBar_titleBarTitlePosition, 0);
            setTitlePosition(titlePosition);

            float titleTextSize = ta.getDimension(R.styleable.EaseTitleBar_titleBarTitleTextSize, (int) sp2px(getContext(), 18));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);

            int titleTextColor = ta.getResourceId(R.styleable.EaseTitleBar_titleBarTitleTextColor, -1);
            if(titleTextColor != -1) {
                mTitleTextColor = ContextCompat.getColor(getContext(), titleTextColor);
            }else {
                mTitleTextColor = ta.getColor(R.styleable.EaseTitleBar_titleBarTitleTextColor, ContextCompat.getColor(getContext(), R.color.em_toolbar_color_title));
            }
            titleView.setTextColor(mTitleTextColor);

            ta.recycle();
        }
    }

    private void setTitlePosition(int titlePosition) {
        ViewGroup.LayoutParams params = titleView.getLayoutParams();
        if(params instanceof RelativeLayout.LayoutParams) {
            if(titlePosition == 0) { //居中
                ((LayoutParams) params).addRule(RelativeLayout.CENTER_IN_PARENT);
            }else if(titlePosition == 1) { //居左
                ((LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                ((LayoutParams) params).addRule(RelativeLayout.CENTER_VERTICAL);
                ((LayoutParams) params).addRule(RelativeLayout.RIGHT_OF, leftLayout.getId());
            }else { //居右
                ((LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                ((LayoutParams) params).addRule(RelativeLayout.CENTER_VERTICAL);
                ((LayoutParams) params).addRule(LEFT_OF, rightLayout.getId());
                ((LayoutParams) params).setMargins(0, 0, (int) dip2px(getContext(), 60), 0);
            }
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

    /**
     * 设置标题位置
     * @param position
     */
    public void setTitlePosition(TitlePosition position) {
        int pos;
        if(position == TitlePosition.Center) {
            pos = 0;
        }else if(position == TitlePosition.Left) {
            pos = 1;
        }else {
            pos = 2;
        }
        setTitlePosition(pos);
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

    public TextView getTitle() {
        return titleView;
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

    public Toolbar getToolbar() {
        return toolbar;
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

    /**
     * 标题位置
     */
    public enum TitlePosition {
        Center, Left, Right
    }

    /**
     * dip to px
     * @param context
     * @param value
     * @return
     */
    public static float dip2px(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    /**
     * sp to px
     * @param context
     * @param value
     * @return
     */
    public static float sp2px(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics());
    }
}
