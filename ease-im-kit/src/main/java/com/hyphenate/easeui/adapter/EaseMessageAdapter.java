package com.hyphenate.easeui.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate;

/**
 * 做为对话列表的adapter，继承自{@link EaseBaseDelegateAdapter}
 */
public class EaseMessageAdapter extends EaseBaseDelegateAdapter<EMMessage> {
    public MessageListItemClickListener itemClickListener;
    private int highlightPosition = -1;

    public EaseMessageAdapter() {}

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
        EaseAdapterDelegate clone = null;
        try {
            clone = (EaseAdapterDelegate) delegate.clone();
            clone.setTag(EMMessage.Direct.RECEIVE.name());
            //设置点击事件
            if(clone instanceof EaseMessageAdapterDelegate) {
                ((EaseMessageAdapterDelegate) clone).setListItemClickListener(itemClickListener);
            }
            super.addDelegate(clone);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        delegate.setTag(EMMessage.Direct.SEND.name());
        //设置点击事件
        if(delegate instanceof EaseMessageAdapterDelegate) {
            ((EaseMessageAdapterDelegate) delegate).setListItemClickListener(itemClickListener);
        }
        return super.addDelegate(delegate);
    }

    @Override
    public EaseBaseDelegateAdapter setFallbackDelegate(EaseAdapterDelegate delegate) {
        EaseAdapterDelegate clone = null;
        try {
            clone = (EaseAdapterDelegate) delegate.clone();
            clone.setTag(EMMessage.Direct.RECEIVE.name());
            //设置点击事件
            if(clone instanceof EaseMessageAdapterDelegate) {
                ((EaseMessageAdapterDelegate) clone).setListItemClickListener(itemClickListener);
            }
            super.setFallbackDelegate(clone);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        delegate.setTag(EMMessage.Direct.SEND.name());
        //设置点击事件
        if(delegate instanceof EaseMessageAdapterDelegate) {
            ((EaseMessageAdapterDelegate) delegate).setListItemClickListener(itemClickListener);
        }
        return super.setFallbackDelegate(delegate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(position == highlightPosition) {
            View outLayout = holder.itemView.findViewById(R.id.rl_bubble_out);
            if(outLayout != null) {
                startAnimator(outLayout);
            }else {
                startAnimator(holder.itemView);
            }

            highlightPosition = -1;
        }
    }

    private void startAnimator(View view) {
        Drawable background = view.getBackground();
        int darkColor = ContextCompat.getColor(mContext, R.color.ease_chat_item_bg_dark);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.TRANSPARENT, darkColor);
        colorAnimation.setDuration(500);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((int)animator.getAnimatedValue());
                if((int)animator.getAnimatedValue() == darkColor) {
                    view.setBackground(background);
                }else if((int)animator.getAnimatedValue() == 0) {
                    view.setBackground(null);
                }
            }
        });
        colorAnimation.start();
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
     * Highlight the item view.
     * @param position
     */
    public void highlightItem(int position) {
        this.highlightPosition = position;
        notifyItemChanged(position);
    }

}
