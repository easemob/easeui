package com.hyphenate.easeui.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.manager.EaseConTypeSetManager;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.utils.EaseCommonUtils;

import java.util.List;

public class EaseChatMessageList extends RelativeLayout implements SwipeRefreshLayout.OnRefreshListener {
    private Context context;
    private SwipeRefreshLayout srlRefresh;
    private EaseRecyclerView messageList;
    private EaseMessageListItemStyle itemStyle;
    private int chatType;
    private String toChatUsername;
    private EMConversation conversation;
    private EaseMessageAdapter messageAdapter;
    private MessageListItemClickListener itemClickListener;
    private OnMessageListListener listener;
    private List<EMMessage> currentMessages;
    private boolean showUserNick;
    private LinearLayoutManager layoutManager;
    private loadMoreStatus status = loadMoreStatus.HAS_MORE;
    private String historyMsgId;//历史消息id
    private boolean isHistoryStatus;//是否是搜索历史消息状态，根据historyMsgId是否为空判断
    private boolean isHistoryMoveToLatest;//历史消息滑动到最新的一条了
    private int recyclerViewLastHeight;//用于记录RecyclerView上一次的高度，用于判断是否高度发生变化

    public EaseChatMessageList(Context context) {
        this(context, null);
    }

    public EaseChatMessageList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatMessageList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseStyle(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        //加载布局
        LayoutInflater.from(context).inflate(R.layout.ease_chat_message_list, this);
        srlRefresh = findViewById(R.id.srl_refresh);
        messageList = findViewById(R.id.message_list);
    }

