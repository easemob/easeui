package com.hyphenate.easeui.modules.conversation.model;

import java.io.Serializable;

/**
 * 做为会话列表的条目对象
 */
public class EaseConversationInfo implements Serializable, Comparable<EaseConversationInfo> {
    //会话列表条目对象，可以是会话消息，可以是系统消息等
    private Object info;
    //条目是否选中
    private boolean isSelected;
    //时间戳
    private long timestamp;
    //是否置顶
    private boolean isTop;
    //是否是群组
    private boolean isGroup;

    private OnSelectListener listener;

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        if(listener != null) {
            listener.onSelect(selected);
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    @Override
    public int compareTo(EaseConversationInfo o) {
        return timestamp > o.timestamp ? -1 : 1;
    }

    public interface OnSelectListener {
        void onSelect(boolean isSelected);
    }
}

