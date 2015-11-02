package com.easemob.easeui.widget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseExpressionPagerAdapter;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.model.EaseDefaultEmojiconDatas;
import com.easemob.easeui.utils.EaseSmileUtils;

/**
 * 表情图片控件
 */
public class EaseEmojiconMenu extends EaseEmojiconMenuBase{
	
	private float emojiconSize;
//	private List<String> reslist;
	private List<EaseEmojicon> emojiconList;
	private Context context;
	private ViewPager expressionViewpager;
	
	private int emojiconRows;
	private int emojiconColumns;
	private final int defaultRows = 3;
	private final int defaultColumns = 7;
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public EaseEmojiconMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public EaseEmojiconMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public EaseEmojiconMenu(Context context) {
		super(context);
		init(context, null);
	}
	
	private void init(Context context, AttributeSet attrs){
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.ease_widget_emojicon, this);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseEmojiconMenu);
		emojiconColumns = ta.getInt(R.styleable.EaseEmojiconMenu_emojiconColumns, defaultColumns);
		emojiconRows = ta.getInt(R.styleable.EaseEmojiconMenu_emojiconRows, defaultRows);
		ta.recycle();
		// 表情list
//		reslist = getExpressionRes(EaseSmileUtils.getSmilesSize());
		emojiconList = Arrays.asList(EaseDefaultEmojiconDatas.getData());
		
		// 初始化表情viewpager
		List<View> views = getGridChildViews();
		expressionViewpager = (ViewPager) findViewById(R.id.vPager);
		expressionViewpager.setAdapter(new EaseExpressionPagerAdapter(views));
	}
	
	
	/**
	 * 获取表情的gridview的子views
	 * @return
	 */
	private List<View> getGridChildViews(){
	    int itemSize = emojiconColumns * emojiconRows -1;
	    int totalSize = EaseSmileUtils.getSmilesSize();
	    int pageSize = totalSize % itemSize == 0 ? totalSize/itemSize : totalSize/itemSize + 1;
	    List<View> views = new ArrayList<View>();
	    for(int i = 0; i < pageSize; i++){
	        View view = View.inflate(context, R.layout.ease_expression_gridview, null);
	        EaseExpandGridView gv = (EaseExpandGridView) view.findViewById(R.id.gridview);
	        gv.setNumColumns(emojiconColumns);
	        List<EaseEmojicon> list = new ArrayList<EaseEmojicon>();
	        if(i != pageSize -1){
	            list.addAll(emojiconList.subList(i * itemSize, (i+1) * itemSize));
	        }else{
	            list.addAll(emojiconList.subList(i * itemSize, totalSize));
	        }
	        EaseEmojicon deleteIcon = new EaseEmojicon();
	        deleteIcon.setEmojiText(EaseSmileUtils.DELETE_KEY);
	        list.add(deleteIcon);
	        final EmojiconGridAdapter gridAdapter = new EmojiconGridAdapter(context, 1, list);
	        gv.setAdapter(gridAdapter);
	        gv.setOnItemClickListener(new OnItemClickListener() {

	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                EaseEmojicon emojicon = gridAdapter.getItem(position);
	                if(listener != null){
	                    String emojiText = emojicon.getEmojiText();
	                    if(emojiText != null && emojiText.equals(EaseSmileUtils.DELETE_KEY)){
	                        listener.onDeleteImageClicked();
	                    }else{
	                        listener.onExpressionClicked(emojicon);
	                    }
	                    
	                }
	                
	            }
	        });
	        
	        views.add(view);
	    }
	    return views;
	}
	
	
	private List<String> getExpressionRes(int getSum) {
		List<String> reslist = new ArrayList<String>();
		for (int x = 1; x <= getSum; x++) {
			String filename = "ee_" + x;

			reslist.add(filename);

		}
		return reslist;

	}
	
	private class EmojiconGridAdapter extends ArrayAdapter<EaseEmojicon>{

	    public EmojiconGridAdapter(Context context, int textViewResourceId, List<EaseEmojicon> objects) {
	        super(context, textViewResourceId, objects);
	    }
	    
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        if(convertView == null){
	            convertView = View.inflate(getContext(), R.layout.ease_row_expression, null);
	        }
	        
	        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
	        EaseEmojicon emojicon = getItem(position);
	        if(EaseSmileUtils.DELETE_KEY.equals(emojicon.getEmojiText())){
	            imageView.setImageResource(R.drawable.ease_delete_expression);
	        }else{
	            if(emojicon.getIcon() != 0){
	                imageView.setImageResource(emojicon.getIcon());
	            }
	        }
	        
//	        String filename = getItem(position);
//	        int resId = getContext().getResources().getIdentifier(filename, "drawable", getContext().getPackageName());
//	        imageView.setImageResource(resId);
	        
	        return convertView;
	    }
	    
	}
	
}
