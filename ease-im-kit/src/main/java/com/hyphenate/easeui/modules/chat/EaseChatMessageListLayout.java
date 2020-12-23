package com.hyphenate.easeui.modules.chat;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.manager.EaseMessageTypeSetManager;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.modules.chat.interfaces.IChatMessageItemSet;
import com.hyphenate.easeui.modules.chat.interfaces.IChatMessageListLayout;
import com.hyphenate.easeui.modules.chat.interfaces.IRecyclerViewHandle;
import com.hyphenate.easeui.modules.chat.model.EaseChatItemStyleHelper;
import com.hyphenate.easeui.modules.chat.presenter.EaseChatMessagePresenter;
import com.hyphenate.easeui.modules.chat.presenter.EaseChatMessagePresenterImpl;
import com.hyphenate.easeui.modules.chat.presenter.IChatMessageListView;
import com.hyphenate.easeui.utils.EaseCommonUtils;

import java.util.List;


public class EaseChatMessageListLayout extends RelativeLayout implements IChatMessageListView, IRecyclerViewHandle
                                                                        , IChatMessageItemSet, IChatMessageListLayout {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String TAG = EaseChatMessageListLayout.class.getSimpleName();
    private EaseChatMessagePresenter presenter;
    private EaseMessageAdapter messageAdapter;
    private ConcatAdapter baseAdapter;
    /**
     * 加载数据的方式，目前有三种，常规模式（从本地加载），漫游模式，查询历史消息模式（通过数据库搜索）
     */
    private LoadDataType loadDataType;
    /**
     * 消息id，一般是搜索历史消息时会用到这个参数
     */
    private String msgId;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private RecyclerView rvList;
    private SwipeRefreshLayout srlRefresh;
    private LinearLayoutManager layoutManager;
    private EMConversation conversation;
    /**
     * 会话类型，包含单聊，群聊和聊天室
     */
    private EMConversation.EMConversationType conType;
    /**
     * 另一侧的环信id
     */
    private String username;
    private boolean canUseRefresh = true;
    private LoadMoreStatus loadMoreStatus;
    private OnMessageTouchListener messageTouchListener;
    private OnChatErrorListener errorListener;
    /**
     * 上一次控件的高度
     */
    private int recyclerViewLastHeight;
    /**
     * 条目具体控件的点击事件
     */
    private MessageListItemClickListener messageListItemClickListener;
    private EaseChatItemStyleHelper chatSetHelper;

    public EaseChatMessageListLayout(@NonNull Context context) {
        this(context, null);
    }

    public EaseChatMessageListLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatMessageListLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ease_chat_message_list, this);
        EaseChatItemStyleHelper.getInstance().clear();
        chatSetHelper = EaseChatItemStyleHelper.getInstance();
        presenter = new EaseChatMessagePresenterImpl();
        if(context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(presenter);
        }
        initAttrs(context, attrs);
        initViews();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EaseChatMessageListLayout);
            float textSize = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_text_size
                    , 0);
            chatSetHelper.setTextSize((int) textSize);
            int textColorRes = a.getResourceId(R.styleable.EaseChatMessageListLayout_ease_chat_item_text_color, -1);
            int textColor;
            if(textColorRes != -1) {
                textColor = ContextCompat.getColor(context, textColorRes);
            }else {
                textColor = a.getColor(R.styleable.EaseChatMessageListLayout_ease_chat_item_text_color, 0);
            }
            chatSetHelper.setTextColor(textColor);

            float itemMinHeight = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_min_height, 0);
            chatSetHelper.setItemMinHeight((int) itemMinHeight);

            float timeTextSize = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_text_size, 0);
            chatSetHelper.setTimeTextSize((int) timeTextSize);
            int timeTextColorRes = a.getResourceId(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_text_color, -1);
            int timeTextColor;
            if(timeTextColorRes != -1) {
                timeTextColor = ContextCompat.getColor(context, textColorRes);
            }else {
                timeTextColor = a.getColor(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_text_color, 0);
            }
            chatSetHelper.setTimeTextColor(timeTextColor);
            chatSetHelper.setTimeBgDrawable(a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_time_background));

            Drawable avatarDefaultDrawable = a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_default_src);
            //float avatarSize = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_size, 0);
            int shapeType = a.getInteger(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_shape_type, 0);
            //float avatarRadius = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_radius, 0);
            //float borderWidth = a.getDimension(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_border_width, 0);
            //int borderColorRes = a.getResourceId(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_border_color, -1);
