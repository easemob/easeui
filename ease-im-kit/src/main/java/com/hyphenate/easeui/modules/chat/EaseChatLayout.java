package com.hyphenate.easeui.modules.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hyphenate.EMConversationListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.adapter.EMAChatRoomManagerListener;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.interfaces.EaseChatRoomListener;
import com.hyphenate.easeui.interfaces.EaseGroupListener;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.manager.EaseAtMessageHelper;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.modules.chat.interfaces.ChatInputMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatLayout;
import com.hyphenate.easeui.modules.chat.interfaces.OnAddMsgAttrsBeforeSendEvent;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatLayoutListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatRecordTouchListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnMenuChangeListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnRecallMessageResultListener;
import com.hyphenate.easeui.modules.chat.presenter.EaseHandleMessagePresenter;
import com.hyphenate.easeui.modules.chat.presenter.EaseHandleMessagePresenterImpl;
import com.hyphenate.easeui.modules.chat.presenter.IHandleMessageView;
import com.hyphenate.easeui.modules.interfaces.IPopupWindow;
import com.hyphenate.easeui.modules.menu.EasePopupWindow;
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper;
import com.hyphenate.easeui.modules.menu.MenuItemBean;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.util.List;

public class EaseChatLayout extends RelativeLayout implements IChatLayout, IHandleMessageView, IPopupWindow
        , ChatInputMenuListener, EMMessageListener, EaseChatMessageListLayout.OnMessageTouchListener
        , MessageListItemClickListener, EaseChatMessageListLayout.OnChatErrorListener {
    private static final String TAG = EaseChatLayout.class.getSimpleName();
    private static final int MSG_TYPING_HEARTBEAT = 0;
    private static final int MSG_TYPING_END = 1;
    private static final int MSG_OTHER_TYPING_END = 2;

    public static final String ACTION_TYPING_BEGIN = "TypingBegin";
    public static final String ACTION_TYPING_END = "TypingEnd";
    protected static final int TYPING_SHOW_TIME = 10000;
    protected static final int OTHER_TYPING_SHOW_TIME = 5000;

    public static final String AT_PREFIX = "@";
    public static final String AT_SUFFIX = " ";

    private EaseChatMessageListLayout messageListLayout;
    private EaseChatInputMenu inputMenu;
    private EaseVoiceRecorderView voiceRecorder;
    /**
     * "正在输入"功能的开关，打开后本设备发送消息将持续发送cmd类型消息通知对方"正在输入"
     */
    private boolean turnOnTyping;
    /**
     * 用于处理用户是否正在输入的handler
     */
    private Handler typingHandler;
    /**
     * 会话id，可能是对方环信id，也可能是群id或者聊天室id
     */
    private String conversationId;
    /**
     * 聊天类型
     */
    private int chatType;
    /**
     * 用于监听消息的变化
     */
    private OnChatLayoutListener listener;
    /**
     * 用于监听发送语音的触摸事件
     */
    private OnChatRecordTouchListener recordTouchListener;
    private EaseHandleMessagePresenter presenter;
    /**
     * 是否展示默认菜单
     */
    private boolean showDefaultMenu = true;
    /**
     * 长按条目菜单帮助类
     */
    private EasePopupWindowHelper menuHelper;
    private ClipboardManager clipboard;
    private OnMenuChangeListener menuChangeListener;
    /**
     * 撤回监听
     */
    private OnRecallMessageResultListener recallMessageListener;
    /**
     * 聊天室监听
     */
    private ChatRoomListener chatRoomListener;
    /**
     * 群组监听
     */
    private GroupListener groupListener;
    /**
     * 发送消息前添加消息属性事件
     */
    private OnAddMsgAttrsBeforeSendEvent sendMsgEvent;
    /**
     * 是否是首次发送，默认true
     */
    private boolean isNotFirstSend;

    public EaseChatLayout(Context context) {
        this(context, null);
    }

    public EaseChatLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        presenter = new EaseHandleMessagePresenterImpl();
        if(context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(presenter);
        }
        LayoutInflater.from(context).inflate(R.layout.ease_layout_chat, this);
        initView();
        initListener();
    }

    private void initView() {
        messageListLayout = findViewById(R.id.layout_chat_message);
        inputMenu = findViewById(R.id.layout_menu);
        voiceRecorder = findViewById(R.id.voice_recorder);

        presenter.attachView(this);

        menuHelper = new EasePopupWindowHelper();
        clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    private void initListener() {
        messageListLayout.setOnMessageTouchListener(this);
        messageListLayout.setMessageListItemClickListener(this);
        messageListLayout.setOnChatErrorListener(this);
        inputMenu.setChatInputMenuListener(this);
        getChatManager().addMessageListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getChatManager().removeMessageListener(this);
        if(chatRoomListener != null) {
            EMClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomListener);
        }
        if(groupListener != null) {
            EMClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
        }
        if(isChatRoomCon()) {
            EMClient.getInstance().chatroomManager().leaveChatRoom(conversationId);
        }
        if(isGroupCon()) {
            EaseAtMessageHelper.get().removeAtMeGroup(conversationId);
            EaseAtMessageHelper.get().cleanToAtUserList();
        }
        if(typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 初始化
     * @param username 环信id
     * @param chatType 聊天类型，单聊，群聊或者聊天室
     */
    public void init(String username, int chatType) {
        init(EaseChatMessageListLayout.LoadDataType.LOCAL, username, chatType);
    }

    /**
     * 初始化
     * @param loadDataType 加载数据模式
     * @param conversationId 会话id，可能是对方环信id，也可能是群id或者聊天室id
     * @param chatType 聊天类型，单聊，群聊或者聊天室
     */
    public void init(EaseChatMessageListLayout.LoadDataType loadDataType, String conversationId, int chatType) {
        this.conversationId = conversationId;
        this.chatType = chatType;
        messageListLayout.init(loadDataType, this.conversationId, chatType);
        presenter.setupWithToUser(chatType, this.conversationId);
        if(isChatRoomCon()) {
            chatRoomListener = new ChatRoomListener();
            EMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomListener);
        }else if(isGroupCon()) {
            EaseAtMessageHelper.get().removeAtMeGroup(conversationId);
            groupListener = new GroupListener();
            EMClient.getInstance().groupManager().addGroupChangeListener(groupListener);
        }
        initTypingHandler();
    }

    /**
     * 初始化历史消息搜索模式
     * @param toChatUsername
     * @param chatType
     */
    public void initHistoryModel(String toChatUsername, int chatType) {
        init(EaseChatMessageListLayout.LoadDataType.HISTORY, toChatUsername, chatType);
    }

    public void loadDefaultData() {
        sendChannelAck();
        messageListLayout.loadDefaultData();
    }

    public void loadData(String msgId, int pageSize) {
        sendChannelAck();
        messageListLayout.loadData(pageSize, msgId);
    }

    public void loadData(String msgId) {
        sendChannelAck();
        messageListLayout.loadData(msgId);
    }

    private void initTypingHandler() {
        typingHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_TYPING_HEARTBEAT :
                        setTypingBeginMsg(this);
                        break;
                    case MSG_TYPING_END :
                        setTypingEndMsg(this);
                        break;
                    case MSG_OTHER_TYPING_END:
                        setOtherTypingEnd(this);
                        break;
                }
            }
        };
        if(!turnOnTyping) {
            if(typingHandler != null) {
                typingHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    /**
     * 发送channel ack消息
     * (1)如果是1v1会话，对方将收到channel ack的回调，回调方法为{@link EMConversationListener#onConversationRead(String, String)},
     * SDK内部将会将该会话的发送消息的isAcked置为true.
     * (2)如果是多端设备，另一端将会收到channel ack的回调，SDK内部将会把该会话置为已读。
     */
    private void sendChannelAck() {
        if(EaseIM.getInstance().getConfigsManager().enableSendChannelAck()) {
            EMConversation conversation = EMClient.getInstance().chatManager().getConversation(conversationId);
            if(conversation == null || conversation.getUnreadMsgCount() <= 0) {
                return;
            }
            try {
                EMClient.getInstance().chatManager().ackConversationRead(conversationId);
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 对方输入状态中止
     * @param handler
     */
    private void setOtherTypingEnd(Handler handler) {
//        if(!turnOnTyping) {
//            return;
//        }
        // Only support single-chat type conversation.
        if (chatType != EaseConstant.CHATTYPE_SINGLE)
            return;
        handler.removeMessages(MSG_OTHER_TYPING_END);
        if(listener != null) {
            listener.onOtherTyping(ACTION_TYPING_END);
        }
    }

    /**
     * 处理“正在输入”开始
     * @param handler
     */
    private void setTypingBeginMsg(Handler handler) {
        if (!turnOnTyping) return;
        // Only support single-chat type conversation.
        if (chatType != EaseConstant.CHATTYPE_SINGLE)
            return;
        // Send TYPING-BEGIN cmd msg
        presenter.sendCmdMessage(ACTION_TYPING_BEGIN);
        handler.sendEmptyMessageDelayed(MSG_TYPING_HEARTBEAT, TYPING_SHOW_TIME);
    }

    /**
     * 处理“正在输入”结束
     * @param handler
     */
    private void setTypingEndMsg(Handler handler) {
        if (!turnOnTyping) return;

        // Only support single-chat type conversation.
        if (chatType != EaseConstant.CHATTYPE_SINGLE)
            return;

        isNotFirstSend = false;
        handler.removeMessages(MSG_TYPING_HEARTBEAT);
        handler.removeMessages(MSG_TYPING_END);
        // Send TYPING-END cmd msg
        //presenter.sendCmdMessage(ACTION_TYPING_END);
    }

    /**
     * 是否是聊天室
     * @return
     */
    public boolean isChatRoomCon() {
        return EaseCommonUtils.getConversationType(chatType) == EMConversation.EMConversationType.ChatRoom;
    }

    /**
     * 是否是群聊
     * @return
     */
    public boolean isGroupCon() {
        return EaseCommonUtils.getConversationType(chatType) == EMConversation.EMConversationType.GroupChat;
    }

    @Override
    public EaseChatMessageListLayout getChatMessageListLayout() {
        return messageListLayout;
    }

    @Override
    public EaseChatInputMenu getChatInputMenu() {
        return inputMenu;
    }

    @Override
    public String getInputContent() {
        return inputMenu.getPrimaryMenu().getEditText().getText().toString().trim();
    }

    @Override
    public void turnOnTypingMonitor(boolean turnOn) {
        this.turnOnTyping = turnOn;
        if(!turnOn) {
            isNotFirstSend = false;
        }
    }

    @Override
    public void sendTextMessage(String content) {
        presenter.sendTextMessage(content);
    }

    @Override
    public void sendTextMessage(String content, boolean isNeedGroupAck) {
        presenter.sendTextMessage(content, isNeedGroupAck);
    }

    @Override
    public void sendAtMessage(String content) {
        presenter.sendAtMessage(content);
    }

    @Override
    public void sendBigExpressionMessage(String name, String identityCode) {
        presenter.sendBigExpressionMessage(name, identityCode);
    }

    @Override
    public void sendVoiceMessage(String filePath, int length) {
        sendVoiceMessage(Uri.parse(filePath), length);
    }

    @Override
    public void sendVoiceMessage(Uri filePath, int length) {
        presenter.sendVoiceMessage(filePath, length);
    }

    @Override
    public void sendImageMessage(Uri imageUri) {
        presenter.sendImageMessage(imageUri);
    }

    @Override
    public void sendImageMessage(Uri imageUri, boolean sendOriginalImage) {
        presenter.sendImageMessage(imageUri, sendOriginalImage);
    }

    @Override
    public void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        presenter.sendLocationMessage(latitude, longitude, locationAddress);
    }

    @Override
    public void sendVideoMessage(Uri videoUri, int videoLength) {
        presenter.sendVideoMessage(videoUri, videoLength);
    }

    @Override
    public void sendFileMessage(Uri fileUri) {
        presenter.sendFileMessage(fileUri);
    }

    @Override
    public void sendMessage(EMMessage message) {
        presenter.sendMessage(message);
    }

    @Override
    public void resendMessage(EMMessage message) {
        EMLog.i(TAG, "resendMessage");
        presenter.resendMessage(message);
    }

    @Override
    public void deleteMessage(EMMessage message) {
        messageListLayout.getCurrentConversation().removeMessage(message.getMsgId());
        messageListLayout.removeMessage(message);
    }

    @Override
    public void recallMessage(EMMessage message) {
        presenter.recallMessage(message);
    }

    @Override
    public void addMessageAttributes(EMMessage message) {
        presenter.addMessageAttributes(message);
    }

    @Override
    public void setOnChatLayoutListener(OnChatLayoutListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnChatRecordTouchListener(OnChatRecordTouchListener recordTouchListener) {
        this.recordTouchListener = recordTouchListener;
    }

    @Override
    public void setOnRecallMessageResultListener(OnRecallMessageResultListener listener) {
        this.recallMessageListener = listener;
    }

    @Override
    public void setOnAddMsgAttrsBeforeSendEvent(OnAddMsgAttrsBeforeSendEvent sendMsgEvent) {
        this.sendMsgEvent = sendMsgEvent;
    }

    /**
     * 发送逻辑：如果正在输入，第一次发送一条cmd消息，然后每隔10s发送一次；
     * 如果停止发送超过10s后，则状态需重置。
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTyping(CharSequence s, int start, int before, int count) {
        if(listener != null) {
            listener.onTextChanged(s, start, before, count);
        }
        if(turnOnTyping) {
            if(typingHandler != null) {
                if(!isNotFirstSend) {
                    isNotFirstSend = true;
                    typingHandler.sendEmptyMessage(MSG_TYPING_HEARTBEAT);
                }
                typingHandler.removeMessages(MSG_TYPING_END);
                typingHandler.sendEmptyMessageDelayed(MSG_TYPING_END, TYPING_SHOW_TIME);
            }
        }
    }

    @Override
    public void onSendMessage(String content) {
        presenter.sendTextMessage(content);
    }

    @Override
    public void onExpressionClicked(Object emojicon) {
        if(emojicon instanceof EaseEmojicon) {
            presenter.sendBigExpressionMessage(((EaseEmojicon) emojicon).getName(), ((EaseEmojicon) emojicon).getIdentityCode());
        }
    }

    @Override
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
        if(recordTouchListener != null && recordTouchListener.onRecordTouch(v, event)) {
            return recordTouchListener.onRecordTouch(v, event);
        }
        return voiceRecorder.onPressToSpeakBtnTouch(v, event, (this::sendVoiceMessage));
    }

    @Override
    public void onChatExtendMenuItemClick(int itemId, View view) {
        if(listener != null) {
            listener.onChatExtendMenuItemClick(view, itemId);
        }
    }

    private EMChatManager getChatManager() {
        return EMClient.getInstance().chatManager();
    }

    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        boolean refresh = false;
        for (EMMessage message : messages) {
            String username = null;
            sendGroupReadAck(message);
            sendReadAck(message);
            // group message
            if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                username = message.getTo();
            } else {
                // single chat message
                username = message.getFrom();
            }
            // if the message is for current conversation
            if (username.equals(conversationId) || message.getTo().equals(conversationId) || message.conversationId().equals(conversationId)) {
                refresh = true;
            }
        }
        if(refresh) {
            getChatMessageListLayout().refreshToLatest();
        }
    }

    /**
     * 发送群组已读回执
     * @param message
     */
    public void sendReadAck(EMMessage message) {
        if(EaseIM.getInstance().getConfigsManager().enableSendChannelAck()) {
            //是接收的消息，未发送过read ack消息且是单聊
            if(message.direct() == EMMessage.Direct.RECEIVE
                    && !message.isAcked()
                    && message.getChatType() == EMMessage.ChatType.Chat) {
                EMMessage.Type type = message.getType();
                //视频，语音及文件需要点击后再发送
                if(type == EMMessage.Type.VIDEO || type == EMMessage.Type.VOICE || type == EMMessage.Type.FILE) {
                    return;
                }
                try {
                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送群组已读回执
     * @param message
     */
    private void sendGroupReadAck(EMMessage message) {
        if(message.isNeedGroupAck() && message.isUnread()) {
            try {
                EMClient.getInstance().chatManager().ackGroupMessageRead(message.getTo(), message.getMsgId(), "");
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接收到正在输入动作的处理逻辑：
     * 如果接收到正在输入的消息，则开始计时，5s内如果没有接收到新的消息，则输入状态结束
     * @param messages
     */
    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
        // 对方是否正在输入的消息回调
        for (final EMMessage msg : messages) {
            final EMCmdMessageBody body = (EMCmdMessageBody) msg.getBody();
            EMLog.i(TAG, "Receive cmd message: " + body.action() + " - " + body.isDeliverOnlineOnly());
            EaseThreadManager.getInstance().runOnMainThread(() -> {
                if(TextUtils.equals(msg.getFrom(), conversationId)) {
                    if(listener != null) {
                        listener.onOtherTyping(body.action());
                    }
                    if(typingHandler != null) {
                        typingHandler.removeMessages(MSG_OTHER_TYPING_END);
                        typingHandler.sendEmptyMessageDelayed(MSG_OTHER_TYPING_END, OTHER_TYPING_SHOW_TIME);
                    }
                }
            });
        }
    }

    @Override
    public void onMessageRead(List<EMMessage> messages) {
        refreshMessages(messages);
    }

    @Override
    public void onMessageDelivered(List<EMMessage> messages) {
        refreshMessages(messages);
    }

    @Override
    public void onMessageRecalled(List<EMMessage> messages) {
        if(getChatMessageListLayout() != null) {
            getChatMessageListLayout().refreshMessages();
        }
    }

    @Override
    public void onMessageChanged(EMMessage message, Object change) {
        refreshMessage(message);
    }

    private void refreshMessage(EMMessage message) {
        if(getChatMessageListLayout() != null) {
            getChatMessageListLayout().refreshMessage(message);
        }
    }

    private void refreshMessages(List<EMMessage> messages) {
        for (EMMessage msg : messages) {
            refreshMessage(msg);
        }
    }

    @Override
    public Context context() {
        return getContext();
    }

    @Override
    public void createThumbFileFail(String message) {
        if(listener != null) {
            listener.onChatError(-1, message);
        }
    }

    @Override
    public void addMsgAttrBeforeSend(EMMessage message) {
        //发送消息前，添加消息属性，比如设置ext
        if(sendMsgEvent != null) {
            sendMsgEvent.addMsgAttrsBeforeSend(message);
        }
    }

    @Override
    public void sendMessageFail(String message) {
        if(listener != null) {
            listener.onChatError(-1, message);
        }
    }

    @Override
    public void sendMessageFinish(EMMessage message) {
        if(getChatMessageListLayout() != null) {
            getChatMessageListLayout().refreshToLatest();
        }
    }

    @Override
    public void deleteLocalMessageSuccess(EMMessage message) {
        messageListLayout.removeMessage(message);
    }

    @Override
    public void recallMessageFinish(EMMessage message) {
        if(recallMessageListener != null) {
            recallMessageListener.recallSuccess(message);
        }
        messageListLayout.refreshMessages();
    }

    @Override
    public void recallMessageFail(int code, String message) {
        if(recallMessageListener != null) {
            recallMessageListener.recallFail(code, message);
        }
        if(listener != null) {
            listener.onChatError(code, message);
        }
    }

    @Override
    public void onTouchItemOutside(View v, int position) {
        inputMenu.hideSoftKeyboard();
        inputMenu.showExtendMenu(false);
    }

    @Override
    public void onViewDragging() {
        inputMenu.hideSoftKeyboard();
        inputMenu.showExtendMenu(false);
    }

    @Override
    public boolean onBubbleClick(EMMessage message) {
        if(listener != null) {
            return listener.onBubbleClick(message);
        }
        return false;
    }

    @Override
    public boolean onResendClick(EMMessage message) {
        EMLog.i(TAG, "onResendClick");
        new EaseAlertDialog(getContext(), R.string.resend, R.string.confirm_resend, null, new EaseAlertDialog.AlertDialogUser() {
            @Override
            public void onResult(boolean confirmed, Bundle bundle) {
                if (!confirmed) {
                    return;
                }
                resendMessage(message);
            }
        }, true).show();
        return true;
    }

    @Override
    public boolean onBubbleLongClick(View v, EMMessage message) {
        if(showDefaultMenu) {
            showDefaultMenu(v, message);
            if(listener != null) {
                return listener.onBubbleLongClick(v, message);
            }
            return true;
        }
        if(listener != null) {
            return listener.onBubbleLongClick(v, message);
        }
        return false;
    }

    @Override
    public void onUserAvatarClick(String username) {
        if(listener != null) {
            listener.onUserAvatarClick(username);
        }
    }

    @Override
    public void onUserAvatarLongClick(String username) {
        EMLog.i(TAG, "onUserAvatarLongClick");
        inputAtUsername(username, true);
        if(listener != null) {
            listener.onUserAvatarLongClick(username);
        }
    }

    @Override
    public void onMessageCreate(EMMessage message) {
        //发送失败的消息再次进行发送
        // send message
        EMLog.i(TAG, "onMessageCreate");
        //sendMessage(message);
    }

    @Override
    public void onMessageSuccess(EMMessage message) {
        EMLog.i(TAG, "send message onMessageSuccess");
    }

    @Override
    public void onMessageError(EMMessage message, int code, String error) {
        if(listener != null) {
            listener.onChatError(code, error);
        }
    }

    @Override
    public void onMessageInProgress(EMMessage message, int progress) {
        EMLog.i(TAG, "send message on progress");

    }

    @Override
    public void onChatError(int code, String errorMsg) {
        if(listener != null) {
            listener.onChatError(code, errorMsg);
        }
    }

    @Override
    public void showItemDefaultMenu(boolean showDefault) {
        this.showDefaultMenu = showDefault;
    }

    @Override
    public void clearMenu() {
        menuHelper.clear();
    }

    @Override
    public void addItemMenu(MenuItemBean item) {
        menuHelper.addItemMenu(item);
    }

    @Override
    public void addItemMenu(int groupId, int itemId, int order, String title) {
        menuHelper.addItemMenu(groupId, itemId, order, title);
    }

    @Override
    public MenuItemBean findItem(int id) {
        return menuHelper.findItem(id);
    }

    @Override
    public void findItemVisible(int id, boolean visible) {
        menuHelper.findItemVisible(id, visible);
    }

    @Override
    public EasePopupWindowHelper getMenuHelper() {
        return menuHelper;
    }

    @Override
    public void setOnPopupWindowItemClickListener(OnMenuChangeListener listener) {
        this.menuChangeListener = listener;
    }

    /**
     * input @
     * only for group chat
     * @param username
     */
    public void inputAtUsername(String username, boolean autoAddAtSymbol){
        if(EMClient.getInstance().getCurrentUser().equals(username) ||
                !messageListLayout.isGroupChat()){
            return;
        }
        EaseAtMessageHelper.get().addAtUser(username);
        EaseUser user = EaseUserUtils.getUserInfo(username);
        if (user != null){
            username = user.getNickname();
        }
        EditText editText = inputMenu.getPrimaryMenu().getEditText();
        if(autoAddAtSymbol)
            insertText(editText, AT_PREFIX + username + AT_SUFFIX);
        else
            insertText(editText, username + AT_SUFFIX);
    }

    /**
     * insert text to EditText
     * @param edit
     * @param text
     */
    private void insertText(EditText edit, String text) {
        if(edit.isFocused()) {
            edit.getText().insert(edit.getSelectionStart(), text);
        }else {
            edit.getText().insert(edit.getText().length() - 1, text);
        }
    }

    private void showDefaultMenu(View v, EMMessage message) {
        menuHelper.initMenu(getContext());
        menuHelper.setDefaultMenus();
        menuHelper.setOutsideTouchable(true);
        setMenuByMsgType(message);
        if(menuChangeListener != null) {
            menuChangeListener.onPreMenu(menuHelper, message);
        }
        menuHelper.setOnPopupMenuItemClickListener(new EasePopupWindow.OnPopupWindowItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItemBean item) {
                if(menuChangeListener != null && menuChangeListener.onMenuItemClick(item, message)) {
                    return true;
                }
                if(showDefaultMenu) {
                    int itemId = item.getItemId();
                    if(itemId == R.id.action_chat_copy) {
                        clipboard.setPrimaryClip(ClipData.newPlainText(null,
                                ((EMTextMessageBody) message.getBody()).getMessage()));
                        EMLog.i(TAG, "copy success");
                    }else if(itemId == R.id.action_chat_delete) {
                        deleteMessage(message);
                        EMLog.i(TAG,"currentMsgId = "+message.getMsgId() + " timestamp = "+message.getMsgTime());
                    }else if(itemId == R.id.action_chat_recall) {
                        recallMessage(message);
                    }
                    return true;
                }
                return false;
            }
        });
        menuHelper.setOnPopupMenuDismissListener(new EasePopupWindow.OnPopupWindowDismissListener() {
            @Override
            public void onDismiss(PopupWindow menu) {
                if(menuChangeListener != null) {
                    menuChangeListener.onDismiss(menu);
                }
            }
        });
        menuHelper.show(this, v);
    }

    private void setMenuByMsgType(EMMessage message) {
        EMMessage.Type type = message.getType();
        menuHelper.findItemVisible(R.id.action_chat_copy, false);
        menuHelper.findItemVisible(R.id.action_chat_recall, false);
        menuHelper.findItem(R.id.action_chat_delete).setTitle(getContext().getString(R.string.action_delete));
        switch (type) {
            case TXT:
                menuHelper.findItemVisible(R.id.action_chat_copy, true);
                menuHelper.findItemVisible(R.id.action_chat_recall, true);
                break;
            case LOCATION:
            case FILE:
            case IMAGE:
                menuHelper.findItemVisible(R.id.action_chat_recall, true);
                break;
            case VOICE:
                menuHelper.findItem(R.id.action_chat_delete).setTitle(getContext().getString(R.string.delete_voice));
                menuHelper.findItemVisible(R.id.action_chat_recall, true);
                break;
            case VIDEO:
                menuHelper.findItem(R.id.action_chat_delete).setTitle(getContext().getString(R.string.delete_video));
                menuHelper.findItemVisible(R.id.action_chat_recall, true);
                break;
        }

        if(message.direct() == EMMessage.Direct.RECEIVE ){
            menuHelper.findItemVisible(R.id.action_chat_recall, false);
        }
    }

    private class ChatRoomListener extends EaseChatRoomListener {

        @Override
        public void onChatRoomDestroyed(String roomId, String roomName) {
            finishCurrent();
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            if(!TextUtils.equals(roomId, conversationId)) {
                return;
            }
            if(reason == EMAChatRoomManagerListener.BE_KICKED) {
                finishCurrent();
            }
        }

        @Override
        public void onMemberJoined(String roomId, String participant) {

        }

        @Override
        public void onMemberExited(String roomId, String roomName, String participant) {

        }
    }

    /**
     * group listener
     */
    private class GroupListener extends EaseGroupListener {

        @Override
        public void onUserRemoved(String groupId, String groupName) {
            finishCurrent();
        }

        @Override
        public void onGroupDestroyed(String groupId, String groupName) {
            finishCurrent();
        }
    }

    /**
     * finish current activity
     */
    private void finishCurrent() {
        if(getContext() instanceof Activity) {
            ((Activity) getContext()).finish();
        }
    }

}

