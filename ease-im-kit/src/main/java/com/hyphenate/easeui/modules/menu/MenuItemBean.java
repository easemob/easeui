package com.hyphenate.easeui.modules.menu;

import java.util.Objects;

public class MenuItemBean {
    private int groupId;
    private int itemId;
    private int order;
    private String title;
    private boolean visible = true;
    private int resourceId;

    public MenuItemBean(int groupId, int itemId, int order, String title) {
        this.groupId = groupId;
        this.itemId = itemId;
        this.order = order;
        this.title = title;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItemBean that = (MenuItemBean) o;
        return groupId == that.groupId &&
                itemId == that.itemId &&
                order == that.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, itemId, order);
    }
}

