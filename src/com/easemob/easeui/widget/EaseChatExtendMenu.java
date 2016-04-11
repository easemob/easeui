package com.easemob.easeui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseViewsPagerAdapter;
import com.easemob.util.DensityUtil;

/**
 * 按+按钮出来的扩展按钮
 *
 */
public class EaseChatExtendMenu extends ViewPager{

	protected Context context;
	protected int numColumns = 4;
	protected List<ChatMenuItemModel> itemModels = new ArrayList<ChatMenuItemModel>();
	protected List<View> views = new ArrayList<View>();
	private int widthMeasureSpec;
	private int heightMeasureSpec;

	public EaseChatExtendMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public EaseChatExtendMenu(Context context) {
		this(context,null);
	}
	
	private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseChatExtendMenu);
        numColumns = ta.getInt(R.styleable.EaseChatExtendMenu_numColumns, 4);
        ta.recycle();
    }
	
	/**
     * 初始化
     */
    public void init(){
    	int pageCount = 0;
    	int totalSize = itemModels.size();
    	int itemSize = numColumns * 2;
    	if(totalSize != 0){
    		pageCount = totalSize % itemSize == 0 ?
    				totalSize / itemSize : totalSize / itemSize + 1;
    		if(pageCount == 0) pageCount = 1;
			for(int i = 0; i < pageCount; i++){
	    		GridView gridView = new EaseExpandGridView(context);
	    		gridView.setNumColumns(numColumns);
	    		gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
	    		gridView.setGravity(Gravity.CENTER_VERTICAL);
	    		gridView.setVerticalSpacing(DensityUtil.dip2px(context, 8));
	    		List<ChatMenuItemModel> list = new ArrayList<ChatMenuItemModel>();
	            if(i != pageCount -1){
	                list.addAll(itemModels.subList(i * itemSize, (i+1) * itemSize));
	            }else{
	                list.addAll(itemModels.subList(i * itemSize, totalSize));
	            }
	            gridView.setAdapter(new ItemAdapter(context, list));
	            views.add(gridView);
	    	}
			setAdapter(new EaseViewsPagerAdapter(views));
			measure(widthMeasureSpec, heightMeasureSpec);
    	}
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	this.widthMeasureSpec = widthMeasureSpec;
    	this.heightMeasureSpec = heightMeasureSpec;
    	View child = getChildAt(0);
    	if (child != null) {
    		child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST));
        	int height = child.getMeasuredHeight();
        	heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		}
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
     * 注册menu item
     * 
     * @param name
     *            item名字
     * @param drawableRes
     *            item背景
     * @param itemId
     *             id
     * @param listener
     *            item点击事件
     */
    public void registerMenuItem(String name, int drawableRes, int itemId, EaseChatExtendMenuItemClickListener listener) {
        ChatMenuItemModel item = new ChatMenuItemModel();
        item.name = name;
        item.image = drawableRes;
        item.id = itemId;
        item.clickListener = listener;
        itemModels.add(item);
    }
    
    /**
     * 注册menu item
     * 
     * @param nameRes
     *            item名字的resource id
     * @param drawableRes
     *            item背景
     * @param itemId
     *             id
     * @param listener
     *            item点击事件
     */
    public void registerMenuItem(int nameRes, int drawableRes, int itemId, EaseChatExtendMenuItemClickListener listener) {
        registerMenuItem(context.getString(nameRes), drawableRes, itemId, listener);
    }
    
    
    public interface EaseChatExtendMenuItemClickListener{
        void onClick(int itemId, View view);
    }
    
    class ChatMenuItemModel{
        String name;
        int image;
        int id;
        EaseChatExtendMenuItemClickListener clickListener;
    }
    
    private class ItemAdapter extends ArrayAdapter<ChatMenuItemModel>{

        private Context context;

        public ItemAdapter(Context context, List<ChatMenuItemModel> objects) {
            super(context, 1, objects);
            this.context = context;
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ChatMenuItem menuItem = null;
            if(convertView == null){
                convertView = new ChatMenuItem(context);
            }
            menuItem = (ChatMenuItem) convertView;
            menuItem.setImage(getItem(position).image);
            menuItem.setText(getItem(position).name);
            menuItem.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if(getItem(position).clickListener != null){
                        getItem(position).clickListener.onClick(getItem(position).id, v);
                    }
                }
            });
            return convertView;
        }
        
        
    }
    
    class ChatMenuItem extends LinearLayout {
        private ImageView imageView;
        private TextView textView;

        public ChatMenuItem(Context context, AttributeSet attrs, int defStyle) {
            this(context, attrs);
        }

        public ChatMenuItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }

        public ChatMenuItem(Context context) {
            super(context);
            init(context, null);
        }

        private void init(Context context, AttributeSet attrs) {
            LayoutInflater.from(context).inflate(R.layout.ease_chat_menu_item, this);
            imageView = (ImageView) findViewById(R.id.image);
            textView = (TextView) findViewById(R.id.text);
        }

        public void setImage(int resid) {
            imageView.setBackgroundResource(resid);
        }

        public void setText(int resid) {
            textView.setText(resid);
        }

        public void setText(String text) {
            textView.setText(text);
        }
    }
}
