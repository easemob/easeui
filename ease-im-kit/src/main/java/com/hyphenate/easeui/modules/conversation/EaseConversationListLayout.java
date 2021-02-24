package com.hyphenate.easeui.modules.conversation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseAdapterDelegate;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.interfaces.OnItemLongClickListener;
import com.hyphenate.easeui.modules.conversation.adapter.EaseConversationListAdapter;
import com.hyphenate.easeui.modules.conversation.delegate.EaseSystemMsgDelegate;
import com.hyphenate.easeui.modules.conversation.interfaces.OnConversationChangeListener;
import com.hyphenate.easeui.modules.conversation.interfaces.OnConversationLoadListener;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.modules.EaseBaseLayout;
import com.hyphenate.easeui.modules.conversation.delegate.EaseBaseConversationDelegate;
import com.hyphenate.easeui.modules.conversation.delegate.EaseConversationDelegate;
import com.hyphenate.easeui.modules.conversation.interfaces.IConversationListLayout;
import com.hyphenate.easeui.modules.conversation.interfaces.IConversationStyle;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationSetStyle;
import com.hyphenate.easeui.modules.conversation.presenter.EaseConversationPresenter;
import com.hyphenate.easeui.modules.conversation.presenter.EaseConversationPresenterImpl;
import com.hyphenate.easeui.modules.conversation.presenter.IEaseConversationListView;
import com.hyphenate.easeui.modules.interfaces.IPopupMenu;
import com.hyphenate.easeui.modules.menu.OnPopupMenuDismissListener;
import com.hyphenate.easeui.modules.menu.OnPopupMenuItemClickListener;
import com.hyphenate.easeui.modules.menu.EasePopupMenuHelper;
import com.hyphenate.easeui.modules.menu.OnPopupMenuPreShowListener;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.easeui.widget.EaseRecyclerView;

import java.util.ArrayList;
import java.util.List;


/**
 * 会话列表
 */
