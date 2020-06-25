package com.hyphenate.easeui.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.Direct;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseAdapter;
import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.util.DateUtils;

import java.util.Date;

/**
 * base chat row view
 */
public abstract class EaseChatRow extends LinearLayout {
    protected static final String TAG = EaseChatRow.class.getSimpleName();

    protected LayoutInflater inflater;
    protected Activity context;
    /**
     * ListView's adapter or RecyclerView's adapter
     */
    protected Object adapter;
    protected EMMessage message;
    /**
     * message's position in list
     */
    protected int position;

    /**
     * timestamp
     */
    protected TextView timeStampView;
    /**
     * avatar
     */
    protected ImageView userAvatarView;
    /**
     * bubble
     */
    protected View bubbleLayout;
    /**
     * nickname
     */
    protected TextView usernickView;
    /**
     * percent
     */
    protected TextView percentageView;
    /**
     * progress
     */
    protected ProgressBar progressBar;
    /**
     * status
     */
    protected ImageView statusView;
    /**
     * if asked
     */
    protected TextView ackedView;
    /**
     * if delivered
     */
    protected TextView deliveredView;
    /**
     * if is sender
     */
    protected boolean isSender;

    protected MessageListItemClickListener itemClickListener;
    protected EaseMessageListItemStyle itemStyle;
    private EaseChatRowActionCallback itemActionCallback;

    public EaseChatRow(Context context, boolean isSender) {
        super(context);
        this.context = (Activity) context;
        this.isSender = isSender;
        this.inflater = LayoutInflater.from(context);

        initView();
    }

    public EaseChatRow(Context context, EMMessage message, int position, Object adapter) {
        super(context);
        this.context = (Activity) context;
        this.message = message;
        this.isSender = message.direct() == Direct.SEND;
        this.position = position;
        this.adapter = adapter;
        this.inflater = LayoutInflater.from(context);

        initView();
    }

