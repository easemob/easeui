package com.easemob.easeui.widget;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseExpressionAdapter;
import com.easemob.easeui.adapter.EaseExpressionPagerAdapter;
import com.easemob.easeui.utils.EaseSmileUtils;

/**
 * 表情图片控件
 */
public class EaseEmojiconMenu extends LinearLayout{
	
	private float emojiconSize;
	private List<String> reslist;
	private Context context;
	private ViewPager expressionViewpager;
	private EmojiconListener listener;
	
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
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EMEmojiconMenu);
		ta.recycle();
		// 表情list
		reslist = getExpressionRes(EaseSmileUtils.simleSize);
		// 初始化表情viewpager
		List<View> views = new ArrayList<View>();
		View gv1 = getGridChildView(1);
		View gv2 = getGridChildView(2);
		views.add(gv1);
		views.add(gv2);
		expressionViewpager = (ViewPager) findViewById(R.id.vPager);
		expressionViewpager.setAdapter(new EaseExpressionPagerAdapter(views));
	}
	
	public void setEmojiconListener(EmojiconListener listener){
		this.listener = listener;
	}
	
	/**
	 * 获取表情的gridview的子view
	 * 
	 * @param i
	 * @return
	 */
	private View getGridChildView(int i) {
		View view = View.inflate(context, R.layout.ease_expression_gridview, null);
		EaseExpandGridView gv = (EaseExpandGridView) view.findViewById(R.id.gridview);
		List<String> list = new ArrayList<String>();
		if (i == 1) {
			List<String> list1 = reslist.subList(0, 20);
			list.addAll(list1);
		} else if (i == 2) {
			list.addAll(reslist.subList(20, reslist.size()));
		}
		list.add("delete_expression");
		final EaseExpressionAdapter expressionAdapter = new EaseExpressionAdapter(context, 1, list);
		gv.setAdapter(expressionAdapter);
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String filename = expressionAdapter.getItem(position);
				if(listener != null){
					if (filename != "delete_expression"){
                        try {
                            // 这里用的反射，所以混淆的时候不要混淆SmileUtils这个类
                            Class clz = Class.forName("com.easemob.easeui.utils.EaseSmileUtils");
                            Field field = clz.getField(filename);
                            CharSequence cs = EaseSmileUtils.getSmiledText(context,(String) field.get(null));
                            listener.onExpressionClicked(cs);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
					}else{
						listener.onDeleteImageClicked();
					}
				}

			}
		});
		return view;
	}
	
	private List<String> getExpressionRes(int getSum) {
		List<String> reslist = new ArrayList<String>();
		for (int x = 1; x <= getSum; x++) {
			String filename = "ee_" + x;

			reslist.add(filename);

		}
		return reslist;

	}
	
	public interface EmojiconListener{
		/**
		 * 表情被点击
		 * @param emojiContent
		 */
		void onExpressionClicked(CharSequence emojiContent);
		/**
		 * 删除按钮被点击
		 */
		void onDeleteImageClicked();
	}
}