public class EaseConversationListLayout extends EaseBaseLayout implements IConversationListLayout, IConversationStyle
                                                                        , IEaseConversationListView, IPopupMenu {
    private EaseRecyclerView rvConversationList;

    private ConcatAdapter adapter;
    private EaseConversationListAdapter listAdapter;
    private OnItemClickListener itemListener;
    private OnItemLongClickListener itemLongListener;
    private OnPopupMenuItemClickListener popupMenuItemClickListener;
    private OnPopupMenuDismissListener dismissListener;
    private OnPopupMenuPreShowListener menuPreShowListener;
    private EaseConversationSetStyle setModel;

    private EaseConversationPresenter presenter;
    private float touchX;
    private float touchY;
    private EasePopupMenuHelper menuHelper;
    private boolean showDefaultMenu = true;
    private OnConversationChangeListener conversationChangeListener;
    private OnConversationLoadListener loadListener;

    public EaseConversationListLayout(Context context) {
        this(context, null);
    }

    public EaseConversationListLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseConversationListLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setModel = new EaseConversationSetStyle();
        LayoutInflater.from(context).inflate(R.layout.ease_conversation_list, this);
        presenter = new EaseConversationPresenterImpl();
        if(context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(presenter);
        }
        initAttrs(context, attrs);
        initViews();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EaseConversationListLayout);
            float titleTextSize = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_title_text_size
                    , sp2px(context, 16));
            setModel.setTitleTextSize(titleTextSize);
            int titleTextColorRes = a.getResourceId(R.styleable.EaseConversationListLayout_ease_con_item_title_text_color, -1);
            int titleTextColor;
            if(titleTextColorRes != -1) {
                titleTextColor = ContextCompat.getColor(context, titleTextColorRes);
            }else {
                titleTextColor = a.getColor(R.styleable.EaseConversationListLayout_ease_con_item_title_text_color
                        , ContextCompat.getColor(context, R.color.ease_conversation_color_item_name));
            }
            setModel.setTitleTextColor(titleTextColor);

            float contentTextSize = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_content_text_size
                    , sp2px(context, 14));
            setModel.setContentTextSize(contentTextSize);
            int contentTextColorRes = a.getResourceId(R.styleable.EaseConversationListLayout_ease_con_item_content_text_color, -1);
            int contentTextColor;
            if(contentTextColorRes != -1) {
                contentTextColor = ContextCompat.getColor(context, contentTextColorRes);
            }else {
                contentTextColor = a.getColor(R.styleable.EaseConversationListLayout_ease_con_item_content_text_color
                        , ContextCompat.getColor(context, R.color.ease_conversation_color_item_message));
            }
            setModel.setContentTextColor(contentTextColor);

            float dateTextSize = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_date_text_size
                    , sp2px(context, 13));
            setModel.setDateTextSize(dateTextSize);
            int dateTextColorRes = a.getResourceId(R.styleable.EaseConversationListLayout_ease_con_item_date_text_color, -1);
            int dateTextColor;
            if(dateTextColorRes != -1) {
                dateTextColor = ContextCompat.getColor(context, dateTextColorRes);
            }else {
                dateTextColor = a.getColor(R.styleable.EaseConversationListLayout_ease_con_item_date_text_color
                        , ContextCompat.getColor(context, R.color.ease_conversation_color_item_time));
            }
            setModel.setDateTextColor(dateTextColor);

            float mentionTextSize = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_mention_text_size
                    , sp2px(context, 14));
            setModel.setMentionTextSize(mentionTextSize);
            int mentionTextColorRes = a.getResourceId(R.styleable.EaseConversationListLayout_ease_con_item_mention_text_color, -1);
            int mentionTextColor;
            if(mentionTextColorRes != -1) {
                mentionTextColor = ContextCompat.getColor(context, mentionTextColorRes);
            }else {
                mentionTextColor = a.getColor(R.styleable.EaseConversationListLayout_ease_con_item_mention_text_color
                        , ContextCompat.getColor(context, R.color.ease_conversation_color_item_mention));
            }
            setModel.setMentionTextColor(mentionTextColor);

            float avatarSize = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_avatar_size, 0);
            int shapeType = a.getInteger(R.styleable.EaseConversationListLayout_ease_con_item_avatar_shape_type, -1);
            float avatarRadius = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_avatar_radius, dip2px(context, 50));
            float borderWidth = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_avatar_border_width, 0);
            int borderColorRes = a.getResourceId(R.styleable.EaseConversationListLayout_ease_con_item_avatar_border_color, -1);
            int borderColor;
            if(borderColorRes != -1) {
                borderColor = ContextCompat.getColor(context, borderColorRes);
            }else {
                borderColor = a.getColor(R.styleable.EaseConversationListLayout_ease_con_item_avatar_border_color, Color.TRANSPARENT);
            }
            setModel.setAvatarSize(avatarSize);
            setModel.setShapeType(shapeType);
            setModel.setAvatarRadius(avatarRadius);
            setModel.setBorderWidth(borderWidth);
            setModel.setBorderColor(borderColor);

            float itemHeight = a.getDimension(R.styleable.EaseConversationListLayout_ease_con_item_height, dip2px(context, 75));
            Drawable bgDrawable = a.getDrawable(R.styleable.EaseConversationListLayout_ease_con_item_background);
            setModel.setItemHeight(itemHeight);
            setModel.setBgDrawable(bgDrawable);

            int unreadDotPosition = a.getInteger(R.styleable.EaseConversationListLayout_ease_con_item_unread_dot_position, 0);
            setModel.setUnreadDotPosition(unreadDotPosition == 0 ? EaseConversationSetStyle.UnreadDotPosition.LEFT
                    : EaseConversationSetStyle.UnreadDotPosition.RIGHT);

            boolean showSystemMessage = a.getBoolean(R.styleable.EaseConversationListLayout_ease_con_item_show_system_message, true);
            setModel.setShowSystemMessage(showSystemMessage);
            presenter.setShowSystemMessage(showSystemMessage);

            a.recycle();
        }
    }

    private void initViews() {
        presenter.attachView(this);

        rvConversationList = findViewById(R.id.rv_conversation_list);

        rvConversationList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ConcatAdapter();
        listAdapter = new EaseConversationListAdapter();
        adapter.addAdapter(listAdapter);

        menuHelper = new EasePopupMenuHelper();

        initListener();
    }

    private void initListener() {
        listAdapter.setOnItemClickListener(new com.hyphenate.easeui.interfaces.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(itemListener != null) {
                    itemListener.onItemClick(view, position);
                }
            }
        });

        listAdapter.setOnItemLongClickListener(new com.hyphenate.easeui.interfaces.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                listAdapter.getItem(position).setSelected(true);
                if(itemLongListener != null) {
                    return itemLongListener.onItemLongClick(view, position);
                }
                if(showDefaultMenu) {
                    showDefaultMenu(view, position, listAdapter.getItem(position));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        touchX = ev.getX();
        touchY = ev.getY();
        return super.dispatchTouchEvent(ev);
    }

    public void init() {
        listAdapter.addDelegate(new EaseSystemMsgDelegate(setModel));
        listAdapter.addDelegate(new EaseConversationDelegate(setModel));
        rvConversationList.setAdapter(adapter);
    }

    public void loadDefaultData() {
        presenter.loadData();
    }

    /**
     * 设置数据
     * @param data
     */
    public void setData(List<EaseConversationInfo> data) {
        presenter.sortData(data);
    }

    /**
     * 添加数据
     * @param data
     */
    public void addData(List<EaseConversationInfo> data) {
        if(data != null) {
            List<EaseConversationInfo> infos = listAdapter.getData();
            infos.addAll(data);
            presenter.sortData(infos);
        }
    }

    /**
     * 刷新数据
     */
    public void notifyDataSetChanged() {
        if(listAdapter != null) {
            List<EaseAdapterDelegate<Object, EaseBaseRecyclerViewAdapter.ViewHolder>> delegates = listAdapter.getAllDelegate();
            if (delegates != null && !delegates.isEmpty()) {
                for(int i = 0; i < delegates.size(); i++) {
                    EaseBaseConversationDelegate delegate = (EaseBaseConversationDelegate) delegates.get(i);
                    delegate.setSetModel(setModel);
                }
            }
            listAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 返回触摸点的x坐标
     * @return
     */
    public float getTouchX() {
        return touchX;
    }

    /**
     * 返回触摸点的y坐标
     * @return
     */
    public float getTouchY() {
        return touchY;
    }

    private void showDefaultMenu(View view, int position, EaseConversationInfo info) {
        menuHelper.addItemMenu(Menu.NONE, R.id.action_con_make_read, 0, getContext().getString(R.string.ease_conversation_menu_make_read));
        menuHelper.addItemMenu(Menu.NONE, R.id.action_con_make_top, 1, getContext().getString(R.string.ease_conversation_menu_make_top));
        menuHelper.addItemMenu(Menu.NONE, R.id.action_con_cancel_top, 2, getContext().getString(R.string.ease_conversation_menu_cancel_top));
        menuHelper.addItemMenu(Menu.NONE, R.id.action_con_delete, 3, getContext().getString(R.string.ease_conversation_menu_delete));

        menuHelper.initMenu(view);

        //检查置顶配置
        menuHelper.findItemVisible(R.id.action_con_make_top, !info.isTop());
        menuHelper.findItemVisible(R.id.action_con_cancel_top, info.isTop());
        //检查已读配置
        if(info.getInfo() instanceof EMConversation) {
            menuHelper.findItemVisible(R.id.action_con_make_read, ((EMConversation) info.getInfo()).getUnreadMsgCount() > 0);
        }
        menuHelper.findItemVisible(R.id.action_con_make_read, false);
        if(menuPreShowListener != null) {
            menuPreShowListener.onMenuPreShow(menuHelper, position);
        }
        menuHelper.setOnPopupMenuItemClickListener(new OnPopupMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item, int menuPos) {
                if(popupMenuItemClickListener != null && popupMenuItemClickListener.onMenuItemClick(item, position)) {
                    return true;
                }
                int itemId = item.getItemId();
                if(itemId == R.id.action_con_make_read) {
                    presenter.makeConversionRead(position, info);
                    return true;
                }else if(itemId == R.id.action_con_make_top) {
                    presenter.makeConversationTop(position, info);
                    return true;
                }else if(itemId == R.id.action_con_cancel_top) {
                    presenter.cancelConversationTop(position, info);
                    return true;
                }else if(itemId == R.id.action_con_delete) {
                    presenter.deleteConversation(position, info);
                    return true;
                }
                return false;
            }
        });

        menuHelper.setOnPopupMenuDismissListener(new OnPopupMenuDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                info.setSelected(false);
                if(dismissListener != null) {
                    dismissListener.onDismiss(menu);
                }
            }
        });

        menuHelper.show((int) getTouchX(), 0);
    }

    @Override
    public void addHeaderAdapter(RecyclerView.Adapter adapter) {
        this.adapter.addAdapter(0, adapter);
    }

    @Override
    public void addFooterAdapter(RecyclerView.Adapter adapter) {
        this.adapter.addAdapter(adapter);
    }

    @Override
    public void removeAdapter(RecyclerView.Adapter adapter) {
        this.adapter.removeAdapter(adapter);
    }

    @Override
    public void addRVItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        rvConversationList.addItemDecoration(decor);
    }

    @Override
    public void removeRVItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        rvConversationList.removeItemDecoration(decor);
    }

