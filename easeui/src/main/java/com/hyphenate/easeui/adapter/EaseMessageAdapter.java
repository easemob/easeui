package com.hyphenate.easeui.adapter;

import android.view.ViewGroup;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.ui.chat.delegates.EaseMessageAdapterDelegate;

/**
 * 做为对话列表的adapter，继承自{@link EaseBaseDelegateAdapter}
 */
public class EaseMessageAdapter extends EaseBaseDelegateAdapter<EMMessage> {
    public MessageListItemClickListener itemClickListener;
    public EaseMessageListItemStyle itemStyle;

    public EaseMessageAdapter() {
        itemStyle = createDefaultItemStyle();
    }

    @Override
    public int getEmptyLayoutId() {
        return R.layout.ease_layout_empty_list_invisible;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        EaseAdapterDelegate delegate = getAdapterDelegate(viewType);
        if(delegate instanceof EaseMessageAdapterDelegate) {
            ((EaseMessageAdapterDelegate) delegate).setListItemClickListener(itemClickListener);
        }
        return super.getViewHolder(parent, viewType);
    }

    /**
     * 为每个delegate添加item listener和item style
     * @param delegate
     * @return
     */
    @Override
    public EaseBaseDelegateAdapter addDelegate(EaseAdapterDelegate delegate) {
        //设置点击事件
        if(delegate instanceof EaseMessageAdapterDelegate) {
            ((EaseMessageAdapterDelegate) delegate).setListItemClickListener(itemClickListener);
            ((EaseMessageAdapterDelegate) delegate).setItemStyle(itemStyle);
        }
        return super.addDelegate(delegate);
    }

    /**
     * get item message
     * @param position
     * @return
     */
    private EMMessage getItemMessage(int position) {
        if(mData != null && !mData.isEmpty()) {
            return mData.get(position);
        }
        return null;
    }

    /**
     * create default item style
     * @return
     */
    public EaseMessageListItemStyle createDefaultItemStyle() {
        EaseMessageListItemStyle.Builder builder = new EaseMessageListItemStyle.Builder();
        builder.showAvatar(true)
                .showUserNick(false);
        return builder.build();
    }

    /**
     * set item click listener
     * @param itemClickListener
     */
    public void setListItemClickListener(MessageListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * set item style
     * @param itemStyle
     */
    public void setItemStyle(EaseMessageListItemStyle itemStyle) {
        this.itemStyle = itemStyle;
    }

    /**
     * if show nick name
     * @param showUserNick
     */
    public void showUserNick(boolean showUserNick) {
        this.itemStyle.setShowUserNick(showUserNick);
    }

}