//            int borderColor;
//            if(borderColorRes != -1) {
//                borderColor = ContextCompat.getColor(context, borderColorRes);
//            }else {
//                borderColor = a.getColor(R.styleable.EaseChatMessageListLayout_ease_chat_item_avatar_border_color, Color.TRANSPARENT);
//            }
            chatSetHelper.setAvatarDefaultSrc(avatarDefaultDrawable);
//            chatSetHelper.setAvatarSize(avatarSize);
            chatSetHelper.setShapeType(shapeType);
//            chatSetHelper.setAvatarRadius(avatarRadius);
//            chatSetHelper.setBorderWidth(borderWidth);
//            chatSetHelper.setBorderColor(borderColor);

            chatSetHelper.setReceiverBgDrawable(a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_receiver_background));
            chatSetHelper.setSenderBgDrawable(a.getDrawable(R.styleable.EaseChatMessageListLayout_ease_chat_item_sender_background));

            //chatSetHelper.setShowAvatar(a.getBoolean(R.styleable.EaseChatMessageListLayout_ease_chat_item_show_avatar, true));
            chatSetHelper.setShowNickname(a.getBoolean(R.styleable.EaseChatMessageListLayout_ease_chat_item_show_nickname, false));

            chatSetHelper.setItemShowType(a.getInteger(R.styleable.EaseChatMessageListLayout_ease_chat_item_show_type, 0));

            a.recycle();
        }
    }

    private void initViews() {
        presenter.attachView(this);

        rvList = findViewById(R.id.message_list);
        srlRefresh = findViewById(R.id.srl_refresh);

        srlRefresh.setEnabled(canUseRefresh);

        layoutManager = new LinearLayoutManager(getContext());
        rvList.setLayoutManager(layoutManager);

        baseAdapter = new ConcatAdapter();
        messageAdapter = new EaseMessageAdapter();
        baseAdapter.addAdapter(messageAdapter);
        rvList.setAdapter(baseAdapter);

        registerChatType();

        initListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(conversation != null) {
            conversation.markAllMessagesAsRead();
        }
        EaseChatItemStyleHelper.getInstance().clear();
    }

    private void registerChatType() {
        try {
            EaseMessageTypeSetManager.getInstance().registerMessageType(messageAdapter);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void init(LoadDataType loadDataType, String username, int chatType) {
        this.username = username;
        this.loadDataType = loadDataType;
        this.conType = EaseCommonUtils.getConversationType(chatType);
        conversation = EMClient.getInstance().chatManager().getConversation(username, conType, true);
        presenter.setupWithConversation(conversation);
    }

    public void init(String username, int chatType) {
        init(LoadDataType.LOCAL, username, chatType);
    }

    public void loadDefaultData() {
        loadData(pageSize, null);
    }

    public void loadData(String msgId) {
        loadData(pageSize, msgId);
    }

    public void loadData(int pageSize, String msgId) {
        this.pageSize = pageSize;
        this.msgId = msgId;
        checkConType();
    }

    private void checkConType() {
        if(isChatRoomCon()) {
            presenter.joinChatRoom(username);
        }else {
            loadData();
        }
    }

    private void loadData() {
        if(!isSingleChat()) {
            chatSetHelper.setShowNickname(true);
        }
        conversation.markAllMessagesAsRead();
        if(loadDataType == LoadDataType.ROAM) {
            presenter.loadServerMessages(pageSize);
        }else if(loadDataType == LoadDataType.HISTORY) {
            presenter.loadMoreLocalHistoryMessages(msgId, pageSize, EMConversation.EMSearchDirection.DOWN);
        }else {
            presenter.loadLocalMessages(pageSize);
        }
    }

    /**
     * 加载更多的更早一些的数据，下拉加载更多
     */
    public void loadMorePreviousData() {
        String msgId = getListFirstMessageId();
        if(loadDataType == LoadDataType.ROAM) {
            presenter.loadMoreServerMessages(msgId, pageSize);
        }else if(loadDataType == LoadDataType.HISTORY) {
            presenter.loadMoreLocalHistoryMessages(msgId, pageSize, EMConversation.EMSearchDirection.UP);
        }else {
            presenter.loadMoreLocalMessages(msgId, pageSize);
        }
    }

    /**
     * 专用于加载更多的更新一些的数据，上拉加载更多时使用
     */
    public void loadMoreHistoryData() {
        String msgId = getListLastMessageId();
        if(loadDataType == LoadDataType.HISTORY) {
            loadMoreStatus = LoadMoreStatus.HAS_MORE;
            presenter.loadMoreLocalHistoryMessages(msgId, pageSize, EMConversation.EMSearchDirection.DOWN);
        }
    }

    /**
     * 获取列表最下面的一条消息的id
     * @return
     */
    private String getListFirstMessageId() {
        EMMessage message = null;
        try {
            message = messageAdapter.getData().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message == null ? null : message.getMsgId();
    }

    /**
     * 获取列表最下面的一条消息的id
     * @return
     */
    private String getListLastMessageId() {
        EMMessage message = null;
        try {
            message = messageAdapter.getData().get(messageAdapter.getData().size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message == null ? null : message.getMsgId();
    }

    public boolean isChatRoomCon() {
        return conType == EMConversation.EMConversationType.ChatRoom;
    }

    public boolean isGroupChat() {
        return conType == EMConversation.EMConversationType.GroupChat;
    }

    private boolean isSingleChat() {
        return conType == EMConversation.EMConversationType.Chat;
    }

    private void initListener() {
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMorePreviousData();
            }
        });
        rvList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //判断状态及是否还有更多数据
                   if(loadDataType == LoadDataType.HISTORY
                           && loadMoreStatus == LoadMoreStatus.HAS_MORE
                           && layoutManager.findLastVisibleItemPosition() != 0
                           && layoutManager.findLastVisibleItemPosition() == layoutManager.getItemCount() -1) {
                       //加载更多
                       loadMoreHistoryData();
                   }
                }else {
                    //if recyclerView not idle should hide keyboard
                    if(messageTouchListener != null) {
                        messageTouchListener.onViewDragging();
                    }
                }
            }
        });

        //用于监听RecyclerView高度的变化，从而刷新列表
        rvList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = rvList.getHeight();
                if(recyclerViewLastHeight == 0) {
                    recyclerViewLastHeight = height;
                }
                if(recyclerViewLastHeight != height) {
                    //RecyclerView高度发生变化，刷新页面
                    if(messageAdapter.getData() != null && !messageAdapter.getData().isEmpty()) {
                        post(()-> smoothSeekToPosition(messageAdapter.getData().size() - 1));
                    }
                }
                recyclerViewLastHeight = height;
            }
        });

        messageAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(messageTouchListener != null) {
                    messageTouchListener.onTouchItemOutside(view, position);
                }
            }
        });
        messageAdapter.setListItemClickListener(new MessageListItemClickListener() {
            @Override
            public boolean onBubbleClick(EMMessage message) {
                if(messageListItemClickListener != null) {
                    return messageListItemClickListener.onBubbleClick(message);
                }
                return false;
            }

            @Override
            public boolean onResendClick(EMMessage message) {
                if(messageListItemClickListener != null) {
                    return messageListItemClickListener.onResendClick(message);
                }
                return false;
            }

            @Override
            public boolean onBubbleLongClick(View v, EMMessage message) {
                if(messageListItemClickListener != null) {
                    return messageListItemClickListener.onBubbleLongClick(v, message);
                }
                return false;
            }

            @Override
            public void onUserAvatarClick(String username) {
                if(messageListItemClickListener != null) {
                    messageListItemClickListener.onUserAvatarClick(username);
                }
            }

            @Override
            public void onUserAvatarLongClick(String username) {
                if(messageListItemClickListener != null) {
                    messageListItemClickListener.onUserAvatarLongClick(username);
                }
            }

            @Override
            public void onMessageCreate(EMMessage message) {
                if(messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageCreate(message);
                }
            }

            @Override
            public void onMessageSuccess(EMMessage message) {
                if(messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageSuccess(message);
                }
            }

            @Override
            public void onMessageError(EMMessage message, int code, String error) {
                if(messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageError(message, code, error);
                }
            }

            @Override
            public void onMessageInProgress(EMMessage message, int progress) {
                if(messageListItemClickListener != null) {
                    messageListItemClickListener.onMessageInProgress(message, progress);
                }
            }
        });
    }

    /**
     * 停止下拉动画
     */
    private void finishRefresh() {
        if(presenter.isActive()) {
            runOnUi(() -> {
                if(srlRefresh != null) {
                    srlRefresh.setRefreshing(false);
                }
            });
        }
    }

    private void notifyDataSetChanged() {
        messageAdapter.notifyDataSetChanged();
    }

    /**
     * 设置数据
     * @param data
     */
    public void setData(List<EMMessage> data) {
        messageAdapter.setData(data);
    }

    /**
     * 添加数据
     * @param data
     */
    public void addData(List<EMMessage> data) {
        messageAdapter.addData(data);
    }

    @Override
    public Context context() {
        return getContext();
    }

    @Override
    public EMConversation getCurrentConversation() {
        return conversation;
    }

    @Override
    public void joinChatRoomSuccess(EMChatRoom value) {
        loadData();
    }

    @Override
    public void joinChatRoomFail(int error, String errorMsg) {
        if(presenter.isActive()) {
            runOnUi(() -> {
                if(errorListener != null) {
                    errorListener.onChatError(error, errorMsg);
                }
            });
        }
    }

    @Override
    public void loadMsgFail(int error, String message) {
        finishRefresh();
        if(errorListener != null) {
            errorListener.onChatError(error, message);
        }
    }

    @Override
    public void loadLocalMsgSuccess(List<EMMessage> data) {
        refreshToLatest();
    }

    @Override
    public void loadNoLocalMsg() {

    }

    @Override
    public void loadMoreLocalMsgSuccess(List<EMMessage> data) {
        finishRefresh();
        presenter.refreshCurrentConversation();
        post(()->smoothSeekToPosition(data.size() - 1));
    }

    @Override
    public void loadNoMoreLocalMsg() {
        finishRefresh();
    }

    @Override
    public void loadMoreLocalHistoryMsgSuccess(List<EMMessage> data, EMConversation.EMSearchDirection direction) {
        if(direction == EMConversation.EMSearchDirection.UP) {
            finishRefresh();
            messageAdapter.addData(0, data);
        }else {
            messageAdapter.addData(data);
            if(data.size() >= pageSize) {
                loadMoreStatus = LoadMoreStatus.HAS_MORE;
            }else {
                loadMoreStatus = LoadMoreStatus.NO_MORE_DATA;
            }
        }
    }

    @Override
    public void loadNoMoreLocalHistoryMsg() {
        finishRefresh();
    }

    @Override
    public void loadServerMsgSuccess(List<EMMessage> data) {
        presenter.refreshToLatest();
    }

    @Override
    public void loadMoreServerMsgSuccess(List<EMMessage> data) {
        finishRefresh();
        presenter.refreshCurrentConversation();
        post(()-> smoothSeekToPosition(data.size() - 1));
    }

    @Override
    public void refreshCurrentConSuccess(List<EMMessage> data, boolean toLatest) {
        messageAdapter.setData(data);
        if(toLatest) {
            seekToPosition(data.size() - 1);
        }
    }

    @Override
    public void canUseDefaultRefresh(boolean canUseRefresh) {
        this.canUseRefresh = canUseRefresh;
        srlRefresh.setEnabled(canUseRefresh);
    }

    @Override
    public void refreshMessages() {
        presenter.refreshCurrentConversation();
    }

    @Override
    public void refreshToLatest() {
        presenter.refreshToLatest();
    }

    @Override
    public void refreshMessage(EMMessage message) {
        int position = messageAdapter.getData().lastIndexOf(message);
        if(position != -1) {
            runOnUi(()-> messageAdapter.notifyItemChanged(position));
        }
    }

    @Override
    public void removeMessage(EMMessage message) {
        if(message == null || messageAdapter.getData() == null) {
            return;
        }
        conversation.removeMessage(message.getMsgId());
        runOnUi(()-> {
            if(presenter.isActive()) {
                List<EMMessage> messages = messageAdapter.getData();
                int position = messages.lastIndexOf(message);
                if(position != -1) {
                    //需要保证条目从集合中删除
                    messages.remove(position);
                    //通知适配器删除条目
                    messageAdapter.notifyItemRemoved(position);
                    //通知刷新下一条消息
                    messageAdapter.notifyItemChanged(position);
                }
            }
        });
    }

    @Override
    public void moveToPosition(int position) {
        seekToPosition(position);
    }

    @Override
    public void showNickname(boolean showNickname) {
        chatSetHelper.setShowNickname(showNickname);
        notifyDataSetChanged();
    }

    @Override
    public void setItemSenderBackground(Drawable bgDrawable) {
        chatSetHelper.setSenderBgDrawable(bgDrawable);
        notifyDataSetChanged();
    }

    @Override
    public void setItemReceiverBackground(Drawable bgDrawable) {
        chatSetHelper.setReceiverBgDrawable(bgDrawable);
        notifyDataSetChanged();
    }

    @Override
    public void setItemTextSize(int textSize) {
        chatSetHelper.setTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setItemTextColor(int textColor) {
        chatSetHelper.setTextColor(textColor);
        notifyDataSetChanged();
    }

//    @Override
//    public void setItemMinHeight(int height) {
//        chatSetHelper.setItemMinHeight(height);
//        notifyDataSetChanged();
//    }

    @Override
    public void setTimeTextSize(int textSize) {
        chatSetHelper.setTimeTextSize(textSize);
        notifyDataSetChanged();
    }

    @Override
    public void setTimeTextColor(int textColor) {
        chatSetHelper.setTimeTextColor(textColor);
        notifyDataSetChanged();
    }

    @Override
    public void setTimeBackground(Drawable bgDrawable) {
        chatSetHelper.setTimeBgDrawable(bgDrawable);
        notifyDataSetChanged();
    }

    @Override
    public void setItemShowType(ShowType type) {
        if(!isSingleChat()) {
            chatSetHelper.setItemShowType(type.ordinal());
            notifyDataSetChanged();
        }
    }

    @Override
    public void setAvatarDefaultSrc(Drawable src) {
        chatSetHelper.setAvatarDefaultSrc(src);
        notifyDataSetChanged();
    }

//    @Override
//    public void setAvatarSize(float avatarSize) {
//        chatSetHelper.setAvatarSize(avatarSize);
//        notifyDataSetChanged();
//    }

    @Override
    public void setAvatarShapeType(int shapeType) {
        chatSetHelper.setShapeType(shapeType);
        notifyDataSetChanged();
    }

//    @Override
//    public void setAvatarRadius(int radius) {
//        chatSetHelper.setAvatarRadius(radius);
//        notifyDataSetChanged();
//    }

//    @Override
//    public void setAvatarBorderWidth(int borderWidth) {
//        chatSetHelper.setBorderWidth(borderWidth);
//        notifyDataSetChanged();
//    }

//    @Override
//    public void setAvatarBorderColor(int borderColor) {
//        chatSetHelper.setBorderColor(borderColor);
//        notifyDataSetChanged();
//    }

    @Override
    public void addHeaderAdapter(RecyclerView.Adapter adapter) {
        baseAdapter.addAdapter(0, adapter);
    }

    @Override
    public void addFooterAdapter(RecyclerView.Adapter adapter) {
        baseAdapter.addAdapter(adapter);
    }

    @Override
    public void removeAdapter(RecyclerView.Adapter adapter) {
        baseAdapter.removeAdapter(adapter);
    }

    @Override
    public void addRVItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        rvList.addItemDecoration(decor);
    }

    @Override
    public void removeRVItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        rvList.removeItemDecoration(decor);
    }

    /**
     * 是否有新的消息
     * 判断依据为：数据库中最新的一条数据的时间戳是否大于页面上的最新一条数据的时间戳
     * @return
     */
    public boolean haveNewMessages() {
        if(messageAdapter == null || messageAdapter.getData() == null || messageAdapter.getData().isEmpty()
                || conversation == null || conversation.getLastMessage() == null) {
            return false;
        }
        return conversation.getLastMessage().getMsgTime() > messageAdapter.getData().get(messageAdapter.getData().size() - 1).getMsgTime();
    }

    /**
     * 移动到指定位置
     * @param position
     */
    private void seekToPosition(int position) {
        if(presenter.isDestroy() || rvList == null) {
            return;
        }
        if(position < 0) {
            position = 0;
        }
        RecyclerView.LayoutManager manager = rvList.getLayoutManager();
        if(manager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
        }
    }

    /**
     * 移动到指定位置
     * @param position
     */
    private void smoothSeekToPosition(int position) {
        if(presenter.isDestroy() || rvList == null) {
            return;
        }
        if(position < 0) {
            position = 0;
        }
        RecyclerView.LayoutManager manager = rvList.getLayoutManager();
        if(manager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
            //setMoveAnimation(manager, position);
        }
    }

    private void setMoveAnimation(RecyclerView.LayoutManager manager, int position) {
        int prePosition;
        if(position > 0) {
            prePosition = position - 1;
        }else {
            prePosition = position;
        }
        View view = manager.findViewByPosition(0);
        int height;
        if(view != null) {
            height = view.getHeight();
        }else {
            height = 200;
        }
        ValueAnimator animator = ValueAnimator.ofInt(-height, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ((LinearLayoutManager)manager).scrollToPositionWithOffset(prePosition, value);
            }
        });
        animator.setDuration(800);
        animator.start();
    }

    @Override
    public void setPresenter(EaseChatMessagePresenter presenter) {
        this.presenter = presenter;
        if(getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getLifecycle().addObserver(presenter);
        }
        this.presenter.attachView(this);
        this.presenter.setupWithConversation(conversation);
    }

    @Override
    public EaseMessageAdapter getMessageAdapter() {
        return messageAdapter;
    }

    @Override
    public void setOnMessageTouchListener(OnMessageTouchListener listener) {
        this.messageTouchListener = listener;
    }

    @Override
    public void setOnChatErrorListener(OnChatErrorListener listener) {
        this.errorListener = listener;
    }

    @Override
    public void setMessageListItemClickListener(MessageListItemClickListener listener) {
        this.messageListItemClickListener = listener;
    }

    /**
     * 是否滑动到底部
     * @param recyclerView
     * @return
     */
    public static boolean isVisibleBottom(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
        //当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
        //RecyclerView的滑动状态
        int state = recyclerView.getScrollState();
        if(visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && state == recyclerView.SCROLL_STATE_IDLE){
            return true;
        } else {
            return false;
        }
    }

    public void runOnUi(Runnable runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable);
    }

    /**
     * 消息列表接口
     */
    public interface OnMessageTouchListener {
        /**
         * touch事件
         * @param v
         * @param position
         */
        void onTouchItemOutside(View v, int position);

        /**
         * 控件正在被拖拽
         */
        void onViewDragging();
    }

    public interface OnChatErrorListener {
        /**
         * 聊天中错误信息
         * @param code
         * @param errorMsg
         */
        void onChatError(int code, String errorMsg);
    }

    /**
     * 三种数据加载模式，local是从本地数据库加载，Roam是开启消息漫游，History是搜索本地消息
     */
    public enum LoadDataType {
        LOCAL, ROAM, HISTORY
    }

    /**
     * 加载更多的状态
     */
    public enum LoadMoreStatus {
        IS_LOADING, HAS_MORE, NO_MORE_DATA
    }

    /**
     * 条目的展示方式
     * normal区分发送方和接收方
     * left发送方和接收方在左侧
     * right发送方和接收方在右侧
     */
    public enum ShowType {
        NORMAL, LEFT/*, RIGHT*/
    }
}