    private void parseStyle(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseChatMessageList);
        EaseMessageListItemStyle.Builder builder = new EaseMessageListItemStyle.Builder();
        builder.showAvatar(ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserAvatar, true))
                .showUserNick(ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserNick, false))
                .myBubbleBg(ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground))
                .otherBuddleBg(ta.getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground));

        itemStyle = builder.build();
        ta.recycle();
    }


    public void init(String toChatUsername, int chatType) {
        this.chatType = chatType;
        this.toChatUsername = toChatUsername;
        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
        layoutManager = new LinearLayoutManager(context);
        messageList.setLayoutManager(layoutManager);
        messageAdapter = new EaseMessageAdapter();
        registerDelegates();
        messageList.setAdapter(messageAdapter);

        messageAdapter.setListItemClickListener(itemClickListener);
        messageAdapter.showUserNick(showUserNick);

        initListener();
    }

    /**
     * 设置历史消息id，用于搜索消息
     * @param historyMsgId
     */
    public void setHistoryMsgId(String historyMsgId) {
        this.historyMsgId = historyMsgId;
        if(!TextUtils.isEmpty(historyMsgId)) {
            isHistoryStatus = true;
        }
    }

    private void initListener() {
        srlRefresh.setOnRefreshListener(this);
        messageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE
                        && status == loadMoreStatus.HAS_MORE
                        && layoutManager.findLastVisibleItemPosition() == layoutManager.getItemCount() -1){
                    //showLiveList(true);
                    if(listener != null) {
                        listener.onLoadMore();
                    }
                }
            }
        });
        //用于监听RecyclerView高度的变化，从而刷新列表
        messageList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = messageList.getHeight();
                if(recyclerViewLastHeight == 0) {
                    recyclerViewLastHeight = height;
                }
                if(recyclerViewLastHeight != height) {
                    //RecyclerView高度发生变化，刷新页面
                    if(currentMessages != null) {
                        seekToPosition(currentMessages.size() - 1);
                    }
                }
                recyclerViewLastHeight = height;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(this.listener != null) {
            this.listener.onTouch(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 设置默认的消息类型
     */
    private void registerDelegates() {
        try {
            EaseConTypeSetManager.getInstance().registerConversationType(messageAdapter);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        if(this.listener != null) {
            listener.onRefresh();
        }
    }

    /**
     * 刷新对话列表
     */
    public void refreshMessages() {
        if(isActivityDisable() || messageList == null || conversation == null) {
            return;
        }
        messageList.post(()-> {
            if(isHistoryStatus && !isHistoryMoveToLatest) {
                //如果是历史消息状态，且没有移动到最新的一条数据，则什么也不做
            }else {
                List<EMMessage> messages = conversation.getAllMessages();
                conversation.markAllMessagesAsRead();
                if(messageAdapter != null) {
                    messageAdapter.setData(messages);
                }
                currentMessages = messages;
                finishRefresh();
            }
        });
    }

    public void refreshToLatest() {
        if(isActivityDisable() || conversation == null) {
            return;
        }
        //如果是历史消息状态，则判断当前消息中是否有最近的消息，如果没有的话，不刷新到最下面
        if(isHistoryStatus) {
            if(currentMessages == null) {
                return;
            }
            EMMessage message = currentMessages.get(currentMessages.size() - 1);
            List<EMMessage> allMessages = conversation.getAllMessages();
            if(allMessages == null || allMessages.size() < 2) {
                return;
            }
            EMMessage lastMessage = allMessages.get(allMessages.size() - 2);
            //当前页面的最新的一条数据，不是数据库中最近的一条；或者还有更多的消息，都不移动到页面的最底部
            if(!TextUtils.equals(message.getMsgId(), lastMessage.getMsgId()) || status != loadMoreStatus.NO_MORE_DATA) {
                return;
            }
            isHistoryMoveToLatest = true;
            refreshMessages();
            seekToPosition(conversation.getAllMessages().size() - 1);
        }else {
            refreshMessages();
            seekToPosition(conversation.getAllMessages().size() - 1);
        }

    }

    private void finishRefresh() {
        if(srlRefresh != null) {
            srlRefresh.setRefreshing(false);
        }
    }

    /**
     * 移动到指定位置
     * @param position
     */
    private void seekToPosition(int position) {
        if(isActivityDisable() || messageList == null) {
            return;
        }
        if(position < 0) {
            position = 0;
        }
        RecyclerView.LayoutManager manager = messageList.getLayoutManager();
        if(manager instanceof LinearLayoutManager) {
            if(isActivityDisable()) {
                return;
            }
            int finalPosition = position;
            messageList.post(()-> {
                setMoveAnimation(manager, finalPosition);
            });
        }
    }

    private void setMoveAnimation(RecyclerView.LayoutManager manager, int position) {
        ValueAnimator animator = ValueAnimator.ofInt(-200, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ((LinearLayoutManager)manager).scrollToPositionWithOffset(position, value);
            }
        });
        animator.setDuration(500);
        animator.start();
    }

    /**
     * load more messages
     */
    public void loadMoreMessages(int pageSize, boolean loadFromServer) {
        if(isHistoryStatus) {
            loadMoreHistoryMessages(pageSize, EMConversation.EMSearchDirection.UP);
        }else {
            if(loadFromServer) {
                loadMoreServerMessages(pageSize);
                return;
            }
            loadMoreLocalMessages(pageSize);
        }

    }

    /**
     * 从本地获取更多的历史消息
     * 此方法主要用于搜索历史消息，上拉加载和下拉加载
     * @param pageSize
     */
    public void loadMoreHistoryMessages(int pageSize, EMConversation.EMSearchDirection direction) {
        long timeStamp = 0;
        List<EMMessage> data = messageAdapter.getData();
        //如果当前页面还没有数据的话，则使用historyMsgId从数据库查找数据
        List<EMMessage> messages;
        if(data == null || data.isEmpty()) {
            messages = conversation.searchMsgFromDB(conversation.getMessage(historyMsgId, true).getMsgTime() - 1,
                    pageSize, direction);
            data = messages;
            if(direction != EMConversation.EMSearchDirection.UP) {
                if(messages.size() >= pageSize) {
                    status = loadMoreStatus.HAS_MORE;
                }else {
                    status = loadMoreStatus.NO_MORE_DATA;
                }
            }
        }else {
            try {
                if(direction == EMConversation.EMSearchDirection.UP) {
                    timeStamp = data.get(0).getMsgTime();
                }else {
                    timeStamp = data.get(data.size() - 1).getMsgTime();
                    status = loadMoreStatus.IS_LOADING;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(timeStamp == 0) {
                return;
            }
            messages = conversation.searchMsgFromDB(timeStamp, pageSize, direction);
            if(direction == EMConversation.EMSearchDirection.UP) {
                data.addAll(0, messages);
            }else {
                data.addAll(messages);
                if(messages.size() >= pageSize) {
                    status = loadMoreStatus.HAS_MORE;
                }else {
                    status = loadMoreStatus.NO_MORE_DATA;
                }
            }
        }
        finishRefresh();
        messageAdapter.setData(data);
        currentMessages = data;
    }

    /**
     * 从本地数据库拉取数据
     */
    public void loadMessagesFromLocal(int pageSize) {
        //如果是历史模式，则直接从数据库中获取数据
        if(isHistoryStatus) {
            loadMoreHistoryMessages(pageSize, EMConversation.EMSearchDirection.DOWN);
            return;
        }
        int msgCount = getCacheMessageCount();
        if(msgCount < getAllMsgCount() && msgCount < pageSize) {
            loadMoreMessages(pageSize - msgCount, false);
        }else {
            seekToPosition(msgCount - 1);
        }
    }

    /**
     * 获取数据库中消息总数目
     * @return
     */
    protected int getAllMsgCount() {
        return conversation == null ? 0 : conversation.getAllMsgCount();
    }

    public void loadMoreServerMessages(int pageSize) {
        loadMoreServerMessages(pageSize, false);
    }

    /**
     * 从服务器加载更多数据
     * @param pageSize 一次加载的条数
     * @param moveToLast 是否移动到最后
     */
    public void loadMoreServerMessages(int pageSize, boolean moveToLast) {
        //如果是搜索历史模式，则直接从数据库获取数据
        if(isHistoryStatus) {
            loadMoreHistoryMessages(pageSize, EMConversation.EMSearchDirection.DOWN);
            return;
        }
        int count = getCacheMessageCount();
        String msgId = moveToLast ? "" : count > 0 ? conversation.getAllMessages().get(0).getMsgId() : "";
        EMClient.getInstance().chatManager().asyncFetchHistoryMessage(toChatUsername,
                EaseCommonUtils.getConversationType(chatType), pageSize, msgId,
                new EMValueCallBack<EMCursorResult<EMMessage>>() {
                    @Override
                    public void onSuccess(EMCursorResult<EMMessage> value) {
                        if(isActivityDisable()) {
                            return;
                        }
                        if(messageList == null) {
                            return;
                        }
                        messageList.post(()->{
                            loadMoreLocalMessages(pageSize, moveToLast);
                        });
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        if(isActivityDisable()) {
                            return;
                        }
                        if(messageList == null) {
                            return;
                        }
                        messageList.post(()-> {
                            if(listener != null) {
                                listener.onMessageListError(errorMsg);
                            }
                            loadMoreLocalMessages(pageSize, moveToLast);
                        });
                    }
                });
    }

    /**
     * 获取内存中消息数目
     * @return
     */
    protected int getCacheMessageCount() {
        if(conversation == null) {
            return 0;
        }
        List<EMMessage> messageList = conversation.getAllMessages();
        return messageList != null ? messageList.size() : 0;
    }

    private void loadMoreLocalMessages(int pageSize) {
        loadMoreLocalMessages(pageSize, false);
    }

    /**
     * 加载更多的本地数据
     * @param pageSize
     * @param moveToLast
     */
    private void loadMoreLocalMessages(int pageSize, boolean moveToLast) {
        List<EMMessage> messageList = conversation.getAllMessages();
        int msgCount = messageList != null ? messageList.size() : 0;
        int allMsgCount = conversation.getAllMsgCount();
        if(msgCount < allMsgCount) {
            String msgId = null;
            if(msgCount > 0) {
                msgId = messageList.get(0).getMsgId();
            }
            List<EMMessage> moreMsgs = null;
            String errorMsg = null;
            try {
                moreMsgs = conversation.loadMoreMsgFromDB(msgId, pageSize);
            } catch (Exception e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
            }
            // 刷新数据，一则刷新数据，二则需要消息进行定位
            if(moreMsgs == null || moreMsgs.isEmpty()) {
                if(listener != null) {
                    listener.onMessageListError(errorMsg);
                }
                return;
            }
            refreshMessages();
            // 对消息进行定位
            seekToPosition(moveToLast ? conversation.getAllMessages().size() - 1 : moreMsgs.size() - 1);
        }else {
            finishRefresh();
            // 对消息进行定位
            if(moveToLast) {
                seekToPosition(conversation.getAllMessages().size() - 1);
            }else {
                Toast.makeText(context, getResources().getString(R.string.no_more_messages), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 是否有新的消息
     * 判断依据为：数据库中最新的一条数据的时间戳是否大于页面上的最新一条数据的时间戳
     * @return
     */
    public boolean haveNewMessages() {
        if(currentMessages == null) {
            return false;
        }
        return conversation.getLastMessage().getMsgTime() > currentMessages.get(currentMessages.size() - 1).getMsgTime();
    }

    /**
     * 判断当前activity是否不可用
     * @return
     */
    public boolean isActivityDisable() {
        return context == null || (context instanceof Activity && ((Activity) context).isFinishing());
    }

    /**
     * 设置条目点击监听
     * @param listener
     */
    public void setItemClickListener(MessageListItemClickListener listener) {
        this.itemClickListener = listener;
        if(messageAdapter != null) {
            messageAdapter.setListItemClickListener(listener);
            messageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置对话列表监听事件
     * @param listener
     */
    public void setOnMessageListListener(OnMessageListListener listener) {
        this.listener = listener;
    }

    /**
     * 是否展示昵称
     * @param showUserNick
     */
    public void showUserNick(boolean showUserNick) {
        this.showUserNick = showUserNick;
        if(messageAdapter != null) {
            messageAdapter.showUserNick(showUserNick);
            messageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 消息列表接口
     */
    public interface OnMessageListListener {
        /**
         * touch事件
         * @param v
         * @param event
         */
        void onTouch(View v, MotionEvent event);

        /**
         * 下拉刷新
         */
        void onRefresh();

        /**
         * 错误
         * @param message
         */
        void onMessageListError(String message);

        /**
         * 上拉加载更多
         */
        void onLoadMore();
    }

    /**
     * 加载更多的状态
     */
    public enum loadMoreStatus {
        IS_LOADING, HAS_MORE, NO_MORE_DATA
    }
}