//    @Override
//    public void addDelegate(EaseBaseConversationDelegate delegate) {
//        delegate.setSetModel(setModel);
//        listAdapter.addDelegate(delegate);
//    }

    @Override
    public void setPresenter(EaseConversationPresenter presenter) {
        this.presenter = presenter;
        if(getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getLifecycle().addObserver(presenter);
        }
        this.presenter.setShowSystemMessage(setModel.isShowSystemMessage());
        this.presenter.attachView(this);
    }

    @Override
    public void showItemDefaultMenu(boolean showDefault) {
        showDefaultMenu = showDefault;
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemListener = listener;
    }

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongListener = listener;
    }

    @Override
    public void setItemBackGround(Drawable backGround) {
        setModel.setBgDrawable(backGround);
        notifyDataSetChanged();
    }

    @Override
    public void setItemHeight(int height) {
        setModel.setItemHeight(height);
        notifyDataSetChanged();
    }

    @Override
    public void hideUnreadDot(boolean hide) {
        setModel.setHideUnreadDot(hide);
        notifyDataSetChanged();
    }

    @Override
    public void showSystemMessage(boolean show) {
        setModel.setShowSystemMessage(show);
        presenter.setShowSystemMessage(show);
        notifyDataSetChanged();
    }

    @Override
    public void showUnreadDotPosition(EaseConversationSetStyle.UnreadDotPosition position) {
        setModel.setUnreadDotPosition(position);
        notifyDataSetChanged();
    }

    @Override
    public void setTitleTextSize(int textSize) {
        setModel.setTitleTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setTitleTextColor(int textColor) {
        setModel.setTitleTextColor(textColor);
        notifyDataSetChanged();
    }

    @Override
    public void setContentTextSize(int textSize) {
        setModel.setContentTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setContentTextColor(int textColor) {
        setModel.setContentTextColor(textColor);
        notifyDataSetChanged();
    }

    @Override
    public void setDateTextSize(int textSize) {
        setModel.setDateTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setDateTextColor(int textColor) {
        setModel.setDateTextColor(textColor);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarSize(float avatarSize) {
        setModel.setAvatarSize(avatarSize);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarShapeType(EaseImageView.ShapeType shapeType) {
        setModel.setShapeType(shapeType);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarRadius(int radius) {
        setModel.setAvatarRadius(radius);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarBorderWidth(int borderWidth) {
        setModel.setBorderWidth(borderWidth);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarBorderColor(int borderColor) {
        setModel.setBorderColor(borderColor);
        notifyDataSetChanged();
    }

    @Override
    public void loadConversationListSuccess(List<EaseConversationInfo> data) {
        presenter.sortData(data);
    }

    @Override
    public void loadConversationListNoData() {
        if(loadListener != null) {
            loadListener.loadDataFinish(new ArrayList<>());
        }
        listAdapter.setData(new ArrayList<>());
    }

    @Override
    public void loadConversationListFail(String message) {
        if(loadListener != null) {
            loadListener.loadDataFail(message);
        }
    }

    @Override
    public void sortConversationListSuccess(List<EaseConversationInfo> data) {
        if(loadListener != null) {
            loadListener.loadDataFinish(data);
        }
        listAdapter.setData(data);
    }

    @Override
    public void refreshList() {
        if(conversationChangeListener != null) {
            conversationChangeListener.notifyAllChange();
        }
        presenter.sortData(listAdapter.getData());
    }

    @Override
    public void refreshList(int position) {
        if(conversationChangeListener != null) {
            conversationChangeListener.notifyItemChange(position);
        }
        listAdapter.notifyItemChanged(position);
    }

    @Override
    public void deleteItem(int position) {
        if(listAdapter.getData() == null) {
            return;
        }
        if(conversationChangeListener != null) {
            conversationChangeListener.notifyItemRemove(position);
        }
        try {
            listAdapter.getData().remove(position);
            listAdapter.notifyItemRemoved(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteItemFail(int position, String message) {
        Toast.makeText(getContext(), R.string.ease_conversation_delete_item_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public EaseConversationListAdapter getListAdapter() {
        return listAdapter;
    }

    @Override
    public EaseConversationInfo getItem(int position) {
        if(position >= listAdapter.getData().size()) {
            throw new ArrayIndexOutOfBoundsException(position);
        }
        return listAdapter.getItem(position);
    }

    @Override
    public void makeConversionRead(int position, EaseConversationInfo info) {
        presenter.makeConversionRead(position, info);
    }

    @Override
    public void makeConversationTop(int position, EaseConversationInfo info) {
        presenter.makeConversationTop(position, info);
    }

    @Override
    public void cancelConversationTop(int position, EaseConversationInfo info) {
        presenter.cancelConversationTop(position, info);
    }

    @Override
    public void deleteConversation(int position, EaseConversationInfo info) {
        presenter.deleteConversation(position, info);
    }

    @Override
    public void setOnConversationChangeListener(OnConversationChangeListener listener) {
        this.conversationChangeListener = listener;
    }

    @Override
    public void setOnConversationLoadListener(OnConversationLoadListener loadListener) {
        this.loadListener = loadListener;
    }

    @Override
    public void clearMenu() {
        menuHelper.clear();
    }

    @Override
    public void addItemMenu(int groupId, int itemId, int order, String title) {
        menuHelper.addItemMenu(groupId, itemId, order, title);
    }

    @Override
    public void findItemVisible(int id, boolean visible) {
        menuHelper.findItemVisible(id, visible);
    }

    @Override
    public void setOnPopupMenuPreShowListener(OnPopupMenuPreShowListener preShowListener) {
        this.menuPreShowListener = preShowListener;
    }

    @Override
    public void setOnPopupMenuItemClickListener(OnPopupMenuItemClickListener listener) {
        popupMenuItemClickListener = listener;
    }

    @Override
    public void setOnPopupMenuDismissListener(OnPopupMenuDismissListener listener) {
        dismissListener = listener;
    }

    @Override
    public EasePopupMenuHelper getMenuHelper() {
        return menuHelper;
    }

    @Override
    public Context context() {
        return getContext();
    }
}

