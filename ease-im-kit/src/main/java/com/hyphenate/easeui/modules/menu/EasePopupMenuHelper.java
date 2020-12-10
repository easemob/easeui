package com.hyphenate.easeui.modules.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EasePopupMenuHelper implements PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener {
    private View targetView;
    private OnPopupMenuItemClickListener itemClickListener;
    private OnPopupMenuDismissListener dismissListener;
    private List<MenuItemBean> menuItems = new ArrayList<>();
    private PopupMenu pMenu;
    private Menu menu;

    public EasePopupMenuHelper() {
        menuItems.clear();
    }

    /**
     * 此方法需要在{@link #show(int, int)}之前调用
     * @param view
     */
    public void initMenu(@NonNull View view) {
        targetView = view;
        pMenu = new PopupMenu(targetView.getContext(), targetView);
        menu = pMenu.getMenu();

        pMenu.setOnMenuItemClickListener(this);
        pMenu.setOnDismissListener(this);

        addMenuItem();
    }

    private void addMenuItem() {
        if(menuItems.isEmpty()) {
            return;
        }
        for (MenuItemBean item : menuItems) {
            MenuItem menuItem = menu.findItem(item.getItemId());
            if(menuItem == null) {
                menu.add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle());
            }
        }
    }

    public void clear() {
        menuItems.clear();
    }

    public void addItemMenu(MenuItemBean item) {
        if(!menuItems.contains(item)) {
            menuItems.add(item);
        }
    }

    public void addItemMenu(int groupId, int itemId, int order, String title) {
        MenuItemBean item = new MenuItemBean(groupId, itemId, order, title);
        if(!menuItems.contains(item)) {
            menuItems.add(item);
        }
    }

    public void findItemVisible(int id, boolean visible) {
        if(menu == null) {
            throw new NullPointerException("PopupMenu must init first!");
        }
        try {
            menu.findItem(id).setVisible(visible);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show() {
        show(0, 0);
    }

    @SuppressLint("RestrictedApi")
    public void show(int x, int y) {
        if(menu == null) {
            throw new NullPointerException("PopupMenu must init first!");
        }
        addMenuItem();
        try {
            Field field = pMenu.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper helper = (MenuPopupHelper) field.get(pMenu);
//            x = (int) (x - dip2px(targetView.getContext(), 100));
//            if(x < 0) {
//                x = 0;
//            }
            //y = y - targetView.getHeight();
            Log.e("TAG", "show menu x = "+x + " y = "+y);
            helper.show(x, y);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(itemClickListener != null) {
            return itemClickListener.onMenuItemClick(item, -1);
        }
        return false;
    }

    /**
     * 设置条目点击事件
     * @param listener
     */
    public void setOnPopupMenuItemClickListener(OnPopupMenuItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * 监听PopupMenu dismiss事件
     * @param listener
     */
    public void setOnPopupMenuDismissListener(OnPopupMenuDismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    public void onDismiss(PopupMenu menu) {
        if(dismissListener != null) {
            dismissListener.onDismiss(menu);
        }
    }
}

