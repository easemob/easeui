package com.hyphenate.easeui.modules.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConversationListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMTranslationResult;
import com.hyphenate.chat.adapter.EMAChatRoomManagerListener;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.interfaces.ChatQuoteMessageProvider;
import com.hyphenate.easeui.interfaces.EaseChatRoomListener;
import com.hyphenate.easeui.interfaces.EaseGroupListener;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.manager.EaseAtMessageHelper;
import com.hyphenate.easeui.manager.EaseChatInterfaceManager;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.hyphenate.easeui.modules.chat.interfaces.ChatInputMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatLayout;
import com.hyphenate.easeui.modules.chat.interfaces.OnAddMsgAttrsBeforeSendEvent;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatFinishListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatLayoutListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatRecordTouchListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnModifyMessageListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnMenuChangeListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnRecallMessageResultListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnTranslateMessageListener;
import com.hyphenate.easeui.modules.chat.presenter.EaseHandleMessagePresenter;
import com.hyphenate.easeui.modules.chat.presenter.EaseHandleMessagePresenterImpl;
import com.hyphenate.easeui.modules.chat.presenter.IHandleMessageView;
import com.hyphenate.easeui.modules.interfaces.IPopupWindow;
import com.hyphenate.easeui.modules.menu.EaseChatFinishReason;
import com.hyphenate.easeui.modules.menu.EasePopupWindow;
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper;
import com.hyphenate.easeui.modules.menu.MenuItemBean;
import com.hyphenate.easeui.ui.EaseBaiduMapActivity;
import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import com.hyphenate.easeui.ui.EaseShowNormalFileActivity;
import com.hyphenate.easeui.ui.EaseShowVideoActivity;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseCompat;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseChatQuoteView;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EaseChatLayout extends RelativeLayout implements IChatLayout, IHandleMessageView, IPopupWindow
        , ChatInputMenuListener, EMMessageListener, EaseChatMessageListLayout.OnMessageTouchListener
        , MessageListItemClickListener, EaseChatMessageListLayout.OnChatErrorListener, LifecycleObserver {
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
    private boolean isReportYourSelf = false;
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
    /**
     * 翻译监听
     */
    private OnTranslateMessageListener translateListener;
    /**
     * 会话结束监听
     */
    private OnChatFinishListener chatFinishListener;
    /**
     * 翻译目标语言，默认英文
     */
    private String targetLanguageCode = "en";
    /**
     * 编辑消息的监听
     */
    private OnModifyMessageListener modifyMessageListener;
    /**
     * Quote message provider.
     */
    private ChatQuoteMessageProvider quoteMessageProvider;
    private JSONObject quoteObject = null;
    private boolean isQuote;
    private int retrievalSize = 100;
    ;

    public EaseChatLayout(Context context) {
        this(context, null);
    }

    public EaseChatLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        presenter = new EaseHandleMessagePresenterImpl();
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(this);
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
        EaseChatInterfaceManager.getInstance().setInterface(EaseConstant.INTERFACE_QUOTE_MESSAGE_TAG, new EaseChatQuoteView.IChatQuoteMessageShow() {
            @Override
            public SpannableString itemQuoteMessageShow(EMMessage quoteMessage, EMMessage.Type quoteMsgType, String quoteSender, String quoteContent) {
                if(quoteMessageProvider != null) {
                    return quoteMessageProvider.providerQuoteMessageContent(quoteMessage, quoteMsgType, quoteSender, quoteContent);
                }
                return null;
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        Log.i(TAG, this.toString() +" onResume");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getChatManager().removeMessageListener(this);
        if (chatRoomListener != null) {
            EMClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomListener);
        }
        if (groupListener != null) {
            EMClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
        }
        if (isChatRoomCon()) {
            EMClient.getInstance().chatroomManager().leaveChatRoom(conversationId);
        }
        if (isGroupCon()) {
            EaseAtMessageHelper.get().removeAtMeGroup(conversationId);
            EaseAtMessageHelper.get().cleanToAtUserList();
        }
        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }
        EaseChatInterfaceManager.getInstance().removeInterface(EaseConstant.INTERFACE_QUOTE_MESSAGE_TAG);
    }

    /**
     * 初始化
     *
     * @param username 环信id
     * @param chatType 聊天类型，单聊，群聊或者聊天室
     */
    public void init(String username, int chatType) {
        init(EaseChatMessageListLayout.LoadDataType.LOCAL, username, chatType);
    }

    /**
     * 初始化
     *
     * @param loadDataType   加载数据模式
     * @param conversationId 会话id，可能是对方环信id，也可能是群id或者聊天室id
     * @param chatType       聊天类型，单聊，群聊或者聊天室
     */
    public void init(EaseChatMessageListLayout.LoadDataType loadDataType, String conversationId, int chatType) {
        this.conversationId = conversationId;
        this.chatType = chatType;
        messageListLayout.init(loadDataType, this.conversationId, chatType);
        presenter.setupWithToUser(chatType, this.conversationId);
        if (isChatRoomCon()) {
            chatRoomListener = new ChatRoomListener();
            EMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomListener);
        } else if (isGroupCon()) {
            EaseAtMessageHelper.get().removeAtMeGroup(conversationId);
            groupListener = new GroupListener();
            EMClient.getInstance().groupManager().addGroupChangeListener(groupListener);
        }
        initTypingHandler();
    }

    /**
     * 初始化历史消息搜索模式
     *
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

    public void setTargetLanguageCode(String lanugageCode) {
        targetLanguageCode = lanugageCode;
    }

    private void initTypingHandler() {
        typingHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_TYPING_HEARTBEAT:
                        setTypingBeginMsg(this);
                        break;
                    case MSG_TYPING_END:
                        setTypingEndMsg(this);
                        break;
                    case MSG_OTHER_TYPING_END:
                        setOtherTypingEnd(this);
                        break;
                }
            }
        };
        if (!turnOnTyping) {
            if (typingHandler != null) {
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
        if (EaseIM.getInstance().getConfigsManager().enableSendChannelAck()) {
            EMConversation conversation = EMClient.getInstance().chatManager().getConversation(conversationId);
            if (conversation == null || conversation.getUnreadMsgCount() <= 0) {
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
     *
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
        if (listener != null) {
            listener.onOtherTyping(ACTION_TYPING_END);
        }
    }

    /**
     * 处理“正在输入”开始
     *
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
     *
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
     *
     * @return
     */
    public boolean isChatRoomCon() {
        return EaseCommonUtils.getConversationType(chatType) == EMConversation.EMConversationType.ChatRoom;
    }

    /**
     * 是否是群聊
     *
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
        if (!turnOn) {
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
        boolean sendOriginalImage = false;
        try {
            sendOriginalImage = getResources().getBoolean(R.bool.ease_enable_send_origin_image);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        presenter.sendImageMessage(imageUri, sendOriginalImage);
    }

    @Override
    public void sendImageMessage(Uri imageUri, boolean sendOriginalImage) {
        presenter.sendImageMessage(imageUri, sendOriginalImage);
    }

    @Override
    public void sendLocationMessage(double latitude, double longitude, String locationAddress, String buildingName) {
        presenter.sendLocationMessage(latitude, longitude, locationAddress, buildingName);
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
    public void modifyMessage(String messageId, EMMessageBody messageBodyModified) {
        presenter.modifyMessage(messageId,messageBodyModified);
    }

    @Override
    public void translateMessage(EMMessage message, boolean isTranslate) {
        presenter.translateMessage(message, targetLanguageCode, isTranslate);
    }

    @Override
    public void hideTranslate(EMMessage message) {
        presenter.hideTranslate(message);
        messageListLayout.refreshMessage(message);
    }

    @Override
    public void setMessageQuoteInfo(JSONObject quoteInfo) {
        isQuote = true;
        quoteObject = quoteInfo;
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

    @Override
    public void setOnTranslateListener(OnTranslateMessageListener translateListener) {
        this.translateListener = translateListener;
    }

    @Override
    public void setOnChatFinishListener(OnChatFinishListener listener) {
        this.chatFinishListener = listener;
    }

    @Override
    public void setOnEditMessageListener(OnModifyMessageListener listener) {
        this.modifyMessageListener = listener;
    }

    @Override
    public void setChatQuoteMessageProvider(ChatQuoteMessageProvider provider) {
        this.quoteMessageProvider = provider;
    }

    /**
     * 发送逻辑：如果正在输入，第一次发送一条cmd消息，然后每隔10s发送一次；
     * 如果停止发送超过10s后，则状态需重置。
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTyping(CharSequence s, int start, int before, int count) {
        if (listener != null) {
            listener.onTextChanged(s, start, before, count);
        }
        if (turnOnTyping) {
            if (typingHandler != null) {
                if (!isNotFirstSend) {
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
    public void showCustomQuote(EMMessage message) {
       if (listener != null){
           listener.showCustomQuote(message);
       }
    }

    @Override
    public void onExpressionClicked(Object emojicon) {
        if (emojicon instanceof EaseEmojicon) {
            presenter.sendBigExpressionMessage(((EaseEmojicon) emojicon).getName(), ((EaseEmojicon) emojicon).getIdentityCode());
        }
    }

    @Override
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
        if (recordTouchListener != null) {
            boolean onRecordTouch = recordTouchListener.onRecordTouch(v, event);
            if (!onRecordTouch) {
                return false;
            }
        }
        return voiceRecorder.onPressToSpeakBtnTouch(v, event, (this::sendVoiceMessage));
    }

    @Override
    public void onChatExtendMenuItemClick(int itemId, View view) {
        if (listener != null) {
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
        if (refresh) {
            getChatMessageListLayout().refreshToLatest();
        }
    }

    /**
     * 发送群组已读回执
     *
     * @param message
     */
    public void sendReadAck(EMMessage message) {
        if (EaseIM.getInstance().getConfigsManager().enableSendChannelAck()) {
            //是接收的消息，未发送过read ack消息且是单聊
            if (message.direct() == EMMessage.Direct.RECEIVE
                    && !message.isAcked()
                    && message.getChatType() == EMMessage.ChatType.Chat) {
                EMMessage.Type type = message.getType();
                //视频，语音及文件需要点击后再发送
                if (type == EMMessage.Type.VIDEO || type == EMMessage.Type.VOICE || type == EMMessage.Type.FILE) {
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
     *
     * @param message
     */
    private void sendGroupReadAck(EMMessage message) {
        if (message.isNeedGroupAck() && message.isUnread()) {
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
     *
     * @param messages
     */
    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
        // 对方是否正在输入的消息回调
        for (final EMMessage msg : messages) {
            final EMCmdMessageBody body = (EMCmdMessageBody) msg.getBody();
            EMLog.i(TAG, "Receive cmd message: " + body.action() + " - " + body.isDeliverOnlineOnly());
            EaseThreadManager.getInstance().runOnMainThread(() -> {
                if (TextUtils.equals(msg.getFrom(), conversationId)) {
                    if (listener != null) {
                        listener.onOtherTyping(body.action());
                    }
                    if (typingHandler != null) {
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
        if (getChatMessageListLayout() != null) {
            getChatMessageListLayout().refreshMessages();
        }
    }

    @Override
    public void onMessageChanged(EMMessage message, Object change) {
        refreshMessage(message);
    }

    private void refreshMessage(EMMessage message) {
        if (getChatMessageListLayout() != null) {
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
        if (listener != null) {
            listener.onChatError(-1, message);
        }
    }

    @Override
    public void addMsgAttrBeforeSend(EMMessage message) {
        if (message.getType() == EMMessage.Type.TXT && isQuote){
            message.setAttribute(EaseConstant.QUOTE_MSG_QUOTE, quoteObject);
        }
        //发送消息前，添加消息属性，比如设置ext
        if (sendMsgEvent != null) {
            sendMsgEvent.addMsgAttrsBeforeSend(message);
        }
    }

    @Override
    public void sendMessageFail(String message) {
        if (listener != null) {
            listener.onChatError(-1, message);
        }
    }

    @Override
    public void sendMessageFinish(EMMessage message) {
        if (getChatMessageListLayout() != null) {
            getChatMessageListLayout().refreshToLatest();
        }
    }

    @Override
    public void deleteLocalMessageSuccess(EMMessage message) {
        messageListLayout.removeMessage(message);
    }

    @Override
    public void recallMessageFinish(EMMessage message) {
        if (recallMessageListener != null) {
            recallMessageListener.recallSuccess(message);
        }
        messageListLayout.refreshMessages();
    }

    @Override
    public void recallMessageFail(int code, String message) {
        if (recallMessageListener != null) {
            recallMessageListener.recallFail(code, message);
        }
        if (listener != null) {
            listener.onChatError(code, message);
        }
    }

    @Override
    public void onPresenterMessageSuccess(EMMessage message) {
        isQuote = false;
        EMLog.i(TAG, "send message onPresenterMessageSuccess");
        if (listener != null) {
            listener.onChatSuccess(message);
        }
    }

    @Override
    public void onPresenterMessageError(EMMessage message, int code, String error) {
        EMLog.i(TAG, "send message onPresenterMessageError code: " + code + " error: " + error);
        //刷新条目
        refreshMessage(message);
        if (listener != null) {
            listener.onChatError(code, error);
        }
    }

    @Override
    public void onPresenterMessageInProgress(EMMessage message, int progress) {
        EMLog.i(TAG, "send message onPresenterMessageInProgress");
    }

    @Override
    public void translateMessageSuccess(EMMessage message) {
        EMLog.i(TAG, "translateMessageSuccess");
        messageListLayout.lastMsgScrollToBottom(message);
        if (translateListener != null) {
            translateListener.translateMessageSuccess(message);
        }
    }

    @Override
    public void translateMessageFail(EMMessage message, int code, String error) {
        EMLog.i(TAG, "translateMessageFail:" + code + ":" + error);
        if (translateListener != null) {
            translateListener.translateMessageFail(message, code, error);
        }
    }

    @Override
    public void onModifyMessageSuccess(String messageId) {
        EMMessage message = getChatManager().getMessage(messageId);
        refreshMessage(message);
        if (modifyMessageListener != null) {
            modifyMessageListener.onModifyMessageSuccess(messageId);
        }
    }

    @Override
    public void onModifyMessageFailure(String messageId, int code, String error) {
        EMLog.i(TAG, "onModifyMessageFailure:" + code + ":" + error);
        if (modifyMessageListener != null) {
            modifyMessageListener.onModifyMessageFailure(messageId,code,error);
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
        if (listener != null) {
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
        if (showDefaultMenu) {
            showDefaultMenu(v, message);
            if (listener != null) {
                return listener.onBubbleLongClick(v, message);
            }
            return true;
        }
        if (listener != null) {
            return listener.onBubbleLongClick(v, message);
        }
        return false;
    }

    @Override
    public void onQuoteViewClick(EMMessage message) {
        EMLog.d(TAG,"onQuoteViewClick: " + message);
        if (listener != null && listener.onQuoteClick(message)){
            return;
        }
        if(message == null) {
            EMLog.d(TAG,"onQuoteViewClick: message is null.");
            return;
        }
        if(message.getType() == EMMessage.Type.IMAGE || message.getType() == EMMessage.Type.VIDEO || message.getType() == EMMessage.Type.FILE) {
            showQuoteByType(message);
            return;
        }
        EMConversation currentConversation = getChatMessageListLayout().getCurrentConversation();
        //如果是文本类型或者语音类型消息 先在当前缓存消息中查看是否能找到
        int size = getChatMessageListLayout().getMessageAdapter().getData().size();
        int position = getChatMessageListLayout().getMessageAdapter().getData().lastIndexOf(message);
        //如果找不到 在从db加载数据 之后再查询
        if(position == -1){
            getChatMessageListLayout().loadMorePreviousData(retrievalSize, new EMCallBack() {
                @Override
                public void onSuccess() {
                    List<EMMessage> currentData = currentConversation.getAllMessages();
                    if (currentData != null && currentData.size() > 0){
                        int dataSize = currentData.size();
                        int position = getChatMessageListLayout().getMessageAdapter().getData().lastIndexOf(message);
                        //如果查到了
                        if (position != -1){
                            //如果 position 再限制条数以内 则直接跳转指定位置
                            if (position - (dataSize - retrievalSize)  > 0){
                                getChatMessageListLayout().moveToPosition(position);
                                getChatMessageListLayout().highlightItem(position);
                            }else {
                                EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getContext() != null){
                                            Toast.makeText(getContext(),getContext().getString(R.string.quote_limitation),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            //如果还没查到
                        }else{
                            EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (getContext() != null){
                                        Toast.makeText(getContext(),getContext().getString(R.string.quote_not_found),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                public void onError(int code, String error) {}
            });
        }else {
            //如果 position 再限制条数以内 则直接跳转指定位置
            if (position - ( size - retrievalSize)  > 0){
                getChatMessageListLayout().moveToPosition(position);
                getChatMessageListLayout().highlightItem(position);
            }else {
                EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getContext() != null){
                            Toast.makeText(getContext(),getContext().getString(R.string.quote_limitation),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    /**
     * 设置检索条数 建议不超过200
     * @param pageSize
     */
    public void setRetrievalSize(int pageSize){
        if (pageSize > 200){
            this.retrievalSize = 200;
        }else {
            this.retrievalSize = pageSize;
        }
    }

    public void showQuoteByType(EMMessage message){
        EMMessage.Type type = message.getType();
        //文本、语音类型引用消息跳转  图片（自定义表情）、视频、文件直接展示
        switch (type){
            case IMAGE:
                EMImageMessageBody imgBody = (EMImageMessageBody) message.getBody();
                Intent imageIntent = new Intent(getContext(), EaseShowBigImageActivity.class);
                Uri imgUri = imgBody.getLocalUri();
                //检查Uri读权限
                EaseFileUtils.takePersistableUriPermission(getContext(), imgUri);
                if(EaseFileUtils.isFileExistByUri(getContext(), imgUri)) {
                    imageIntent.putExtra("uri", imgUri);
                } else{
                    String msgId = message.getMsgId();
                    imageIntent.putExtra("messageId", msgId);
                    imageIntent.putExtra("filename", imgBody.getFileName());
                }
                if (getContext() != null){
                    getContext().startActivity(imageIntent);
                }
                break;
            case VIDEO:
                Intent videoIntent = new Intent(getContext(), EaseShowVideoActivity.class);
                videoIntent.putExtra("msg", message);
                if (getContext() != null){
                    getContext().startActivity(videoIntent);
                }
                break;
            case FILE:
                EMNormalFileMessageBody fileMessageBody = (EMNormalFileMessageBody) message.getBody();
                Uri filePath = fileMessageBody.getLocalUri();
                //检查Uri读权限
                EaseFileUtils.takePersistableUriPermission(getContext(), filePath);
                if(EaseFileUtils.isFileExistByUri(getContext(), filePath)){
                    EaseCompat.openFile(getContext(), filePath);
                } else {
                    if (getContext() != null){
                        getContext().startActivity(new Intent(getContext(), EaseShowNormalFileActivity.class).putExtra("msg", message));
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onQuoteViewLongClick(View v, EMMessage message) {
        if (listener != null){
            return listener.onQuoteLongClick(v,message);
        }
        return false;
    }


    @Override
    public void onUserAvatarClick(String username) {
        if (listener != null) {
            listener.onUserAvatarClick(username);
        }
    }

    @Override
    public void onUserAvatarLongClick(String username) {
        EMLog.i(TAG, "onUserAvatarLongClick");
        inputAtUsername(username, true);
        if (listener != null) {
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
        isQuote = false;
        EMLog.i(TAG, "send message onMessageSuccess");
        if (listener != null) {
            listener.onChatSuccess(message);
        }
    }

    @Override
    public void onMessageError(EMMessage message, int code, String error) {
        EMLog.i(TAG, "send message onMessageError");
        if(listener != null) {
            listener.onChatError(code, error);
        }
    }

    @Override
    public void onMessageInProgress(EMMessage message, int progress) {
        EMLog.i(TAG, "send message on progress: " + progress);

    }

    @Override
    public void onChatError(int code, String errorMsg) {
        if (listener != null) {
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
     *
     * @param username
     */
    public void inputAtUsername(String username, boolean autoAddAtSymbol) {
        if (EMClient.getInstance().getCurrentUser().equals(username) ||
                !messageListLayout.isGroupChat()) {
            return;
        }
        EaseAtMessageHelper.get().addAtUser(username);
        EaseUser user = EaseUserUtils.getUserInfo(username);
        if (user != null) {
            username = user.getNickname();
        }
        EditText editText = inputMenu.getPrimaryMenu().getEditText();
        if (autoAddAtSymbol)
            insertText(editText, AT_PREFIX + username + AT_SUFFIX);
        else
            insertText(editText, username + AT_SUFFIX);
    }

    /**
     * insert text to EditText
     *
     * @param edit
     * @param text
     */
    private void insertText(EditText edit, String text) {
        if (edit.isFocused()) {
            edit.getText().insert(edit.getSelectionStart(), text);
        } else {
            edit.getText().insert(edit.getText().length() - 1, text);
        }
    }

    private void showDefaultMenu(View v, EMMessage message) {
        menuHelper.initMenu(getContext());
        menuHelper.setDefaultMenus();
        menuHelper.setOutsideTouchable(true);
        setMenuByMsgType(v, message);
        if (menuChangeListener != null) {
            menuChangeListener.onPreMenu(menuHelper, message, v);
        }
        menuHelper.setOnPopupMenuItemClickListener(new EasePopupWindow.OnPopupWindowItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItemBean item) {
                if (menuChangeListener != null && menuChangeListener.onMenuItemClick(item, message)) {
                    return true;
                }
                if (showDefaultMenu) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_chat_copy) {
                        clipboard.setPrimaryClip(ClipData.newPlainText(null,
                                ((EMTextMessageBody) message.getBody()).getMessage()));
                        EMLog.i(TAG, "copy success");
                    } else if (itemId == R.id.action_chat_delete) {
                        deleteMessage(message);
                        EMLog.i(TAG, "currentMsgId = " + message.getMsgId() + " timestamp = " + message.getMsgTime());
                    } else if (itemId == R.id.action_chat_recall) {
                        recallMessage(message);
                    } else if (itemId == R.id.action_chat_hide) {
                        hideTranslate(message);
                    } else if(itemId == R.id.action_chat_quote) {
                        onQuoteMenuItemClick(message);
                    }
                    return true;
                }
                return false;
            }
        });
        menuHelper.setOnPopupMenuDismissListener(new EasePopupWindow.OnPopupWindowDismissListener() {
            @Override
            public void onDismiss(PopupWindow menu) {
                if (menuChangeListener != null) {
                    menuChangeListener.onDismiss(menu);
                }
            }
        });
        menuHelper.show(this, v);
    }

    public void onQuoteMenuItemClick(EMMessage message){
        isQuote = true;
        quoteObject = new JSONObject();
        try {
            if (message.getBody() != null){
                quoteObject.put(EaseConstant.QUOTE_MSG_ID,message.getMsgId());
                if (message.getType() == EMMessage.Type.TXT && !TextUtils.isEmpty(((EMTextMessageBody)message.getBody()).getMessage())){
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,((EMTextMessageBody)message.getBody()).getMessage());
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"txt");
                }else if (message.getType() == EMMessage.Type.IMAGE){
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,getResources().getString(R.string.quote_image));
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"img");
                }else if (message.getType() == EMMessage.Type.VIDEO){
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,getResources().getString(R.string.quote_video));
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"video");
                }else if (message.getType() == EMMessage.Type.LOCATION){
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,getResources().getString(R.string.quote_location));
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"location");
                }else if (message.getType() == EMMessage.Type.VOICE){
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,getResources().getString(R.string.quote_voice));
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"audio");
                }else if (message.getType() == EMMessage.Type.FILE){
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,getResources().getString(R.string.quote_file));
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"file");
                }else if (message.getType() == EMMessage.Type.CUSTOM){
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,getResources().getString(R.string.custom));
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"custom");
                }else {
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,"");
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE, message.getType().name().toLowerCase());
                }
                quoteObject.put(EaseConstant.QUOTE_MSG_SENDER,message.getFrom());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getChatInputMenu().getPrimaryMenu().primaryStartQuote(message);
    }

    private boolean showTranslation(EMMessage message) {
        if (!EMClient.getInstance().translationManager().isInitialized())
            return false;

        if (!EMClient.getInstance().translationManager().isTranslationResultForMessage(message.getMsgId()))
            return true;

        EMTranslationResult result = EMClient.getInstance().translationManager().getTranslationResult(message.getMsgId());
        if (result.showTranslation())
            return false;

        return true;
    }

    private void setMenuByMsgType(View v, EMMessage message) {
        EMMessage.Type type = message.getType();
        menuHelper.findItemVisible(R.id.action_chat_copy, false);
        menuHelper.findItemVisible(R.id.action_chat_recall, false);
        menuHelper.findItemVisible(R.id.action_chat_translate, false);
        menuHelper.findItemVisible(R.id.action_chat_reTranslate, false);
        menuHelper.findItemVisible(R.id.action_chat_hide, false);
        menuHelper.findItemVisible(R.id.action_chat_quote, message.status() == EMMessage.Status.SUCCESS);
        menuHelper.findItem(R.id.action_chat_delete).setTitle(getContext().getString(R.string.action_delete));
        menuHelper.findItemVisible(com.hyphenate.easeui.R.id.action_chat_label, true);
        if (!isReportYourSelf) {
            menuHelper.findItemVisible(com.hyphenate.easeui.R.id.action_chat_label, message.direct() == EMMessage.Direct.RECEIVE ? true : false);
        }
        switch (type) {
            case TXT:
                EMTranslationResult result = EMClient.getInstance().translationManager().getTranslationResult(message.getMsgId());
                if (v.getId() == R.id.subBubble && result != null) {
                    menuHelper.findItemVisible(R.id.action_chat_delete, false);

                    if (result.translateCount() < 2)
                        menuHelper.findItemVisible(R.id.action_chat_reTranslate, true);

                    menuHelper.findItemVisible(R.id.action_chat_hide, true);
                } else {
                    menuHelper.findItemVisible(R.id.action_chat_copy, true);
                    menuHelper.findItemVisible(R.id.action_chat_recall, true);
                    menuHelper.findItemVisible(R.id.action_chat_delete, true);
                    if (showTranslation(message))
                        menuHelper.findItemVisible(R.id.action_chat_translate, true);
                }
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

        if (message.direct() == EMMessage.Direct.RECEIVE) {
            menuHelper.findItemVisible(R.id.action_chat_recall, false);
        }
    }

    private class ChatRoomListener extends EaseChatRoomListener {

        @Override
        public void onChatRoomDestroyed(String roomId, String roomName) {
            finishCurrent(EaseChatFinishReason.onChatRoomDestroyed, roomId);
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            if (!TextUtils.equals(roomId, conversationId)) {
                return;
            }
            if (reason == EMAChatRoomManagerListener.BE_KICKED) {
                finishCurrent(EaseChatFinishReason.onChatRoomUserRemoved, roomId);
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
            finishCurrent(EaseChatFinishReason.onGroupUserRemoved, groupId);
        }

        @Override
        public void onGroupDestroyed(String groupId, String groupName) {
            finishCurrent(EaseChatFinishReason.onGroupDestroyed, groupId);
        }
    }

    /**
     * finish current activity
     *
     * @param reason
     * @param id
     */
    private void finishCurrent(EaseChatFinishReason reason, String id) {
        if (chatFinishListener != null) {
            chatFinishListener.onChatFinish(reason, id);
        }
    }

    /**
     * Set whether you are allowed to report your own messages
     */
    public void setReportYourSelf(boolean isReport) {
        this.isReportYourSelf = isReport;
    }

}

