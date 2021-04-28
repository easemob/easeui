package com.hyphenate.easeui.modules.chat;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseChatExtendMenuAdapter;
import com.hyphenate.easeui.adapter.EaseChatExtendMenuIndicatorAdapter;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatExtendMenuItemClickListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatExtendMenu;
import com.hyphenate.easeui.widget.chatextend.HorizontalPageLayoutManager;
import com.hyphenate.easeui.widget.chatextend.PagingScrollHelper;
import com.hyphenate.util.DensityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extend menu when user want send image, voice clip, etc
 *
 */
public class EaseChatExtendMenu extends FrameLayout implements PagingScrollHelper.onPageChangeListener, IChatExtendMenu, OnItemClickListener {
    protected Context context;
    private RecyclerView rvExtendMenu;
    private RecyclerView rvIndicator;
    private List<ChatMenuItemModel> itemModels = new ArrayList<ChatMenuItemModel>();
    private Map<Integer, ChatMenuItemModel> itemMap = new HashMap();
    private EaseChatExtendMenuAdapter adapter;
    private int numColumns;
    private int numRows;
    private int currentPosition;
    private PagingScrollHelper helper;
    private EaseChatExtendMenuIndicatorAdapter indicatorAdapter;
    private EaseChatExtendMenuItemClickListener itemListener;

    private int[] itemStrings = { R.string.attach_take_pic, R.string.attach_picture,
            R.string.attach_location, R.string.attach_video, R.string.attach_file};
    private int[] itemdrawables = { R.drawable.ease_chat_takepic_selector, R.drawable.ease_chat_image_selector,
            R.drawable.ease_chat_location_selector, R.drawable.em_chat_video_selector, R.drawable.em_chat_file_selector};
    private int[] itemIds = { R.id.extend_item_take_picture, R.id.extend_item_picture, R.id.extend_item_location, R.id.extend_item_video, R.id.extend_item_file};

    public EaseChatExtendMenu(Context context) {
        this(context, null);
    }

