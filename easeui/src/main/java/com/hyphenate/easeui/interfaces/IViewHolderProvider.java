package com.hyphenate.easeui.interfaces;

import android.util.SparseArray;
import android.view.ViewGroup;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;

import java.util.Map;

/**
 * 开发者可以通过实现下面的两个接口，提供相应的ViewHolder和ViewType
 */
public interface IViewHolderProvider {
    /**
     * 提供对应的ViewHolder
     * @return key指的的对应的viewType, value为对应的ViewHolder
     * @param parent
     * @param itemClickListener
     * @param itemStyle
     */
    EaseChatRowViewHolder provideViewHolder(ViewGroup parent, int viewType, MessageListItemClickListener itemClickListener, EaseMessageListItemStyle itemStyle);

    /**
     * 根据消息类型提供相对应的view type
     * @param message
     * @return 返回的为viewType
     */
    int provideViewType(EMMessage message);

}
