package com.hyphenate.easeui.modules.contact;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
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

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.modules.contact.adapter.EaseContactListAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.interfaces.OnItemLongClickListener;
import com.hyphenate.easeui.modules.contact.adapter.EaseContactCustomAdapter;
import com.hyphenate.easeui.modules.contact.interfaces.IContactCustomListLayout;
import com.hyphenate.easeui.modules.contact.interfaces.IContactListLayout;
import com.hyphenate.easeui.modules.contact.interfaces.IContactListStyle;
import com.hyphenate.easeui.modules.contact.interfaces.OnContactLoadListener;
import com.hyphenate.easeui.modules.contact.model.EaseContactSetStyle;
import com.hyphenate.easeui.modules.contact.presenter.EaseContactPresenter;
import com.hyphenate.easeui.modules.contact.presenter.EaseContactPresenterImpl;
import com.hyphenate.easeui.modules.contact.presenter.IEaseContactListView;
import com.hyphenate.easeui.modules.interfaces.IPopupMenu;
import com.hyphenate.easeui.modules.menu.OnPopupMenuDismissListener;
import com.hyphenate.easeui.modules.menu.OnPopupMenuItemClickListener;
import com.hyphenate.easeui.modules.menu.EasePopupMenuHelper;
import com.hyphenate.easeui.modules.menu.OnPopupMenuPreShowListener;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.easeui.widget.EaseRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EaseContactListLayout extends EaseRecyclerView implements IEaseContactListView, IContactListLayout, IContactCustomListLayout
                                                                        , IContactListStyle, IPopupMenu {

    private EaseContactSetStyle contactSetModel;
    private EaseContactPresenter presenter;
    private ConcatAdapter concatAdapter;
    private EaseContactListAdapter listAdapter;
    private OnItemClickListener itemListener;
    private OnItemLongClickListener itemLongListener;
    private OnPopupMenuItemClickListener popupMenuItemClickListener;
    private OnPopupMenuDismissListener dismissListener;
    private OnPopupMenuPreShowListener menuPreShowListener;
    private EasePopupMenuHelper menuHelper;
    private OnItemClickListener customItemClickListener;
    private OnContactLoadListener loadListener;

    private boolean showDefaultMenu = true;
    private float touchX;
    private float touchY;
    private EaseContactCustomAdapter customAdapter;

    public EaseContactListLayout(Context context) {
        this(context, null);
    }

    public EaseContactListLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseContactListLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contactSetModel = new EaseContactSetStyle();
        contactSetModel.setShowItemHeader(true);

        presenter = new EaseContactPresenterImpl();
        if(context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(presenter);
        }

        initAttrs(context, attrs);

        initViews();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EaseContactListLayout);
            float titleTextSize = a.getDimension(R.styleable.EaseContactListLayout_ease_contact_item_title_text_size
                    , sp2px(context, 16));
            contactSetModel.setTitleTextSize(titleTextSize);
            int titleTextColorRes = a.getResourceId(R.styleable.EaseContactListLayout_ease_contact_item_title_text_color, -1);
            int titleTextColor;
            if(titleTextColorRes != -1) {
                titleTextColor = ContextCompat.getColor(context, titleTextColorRes);
            }else {
                titleTextColor = a.getColor(R.styleable.EaseContactListLayout_ease_contact_item_title_text_color
                        , ContextCompat.getColor(context, R.color.ease_contact_color_item_title));
            }
            contactSetModel.setTitleTextColor(titleTextColor);

            float headerTextSize = a.getDimension(R.styleable.EaseContactListLayout_ease_contact_item_header_text_size
                    , sp2px(context, 16));
            contactSetModel.setHeaderTextSize(headerTextSize);
            int headerTextColorRes = a.getResourceId(R.styleable.EaseContactListLayout_ease_contact_item_header_text_color, -1);
            int headerTextColor;
            if(headerTextColorRes != -1) {
                headerTextColor = ContextCompat.getColor(context, titleTextColorRes);
            }else {
                headerTextColor = a.getColor(R.styleable.EaseContactListLayout_ease_contact_item_header_text_color
                        , ContextCompat.getColor(context, R.color.ease_contact_color_item_header));
            }
            contactSetModel.setHeaderTextColor(headerTextColor);
            contactSetModel.setHeaderBgDrawable(a.getDrawable(R.styleable.EaseContactListLayout_ease_contact_item_header_background));

            Drawable avatarDefaultDrawable = a.getDrawable(R.styleable.EaseContactListLayout_ease_contact_item_avatar_default_src);
            float avatarSize = a.getDimension(R.styleable.EaseContactListLayout_ease_contact_item_avatar_size, 0);
            int shapeType = a.getInteger(R.styleable.EaseContactListLayout_ease_contact_item_avatar_shape_type, -1);
            float avatarRadius = a.getDimension(R.styleable.EaseContactListLayout_ease_contact_item_avatar_radius, dip2px(context, 50));
            float borderWidth = a.getDimension(R.styleable.EaseContactListLayout_ease_contact_item_avatar_border_width, 0);
            int borderColorRes = a.getResourceId(R.styleable.EaseContactListLayout_ease_contact_item_avatar_border_color, -1);
            int borderColor;
            if(borderColorRes != -1) {
                borderColor = ContextCompat.getColor(context, borderColorRes);
            }else {
                borderColor = a.getColor(R.styleable.EaseContactListLayout_ease_contact_item_avatar_border_color, Color.TRANSPARENT);
            }
            contactSetModel.setAvatarDefaultSrc(avatarDefaultDrawable);
            contactSetModel.setAvatarSize(avatarSize);
            contactSetModel.setShapeType(shapeType);
            contactSetModel.setAvatarRadius(avatarRadius);
            contactSetModel.setBorderWidth(borderWidth);
            contactSetModel.setBorderColor(borderColor);

            float itemHeight = a.getDimension(R.styleable.EaseContactListLayout_ease_contact_item_height, dip2px(context, 75));
            Drawable bgDrawable = a.getDrawable(R.styleable.EaseContactListLayout_ease_contact_item_background);
            contactSetModel.setItemHeight(itemHeight);
            contactSetModel.setBgDrawable(bgDrawable);

            a.recycle();
        }
    }

    private void initViews() {
        presenter.attachView(this);

        this.setLayoutManager(new LinearLayoutManager(getContext()));
        concatAdapter = new ConcatAdapter();
        listAdapter = new EaseContactListAdapter();
        listAdapter.setSettingModel(contactSetModel);
        addHeader();
        concatAdapter.addAdapter(listAdapter);
        setAdapter(concatAdapter);

        menuHelper = new EasePopupMenuHelper();

        initListener();
    }

    private void addHeader() {
        customAdapter = new EaseContactCustomAdapter();
        customAdapter.setSettingModel(contactSetModel);
        customAdapter.setEmptyView(R.layout.ease_layout_empty_list_wrap_content);
        concatAdapter.addAdapter(customAdapter);
    }

    private void initListener() {
        listAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(itemListener != null) {
                    itemListener.onItemClick(view, position);
                }
            }
        });

        listAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
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

        customAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(customItemClickListener != null) {
                    customItemClickListener.onItemClick(view, position);
                }
            }
        });
    }

    private void showDefaultMenu(View view, int position, EaseUser user) {
        menuHelper.addItemMenu(Menu.NONE, R.id.action_add_note, 0, getContext().getString(R.string.ease_contact_menu_add_note));

        menuHelper.initMenu(view);

        //该功能未实现，暂时隐藏
        menuHelper.findItemVisible(R.id.action_add_note, false);
        if(menuPreShowListener != null) {
            menuPreShowListener.onMenuPreShow(menuHelper, position);
        }

        menuHelper.setOnPopupMenuItemClickListener(new OnPopupMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item, int menuPos) {
                if(popupMenuItemClickListener != null && popupMenuItemClickListener.onMenuItemClick(item, position)) {
                    return true;
                }
                if(item.getItemId() == R.id.action_add_note) {
                    presenter.addNote(position, user);
                    return true;
                }
                return false;
            }
        });

        menuHelper.setOnPopupMenuDismissListener(new OnPopupMenuDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if(dismissListener != null) {
                    dismissListener.onDismiss(menu);
                }
            }
        });

        menuHelper.show((int) getTouchX(), 0);
    }

    public void loadDefaultData() {
        presenter.loadData();
    }

    /**
     * 设置数据
     * @param data
     */
    public void setData(List<EaseUser> data) {
        presenter.sortData(data);
    }

    /**
     * 设置数据，不进行排序
     * @param data
     */
    public void setDataNotSort(List<EaseUser> data) {
        listAdapter.setData(data);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        touchX = ev.getX();
        touchY = ev.getY();
        return super.dispatchTouchEvent(ev);
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

    public void notifyDataSetChanged() {
        listAdapter.setSettingModel(contactSetModel);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(EaseContactPresenter presenter) {
        this.presenter = presenter;
        if(getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getLifecycle().addObserver(presenter);
        }
        this.presenter.attachView(this);
    }

    @Override
    public void showItemDefaultMenu(boolean showDefault) {
        this.showDefaultMenu = showDefault;
    }

    @Override
    public void showItemHeader(boolean showItemHeader) {
        contactSetModel.setShowItemHeader(showItemHeader);
        notifyDataSetChanged();
    }

    @Override
    public EaseContactListAdapter getListAdapter() {
        return listAdapter;
    }

    @Override
    public EaseUser getItem(int position) {
        if(position >= listAdapter.getData().size()) {
            throw new ArrayIndexOutOfBoundsException(position);
        }
        return listAdapter.getItem(position);
    }

    @Override
    public void setItemBackGround(Drawable backGround) {
        contactSetModel.setBgDrawable(backGround);
        notifyDataSetChanged();
    }

    @Override
    public void setItemHeight(int height) {
        contactSetModel.setItemHeight(height);
        notifyDataSetChanged();
    }

    @Override
    public void setHeaderBackGround(Drawable backGround) {
        contactSetModel.setHeaderBgDrawable(backGround);
        notifyDataSetChanged();
    }

    @Override
    public void setTitleTextSize(int textSize) {
        contactSetModel.setTitleTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setTitleTextColor(int textColor) {
        contactSetModel.setTitleTextColor(textColor);
        notifyDataSetChanged();
    }

    @Override
    public void setHeaderTextSize(int textSize) {
        contactSetModel.setHeaderTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setHeaderTextColor(int textColor) {
        contactSetModel.setHeaderTextColor(textColor);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarDefaultSrc(Drawable src) {
        contactSetModel.setAvatarDefaultSrc(src);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarSize(float avatarSize) {
        contactSetModel.setAvatarSize(avatarSize);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarShapeType(EaseImageView.ShapeType shapeType) {
        contactSetModel.setShapeType(shapeType);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarRadius(int radius) {
        contactSetModel.setAvatarRadius(radius);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarBorderWidth(int borderWidth) {
        contactSetModel.setBorderWidth(borderWidth);
        notifyDataSetChanged();
    }

    @Override
    public void setAvatarBorderColor(int borderColor) {
        contactSetModel.setBorderColor(borderColor);
        notifyDataSetChanged();
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
        this.popupMenuItemClickListener = listener;
    }

    @Override
    public void setOnPopupMenuDismissListener(OnPopupMenuDismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    public EasePopupMenuHelper getMenuHelper() {
        return menuHelper;
    }

    @Override
    public void addHeaderAdapter(Adapter adapter) {
        concatAdapter.addAdapter(0, adapter);
    }

    @Override
    public void addFooterAdapter(Adapter adapter) {
        concatAdapter.addAdapter(adapter);
    }

    @Override
    public void removeAdapter(Adapter adapter) {
        concatAdapter.removeAdapter(adapter);
    }

    @Override
    public void addRVItemDecoration(@NonNull ItemDecoration decor) {
        addItemDecoration(decor);
    }

    @Override
    public void removeRVItemDecoration(@NonNull ItemDecoration decor) {
        removeItemDecoration(decor);
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
    public void loadContactListSuccess(List<EaseUser> data) {
        presenter.sortData(data);
    }

    @Override
    public void loadContactListNoData() {
        if(loadListener != null) {
            loadListener.loadDataFinish(new ArrayList<>());
        }
    }

    @Override
    public void loadContactListFail(String message) {
        if(loadListener != null) {
            loadListener.loadDataFail(message);
        }
    }

    @Override
    public void sortContactListSuccess(List<EaseUser> data) {
        if(loadListener != null) {
            loadListener.loadDataFinish(data);
        }
        listAdapter.setData(data);
    }

    @Override
    public void refreshList() {
        notifyDataSetChanged();
    }

    @Override
    public void refreshList(int position) {
        listAdapter.notifyItemChanged(position);
    }

    @Override
    public void addNote(int position) {
        listAdapter.notifyItemChanged(position);
    }

    @Override
    public void addNoteFail(int position, String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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

    @Override
    public Context context() {
        return getContext();
    }

    @Override
    public void addCustomItem(int id, int imageResource, String name) {
        if(customAdapter != null) {
            customAdapter.addItem(id, imageResource, name);
        }
    }

    @Override
    public void addCustomItem(int id, String image, String name) {
        if(customAdapter != null) {
            customAdapter.addItem(id, image, name);
        }
    }

    @Override
    public void setOnCustomItemClickListener(OnItemClickListener listener) {
        customItemClickListener = listener;
    }

    @Override
    public void setOnContactLoadListener(OnContactLoadListener loadListener) {
        this.loadListener = loadListener;
    }

    @Override
    public EaseContactCustomAdapter getCustomAdapter() {
        return customAdapter;
    }
}

