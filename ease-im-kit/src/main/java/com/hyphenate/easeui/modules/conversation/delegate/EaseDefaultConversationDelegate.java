package com.hyphenate.easeui.modules.conversation.delegate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationSetStyle;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

public abstract class EaseDefaultConversationDelegate extends EaseBaseConversationDelegate<EaseConversationInfo, EaseDefaultConversationDelegate.ViewHolder> {

    public EaseDefaultConversationDelegate(EaseConversationSetStyle setModel) {
        super(setModel);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, String tag) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ease_item_row_chat_history, parent, false);
        return new ViewHolder(view, setModel);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, EaseConversationInfo item) {
        super.onBindViewHolder(holder, position, item);
        onBindConViewHolder(holder, position, item);
    }

    protected abstract void onBindConViewHolder(ViewHolder holder, int position, EaseConversationInfo item);

    public static class ViewHolder extends EaseBaseRecyclerViewAdapter.ViewHolder<EaseConversationInfo> {
        public ConstraintLayout listIteaseLayout;
        public EaseImageView avatar;
        public TextView mUnreadMsgNumber;
        public TextView unreadMsgNumberRight;
        public TextView name;
        public TextView time;
        public ImageView mMsgState;
        public TextView mentioned;
        public TextView message;
        public Context mContext;
        private final Drawable bgDrawable;

        public ViewHolder(@NonNull View itemView, EaseConversationSetStyle setModel) {
            super(itemView);
            mContext = itemView.getContext();
            listIteaseLayout = findViewById(R.id.list_itease_layout);
            avatar = findViewById(R.id.avatar);
            mUnreadMsgNumber = findViewById(R.id.unread_msg_number);
            unreadMsgNumberRight = findViewById(R.id.unread_msg_number_right);
            name = findViewById(R.id.name);
            time = findViewById(R.id.time);
            mMsgState = findViewById(R.id.msg_state);
            mentioned = findViewById(R.id.mentioned);
            message = findViewById(R.id.message);
            EaseUserUtils.setUserAvatarStyle(avatar);
            if(setModel != null) {
                float titleTextSize = setModel.getTitleTextSize();
                if(titleTextSize != 0) {
                    name.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
                }
                int titleTextColor = setModel.getTitleTextColor();
                if(titleTextColor != 0) {
                    name.setTextColor(titleTextColor);
                }
                float contentTextSize = setModel.getContentTextSize();
                if(contentTextSize != 0) {
                    message.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize);
                }
                int contentTextColor = setModel.getContentTextColor();
                if(contentTextColor != 0) {
                    message.setTextColor(contentTextColor);
                }
                float dateTextSize = setModel.getDateTextSize();
                if(dateTextSize != 0) {
                    time.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextSize);
                }
                int dateTextColor = setModel.getDateTextColor();
                if(dateTextColor != 0) {
                    time.setTextColor(dateTextColor);
                }
                float mentionTextSize = setModel.getMentionTextSize();
                if(mentionTextSize != 0) {
                    mentioned.setTextSize(TypedValue.COMPLEX_UNIT_PX, mentionTextSize);
                }
                int mentionTextColor = setModel.getMentionTextColor();
                if(mentionTextColor != 0) {
                    mentioned.setTextColor(mentionTextColor);
                }
                float avatarSize = setModel.getAvatarSize();
                if(avatarSize != 0) {
                    ViewGroup.LayoutParams layoutParams = avatar.getLayoutParams();
                    layoutParams.height = (int) avatarSize;
                    layoutParams.width = (int) avatarSize;
                }
                avatar.setShapeType(setModel.getShapeType());
                float borderWidth = setModel.getBorderWidth();
                if(borderWidth != 0) {
                    avatar.setBorderWidth((int) borderWidth);
                }
                int borderColor = setModel.getBorderColor();
                if(borderColor != 0) {
                    avatar.setBorderColor(borderColor);
                }
                float avatarRadius = setModel.getAvatarRadius();
                if(avatarRadius != 0) {
                    avatar.setRadius((int) avatarRadius);
                }
                float itemHeight = setModel.getItemHeight();
                if(itemHeight != 0) {
                    ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                    layoutParams.height = (int) itemHeight;
                }
                Drawable bgDrawable = setModel.getBgDrawable();
                if(bgDrawable != null) {
                    itemView.setBackground(bgDrawable);
                }
                mUnreadMsgNumber.setVisibility(setModel.isHideUnreadDot() ? View.GONE : View.VISIBLE);
                EaseConversationSetStyle.UnreadDotPosition dotPosition = setModel.getUnreadDotPosition();
                if(dotPosition == EaseConversationSetStyle.UnreadDotPosition.LEFT) {
                    mUnreadMsgNumber.setVisibility(View.VISIBLE);
                    unreadMsgNumberRight.setVisibility(View.GONE);
                }else {
                    mUnreadMsgNumber.setVisibility(View.GONE);
                    unreadMsgNumberRight.setVisibility(View.VISIBLE);
                }
            }
            bgDrawable = itemView.getBackground();
        }

        @Override
        public void initView(View itemView) {

        }

        @Override
        public void setData(EaseConversationInfo item, int position) {
            item.setOnSelectListener(new EaseConversationInfo.OnSelectListener() {
                @Override
                public void onSelect(boolean isSelected) {
                    if(isSelected) {
                        itemView.setBackgroundResource(R.drawable.ease_conversation_item_selected);
                    }else {
                        if(item.isTop()) {
                            itemView.setBackgroundResource(R.drawable.ease_conversation_top_bg);
                        }else {
                            itemView.setBackground(bgDrawable);
                        }
                    }
                }
            });
        }
    }

    public void showUnreadNum(ViewHolder holder, int unreadMsgCount) {
        if(unreadMsgCount > 0) {
            holder.mUnreadMsgNumber.setText(handleBigNum(unreadMsgCount));
            holder.unreadMsgNumberRight.setText(handleBigNum(unreadMsgCount));
            showUnreadRight(holder, setModel.getUnreadDotPosition() == EaseConversationSetStyle.UnreadDotPosition.RIGHT);
        }else {
            holder.mUnreadMsgNumber.setVisibility(View.GONE);
            holder.unreadMsgNumberRight.setVisibility(View.GONE);
        }
    }

    public String handleBigNum(int unreadMsgCount) {
        if(unreadMsgCount <= 99) {
            return String.valueOf(unreadMsgCount);
        }else {
            return "99+";
        }
    }

    public void showUnreadRight(ViewHolder holder, boolean isRight) {
        if(isRight) {
            holder.mUnreadMsgNumber.setVisibility(View.GONE);
            holder.unreadMsgNumberRight.setVisibility(View.VISIBLE);
        }else {
            holder.mUnreadMsgNumber.setVisibility(View.VISIBLE);
            holder.unreadMsgNumberRight.setVisibility(View.GONE);
        }
    }
}

