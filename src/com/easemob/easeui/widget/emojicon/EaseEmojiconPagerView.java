package com.easemob.easeui.widget.emojicon;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.easemob.easeui.R;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.domain.EaseEmojiconGroupEntity;
import com.easemob.easeui.utils.EaseSmileUtils;
import com.easemob.easeui.widget.EaseExpandGridView;

public class EaseEmojiconPagerView extends ViewPager{

    private Context context;
    private List<EaseEmojiconGroupEntity> groupEntities = new ArrayList<EaseEmojiconGroupEntity>();
    private List<EaseEmojicon> emojiconList = new ArrayList<EaseEmojicon>();
    //总共多少个pager
    private List<View> pagerList = new ArrayList<View>();
    
    private PagerAdapter pagerAdapter;
    
    private int emojiconRows;
    private int emojiconColumns;
    
    private int maxPagerCount;
    private int previousPagerPosition;
	private EaseEmojiconPagerViewListener pagerViewListener; 

    public EaseEmojiconPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public EaseEmojiconPagerView(Context context) {
        this(context, null);
    }
    
    public void init(List<EaseEmojiconGroupEntity> groupList){
        if(groupList != null){
            this.groupEntities = groupList;
        }
        
        List<View> pages = new ArrayList<View>();
        for(EaseEmojiconGroupEntity group : groupEntities){
            List<EaseEmojicon> groupEmojicons = group.getEmojiconList();
            emojiconList.addAll(groupEmojicons);
            List<View> gridViews = getGridChildViews(groupEmojicons);
            maxPagerCount = Math.max(gridViews.size(), maxPagerCount);
            pages.addAll(gridViews);
        }
        
        pagerAdapter = new EmojiconPagerAdapter(pages);
        setAdapter(pagerAdapter);
        
        setOnPageChangeListener(new EmojiPagerChangeListener());
        
    }
    
    public void setPagerViewListener(EaseEmojiconPagerViewListener pagerViewListener){
    	this.pagerViewListener = pagerViewListener;
    }
    
    /**
     * 设置当前位置
     * @param position
     */
    public void setPagerPostion(int position){
    	if (getAdapter() != null && position >= 0 && position < groupEntities.size()) {
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += getPagerSize(groupEntities.get(0).getEmojiconList());
            }
            setCurrentItem(count);
        }
    }
    
    /**
     * 获取表情的gridviews的的集合
     * @return
     */
    private List<View> getGridChildViews(List<EaseEmojicon> emojiconList){
        int itemSize = emojiconColumns * emojiconRows -1;
        int totalSize = emojiconList.size();
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
//                    if(listener != null){
//                        String emojiText = emojicon.getEmojiText();
//                        if(emojiText != null && emojiText.equals(EaseSmileUtils.DELETE_KEY)){
//                            listener.onDeleteImageClicked();
//                        }else{
//                            listener.onExpressionClicked(emojicon);
//                        }
//                        
//                    }
                    
                }
            });
            
            views.add(view);
        }
        return views;
    }

    /**
     * 获取pager数量
     * @param emojiconList
     * @return
     */
    private int getPagerSize(List<EaseEmojicon> emojiconList) {
    	int itemSize = emojiconColumns * emojiconRows -1;
        int totalSize = emojiconList.size();
        int pageSize = totalSize % itemSize == 0 ? totalSize/itemSize : totalSize/itemSize + 1;
        return pageSize;
    }
    
    private class EmojiPagerChangeListener implements OnPageChangeListener{
        @Override
        public void onPageSelected(int position) {
        	int endSize = 0;
        	int groupPosition = 0;
            for(EaseEmojiconGroupEntity groupEntity : groupEntities){
            	int groupPagerSize = getPagerSize(groupEntity.getEmojiconList());
            	//选中的position在当前遍历的group里
            	if(endSize + groupPagerSize > position){
            		//前面的group切换过来的
            		if(previousPagerPosition - endSize < 0){
            			if(pagerViewListener != null){
            				pagerViewListener.onGroupPositionChanged(groupPosition, groupPagerSize);
            				pagerViewListener.onGroupIndicatorChangedTo(0);
            			}
            			break;
            		}
            		//后面的group切换过来的
            		if(previousPagerPosition - endSize >= groupPagerSize){
            			if(pagerViewListener != null){
            				pagerViewListener.onGroupPositionChanged(groupPosition, groupPagerSize);
            				pagerViewListener.onGroupIndicatorChangedTo(position - endSize);
            			}
            			break;
            		}
            		
            		//当前group的pager切换
            		if(pagerViewListener != null){
            			pagerViewListener.onGroupIndicatorChanged(previousPagerPosition-endSize, position-endSize);
            		}
            		break;
            		
            	}
            	groupPosition++;
            	endSize += groupPagerSize;
            }
            
            previousPagerPosition = position;
        }
        
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
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
            
//          String filename = getItem(position);
//          int resId = getContext().getResources().getIdentifier(filename, "drawable", getContext().getPackageName());
//          imageView.setImageResource(resId);
            
            return convertView;
        }
        
    }
    
    private class EmojiconPagerAdapter extends PagerAdapter{

        private List<View> views;

        public EmojiconPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(views.get(arg1));
            return views.get(arg1);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(views.get(arg1));

        }
        
    }
    
    public interface EaseEmojiconPagerViewListener{
    	/**
    	 * 表情组位置变动
    	 * @param groupPosition 表情组位置
    	 * @param pagerSizeOfGroup 表情组里的pager的size
    	 * @return
    	 */
    	int onGroupPositionChanged(int groupPosition, int pagerSizeOfGroup);
    	/**
    	 * 表情组内的指示器位置变动
    	 * @param oldPosition
    	 * @param newPosition
    	 * @return
    	 */
    	int onGroupIndicatorChanged(int oldPosition, int newPosition);
    	
    	int onGroupIndicatorChangedTo(int position);
    	
    	
    }
}
