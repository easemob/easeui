package com.hyphenate.easeui.viewholder;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowBigExpression;

public class EaseExpressionViewHolder extends EaseChatRowViewHolder {

    public EaseExpressionViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener,
                                    EaseMessageListItemStyle itemStyle) {
        super(itemView, itemClickListener, itemStyle);
    }

    public static EaseChatRowViewHolder create(ViewGroup parent
            , boolean isSender, MessageListItemClickListener itemClickListener
            , EaseMessageListItemStyle itemStyle) {
        return new EaseExpressionViewHolder(new EaseChatRowBigExpression(parent.getContext(), isSender), itemClickListener, itemStyle);
    }

}
