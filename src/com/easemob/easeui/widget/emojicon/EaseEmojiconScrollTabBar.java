package com.easemob.easeui.widget.emojicon;

import java.util.ArrayList;
import java.util.List;

import com.easemob.easeui.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class EaseEmojiconScrollTabBar extends RelativeLayout{

    private Context context;
    private HorizontalScrollView scrollView;
    private LinearLayout tabContainer;
    
    private List<ImageView> tabList = new ArrayList<ImageView>();
    private EaseScrollTabBarItemClickListener itemClickListener;

    public EaseEmojiconScrollTabBar(Context context) {
        this(context, null);
    }

    public EaseEmojiconScrollTabBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseEmojiconScrollTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_emojicon_tab_bar, this);
        
        scrollView = (HorizontalScrollView) findViewById(R.id.scroll_view);
        tabContainer = (LinearLayout) findViewById(R.id.tab_container);
    }
    
    
    public void setTabBarItemClickListener(EaseScrollTabBarItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
    
    
    public interface EaseScrollTabBarItemClickListener{
        void onItemClick(int position);
    }
    

}