    public EaseChatExtendMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatExtendMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        initLayout();
    }

    private void init(Context context, AttributeSet attrs){
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseChatExtendMenu);
        numColumns = ta.getInt(R.styleable.EaseChatExtendMenu_numColumns, 4);
        numRows = ta.getInt(R.styleable.EaseChatExtendMenu_numRows, 2);
        ta.recycle();
    }

    private void initLayout() {
        LayoutInflater.from(context).inflate(R.layout.ease_layout_chat_extend_menu, this);
        rvExtendMenu = findViewById(R.id.rv_extend_menu);
        rvIndicator = findViewById(R.id.rv_indicator);
    }

    /**
     * init
     */
    public void init(){
        initChatExtendMenu();
        initChatExtendMenuIndicator();
        addDefaultData();
    }

    private void initChatExtendMenu() {
        HorizontalPageLayoutManager manager = new HorizontalPageLayoutManager(numRows, numColumns);
        manager.setItemHeight(DensityUtil.dip2px(context, 90));
        rvExtendMenu.setLayoutManager(manager);
        rvExtendMenu.setHasFixedSize(true);
        ConcatAdapter concatAdapter = new ConcatAdapter();
        adapter = new EaseChatExtendMenuAdapter();
        concatAdapter.addAdapter(adapter);
        rvExtendMenu.setAdapter(concatAdapter);
        adapter.setData(itemModels);

        helper = new PagingScrollHelper();
        helper.setUpRecycleView(rvExtendMenu);
        helper.updateLayoutManger();
        helper.scrollToPosition(0);
        setHorizontalFadingEdgeEnabled(true);
        helper.setOnPageChangeListener(this);

        adapter.setOnItemClickListener(this);
    }

    private void initChatExtendMenuIndicator() {
        indicatorAdapter = new EaseChatExtendMenuIndicatorAdapter();
        rvIndicator.setAdapter(indicatorAdapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.ease_chat_extend_menu_indicator_divider));
        rvIndicator.addItemDecoration(itemDecoration);
        indicatorAdapter.setSelectedPosition(currentPosition);
    }

    private void addDefaultData() {
        for(int i = 0; i < itemStrings.length; i++) {
            registerMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], null);
        }
    }

    /**
     * 清空数据
     */
    @Override
    public void clear() {
        itemModels.clear();
        itemMap.clear();
        adapter.notifyDataSetChanged();
        indicatorAdapter.setPageCount(0);
    }

    @Override
    public void setMenuOrder(int itemId, int order) {
        if(itemMap.containsKey(itemId)) {
            ChatMenuItemModel model = itemMap.get(itemId);
            if(model != null) {
                model.order = order;
                sortByOrder(itemModels);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * register menu item
     * 
     * @param name
     *            item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param listener
     *            on click event of item
     */
    public void registerMenuItem(String name, int drawableRes, int itemId, EaseChatExtendMenuItemClickListener listener) {
        if(!itemMap.containsKey(itemId)) {
            ChatMenuItemModel item = new ChatMenuItemModel();
            item.name = name;
            item.image = drawableRes;
            item.id = itemId;
            item.clickListener = listener;
            itemMap.put(itemId, item);
            itemModels.add(item);
            adapter.notifyItemInserted(itemModels.size() - 1);
            //设置需要显示的indicator的个数
            indicatorAdapter.setPageCount((int) Math.ceil(itemModels.size() * 1.0f / (numColumns * numRows)));
        }
    }

    /**
     * register menu item
     *
     * @param name
     *            item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param order
     *             order by
     * @param listener
     *            on click event of item
     */
    public void registerMenuItem(String name, int drawableRes, int itemId, int order, EaseChatExtendMenuItemClickListener listener) {
        if(!itemMap.containsKey(itemId)) {
            ChatMenuItemModel item = new ChatMenuItemModel();
            item.name = name;
            item.image = drawableRes;
            item.id = itemId;
            item.order = order;
            item.clickListener = listener;
            itemMap.put(itemId, item);
            itemModels.add(item);
            sortByOrder(itemModels);
            adapter.notifyDataSetChanged();
            //设置需要显示的indicator的个数
            indicatorAdapter.setPageCount((int) Math.ceil(itemModels.size() * 1.0f / (numColumns * numRows)));
        }
    }

    /**
     * register menu item
     * 
     * @param nameRes
     *            resource id of item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param listener
     *             on click event of item
     */
    public void registerMenuItem(int nameRes, int drawableRes, int itemId, EaseChatExtendMenuItemClickListener listener) {
        registerMenuItem(context.getString(nameRes), drawableRes, itemId, listener);
    }

    /**
     * register menu item
     *
     * @param nameRes
     *            resource id of item name
     * @param drawableRes
     *            background of item
     * @param itemId
     *             id
     * @param order
     *             order by
     * @param listener
     *             on click event of item
     */
    public void registerMenuItem(int nameRes, int drawableRes, int itemId, int order, EaseChatExtendMenuItemClickListener listener) {
        registerMenuItem(context.getString(nameRes), drawableRes, itemId, order, listener);
    }

    private void sortByOrder(List<ChatMenuItemModel> itemModels) {
        Collections.sort(itemModels, new Comparator<ChatMenuItemModel>() {
            @Override
            public int compare(ChatMenuItemModel o1, ChatMenuItemModel o2) {
                int val = o1.order - o2.order;
                if(val > 0) {
                    return 1;
                }else if(val == 0) {
                    return 0;
                }else {
                    return -1;
                }
            }
        });
    }

    @Override
    public void onPageChange(int index) {
        currentPosition = index;
        //设置选中的indicator
        indicatorAdapter.setSelectedPosition(index);
    }

    @Override
    public void onItemClick(View view, int position) {
        ChatMenuItemModel itemModel = itemModels.get(position);
        if(itemListener != null) {
            itemListener.onChatExtendMenuItemClick(itemModel.id, view);
        }
    }

    @Override
    public void registerMenuItem(String name, int drawableRes, int itemId) {
        registerMenuItem(name, drawableRes, itemId, null);
    }

    @Override
    public void registerMenuItem(String name, int drawableRes, int itemId, int order) {
        registerMenuItem(name, drawableRes, itemId, order, null);
    }

    @Override
    public void registerMenuItem(int nameRes, int drawableRes, int itemId) {
        registerMenuItem(nameRes, drawableRes, itemId, null);
    }

    @Override
    public void registerMenuItem(int nameRes, int drawableRes, int itemId, int order) {
        registerMenuItem(nameRes, drawableRes, itemId, order, null);
    }

    @Override
    public void setEaseChatExtendMenuItemClickListener(EaseChatExtendMenuItemClickListener listener) {
        this.itemListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(helper != null && rvExtendMenu != null) {
            helper.scrollToPosition(0);
            helper.checkCurrentStatus();
        }
    }

    public static class ChatMenuItemModel{
        /**
         * 条目名称
         */
        public String name;
        /**
         * 条目图标
         */
        public int image;
        /**
         * 条目id
         */
        public int id;
        /**
         * 用作排序
         */
        public int order;
        public EaseChatExtendMenuItemClickListener clickListener;
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