    @Override
    protected void onDetachedFromWindow() {
        itemActionCallback.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    private void initView() {
        onInflateView();
        timeStampView = (TextView) findViewById(R.id.timestamp);
        userAvatarView = (ImageView) findViewById(R.id.iv_userhead);
        bubbleLayout = findViewById(R.id.bubble);
        usernickView = (TextView) findViewById(R.id.tv_userid);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusView = (ImageView) findViewById(R.id.msg_status);
        ackedView = (TextView) findViewById(R.id.tv_ack);
        deliveredView = (TextView) findViewById(R.id.tv_delivered);

        onFindViewById();
    }

    /**
     * update view
     * @param msg
     */
    public void updateView(final EMMessage msg) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onViewUpdate(msg);
            }
        });
    }

    /**
     * set property according message and position
     * the method should be called by child
     * 
     * @param message
     * @param position
     */
    public void setUpView(EMMessage message, int position,
                          MessageListItemClickListener itemClickListener,
                          EaseChatRowActionCallback itemActionCallback,
                          EaseMessageListItemStyle itemStyle) {
        this.message = message;
        this.position = position;
        this.itemClickListener = itemClickListener;
        this.itemActionCallback = itemActionCallback;
        this.itemStyle = itemStyle;

        setUpBaseView();
        onSetUpView();
        setClickListener();
    }

    /**
     * set timestamp, avatar, nickname and so on
     */
    private void setUpBaseView() {
        TextView timestamp = (TextView) findViewById(R.id.timestamp);
        if (timestamp != null) {
            setTimestamp(timestamp);
        }
        if(userAvatarView != null) {
            setAvatarAndNick();
        }
        if (EMClient.getInstance().getOptions().getRequireDeliveryAck()) {
            if(deliveredView != null){
                if (message.isDelivered()) {
                    deliveredView.setVisibility(View.VISIBLE);
                } else {
                    deliveredView.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (EMClient.getInstance().getOptions().getRequireAck()) {
            if (ackedView != null) {
                if (message.isAcked()) {
                    if (deliveredView != null) {
                        deliveredView.setVisibility(View.INVISIBLE);
                    }
                    ackedView.setVisibility(View.VISIBLE);
                } else {
                    ackedView.setVisibility(View.INVISIBLE);
                }
            }
        }
        setItemStyle();
    }

    /**
     * set item's style by easeMessageListItemStyle
     */
    private void setItemStyle() {
        if (itemStyle != null) {
            // avatar's style which we can easily control avatar's style
            if (userAvatarView != null) {
                setAvatarOptions();
            }
            // nickname's style
            if (usernickView != null) {
                if (itemStyle.isShowUserNick())
                    usernickView.setVisibility(View.VISIBLE);
                else
                    usernickView.setVisibility(View.GONE);
            }
            // bubble's background style
            if (bubbleLayout != null) {
                if (message.direct() == Direct.SEND) {
                    if (itemStyle.getMyBubbleBg() != null) {
                        bubbleLayout.setBackground(itemStyle.getMyBubbleBg());
                    }
                } else if (message.direct() == Direct.RECEIVE) {
                    if (itemStyle.getOtherBubbleBg() != null) {
                        bubbleLayout.setBackground(itemStyle.getOtherBubbleBg());
                    }
                }
            }
        }
    }

    /**
     * set avatar options
     */
    protected void setAvatarOptions() {
        if (itemStyle.isShowAvatar()) {
            userAvatarView.setVisibility(View.VISIBLE);
            EaseAvatarOptions avatarOptions = provideAvatarOptions();
            if(avatarOptions != null && userAvatarView instanceof EaseImageView){
                EaseImageView avatarView = ((EaseImageView)userAvatarView);
                if(avatarOptions.getAvatarShape() != 0)
                    avatarView.setShapeType(avatarOptions.getAvatarShape());
                if(avatarOptions.getAvatarBorderWidth() != 0)
                    avatarView.setBorderWidth(avatarOptions.getAvatarBorderWidth());
                if(avatarOptions.getAvatarBorderColor() != 0)
                    avatarView.setBorderColor(avatarOptions.getAvatarBorderColor());
                if(avatarOptions.getAvatarRadius() != 0)
                    avatarView.setRadius(avatarOptions.getAvatarRadius());
            }
        } else {
            userAvatarView.setVisibility(View.GONE);
        }
    }

    /**
     *
     * @return
     */
    protected EaseAvatarOptions provideAvatarOptions() {
        return EaseUI.getInstance().getAvatarOptions();
    }

    /**
     * set avatar and nickname
     */
    protected void setAvatarAndNick() {
        if (message.direct() == Direct.SEND) {
            EaseUserUtils.setUserAvatar(context, EMClient.getInstance().getCurrentUser(), userAvatarView);
        } else {
            EaseUserUtils.setUserAvatar(context, message.getFrom(), userAvatarView);
            EaseUserUtils.setUserNick(message.getFrom(), usernickView);
        }
    }

    /**
     * set timestamp
     * @param timestamp
     */
    protected void setTimestamp(TextView timestamp) {
        if(adapter != null) {
            if (position == 0) {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            } else {
                // show time stamp if interval with last message is > 30 seconds
                EMMessage prevMessage = null;
                if(adapter instanceof BaseAdapter) {
                    prevMessage = (EMMessage) ((BaseAdapter)adapter).getItem(position - 1);
                }
                if(adapter instanceof EaseBaseAdapter) {
                    prevMessage = (EMMessage) ((EaseBaseAdapter)adapter).getItem(position - 1);
                }

                if (prevMessage != null && DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())) {
                    timestamp.setVisibility(View.GONE);
                } else {
                    timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                    timestamp.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void setTimestamp(EMMessage preMessage) {
        if (position == 0) {
            timeStampView.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
            timeStampView.setVisibility(View.VISIBLE);
        } else {
            if (preMessage != null && DateUtils.isCloseEnough(message.getMsgTime(), preMessage.getMsgTime())) {
                timeStampView.setVisibility(View.GONE);
            } else {
                timeStampView.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timeStampView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * set click listener
     */
    private void setClickListener() {
        if(bubbleLayout != null){
            bubbleLayout.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null && itemClickListener.onBubbleClick(message)){
                        return;
                    }
                    if (itemActionCallback != null) {
                        itemActionCallback.onBubbleClick(message);
                    }
                }
            });
    
            bubbleLayout.setOnLongClickListener(new OnLongClickListener() {
    
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleLongClick(v, message);
                    }
                    return true;
                }
            });
        }

        if (statusView != null) {
            statusView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (itemClickListener != null && itemClickListener.onResendClick(message)){
                        return;
                    }
                    if (itemActionCallback != null) {
                        itemActionCallback.onResendClick(message);
                    }
                }
            });
        }

        if(userAvatarView != null){
            userAvatarView.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        if (message.direct() == Direct.SEND) {
                            itemClickListener.onUserAvatarClick(EMClient.getInstance().getCurrentUser());
                        } else {
                            itemClickListener.onUserAvatarClick(message.getFrom());
                        }
                    }
                }
            });
            userAvatarView.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    if(itemClickListener != null){
                        if (message.direct() == Direct.SEND) {
                            itemClickListener.onUserAvatarLongClick(EMClient.getInstance().getCurrentUser());
                        } else {
                            itemClickListener.onUserAvatarLongClick(message.getFrom());
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * inflate view, child should implement it
     */
    protected abstract void onInflateView();

    /**
     * find view by id
     */
    protected abstract void onFindViewById();

    /**
     * refresh view when message status change
     */
    protected abstract void onViewUpdate(EMMessage msg);

    /**
     * setup view
     * 
     */
    protected abstract void onSetUpView();

    /**
     * row action call back
     */
    public interface EaseChatRowActionCallback {
        /**
         * click resend action
         * @param message
         */
        void onResendClick(EMMessage message);

        /**
         * click bubble layout
         * @param message
         */
        void onBubbleClick(EMMessage message);

        /**
         * when view detach from window
         */
        void onDetachedFromWindow();
    }
}
