package com.hyphenate.easeui.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.adapter.EMAChatRoomManagerListener;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.interfaces.EaseChatRoomListener;
import com.hyphenate.easeui.interfaces.EaseGroupListener;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.EaseAtMessageHelper;
import com.hyphenate.easeui.model.EaseCompat;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.ui.EaseBaiduMapActivity;
import com.hyphenate.easeui.ui.base.EaseBaseFragment;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseDingMessageHelper;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseChatExtendMenu;
import com.hyphenate.easeui.widget.EaseChatInputMenu;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;
import com.hyphenate.util.VersionUtils;

import java.io.File;
import java.util.List;

public class EaseChatFragment extends EaseBaseFragment implements View.OnClickListener,
        EaseChatInputMenu.ChatInputMenuListener,
        EaseChatExtendMenu.EaseChatExtendMenuItemClickListener, MessageListItemClickListener,
        EMCallBack, EMMessageListener, TextWatcher, EaseChatMessageList.OnMessageListListener {

    private static final String TAG = EaseChatFragment.class.getSimpleName();

    protected static final int MSG_TYPING_BEGIN = 0;
    protected static final int MSG_TYPING_END = 1;

    protected static final String ACTION_TYPING_BEGIN = "TypingBegin";
    protected static final String ACTION_TYPING_END = "TypingEnd";
    protected static final int TYPING_SHOW_TIME = 5000;

    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected static final int REQUEST_CODE_DING_MSG = 4;
    protected static final int REQUEST_CODE_SELECT_VIDEO = 11;
    protected static final int REQUEST_CODE_SELECT_FILE = 12;

    protected TextView tvErrorMsg;
    protected EaseChatMessageList chatMessageList;
    protected EaseChatInputMenu inputMenu;
    protected EaseVoiceRecorderView voiceRecorderView;

    /**
     * 消息类别，自定义
     */
    protected int chatType = EaseConstant.CHATTYPE_SINGLE;
    /**
     * 转发消息的消息id
     */
    private String forwardMsgId;
    /**
     * 历史消息id（搜索时用）
     */
    private String historyMsgId;
    /**
     * "正在输入"功能的开关，打开后本设备发送消息将持续发送cmd类型消息通知对方"正在输入"
     */
    private boolean turnOnTyping;
    /**
     * 消息类别，SDK定义
     */
    protected String toChatUsername;
    protected File cameraFile;
    /**
     * chat conversation
     */
    protected EMConversation conversation;
    protected boolean isRoaming;
    /**
     * 是否是页面初始化的时候
     */
    private boolean isInitMsg;
    /**
     * 首次onResume不刷新
     */
    private boolean isNotFirst;
    /**
     * load count from db or server
     */
    protected static int PAGE_SIZE = 20;
    private ChatRoomListener chatRoomListener;
    private GroupListener groupListener;
    private Handler typingHandler;
    protected OnMessageChangeListener messageChangeListener;
    private IChatTitleProvider titleProvider;//provide title to activity's title bar

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initArguments();
        return inflater.inflate(R.layout.ease_fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private void initArguments() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            isRoaming = bundle.getBoolean("isRoaming", false);
            chatType = bundle.getInt(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
            toChatUsername = bundle.getString(EaseConstant.EXTRA_USER_ID);
            forwardMsgId = bundle.getString(EaseConstant.FORWARD_MSG_ID);
            historyMsgId = bundle.getString(EaseConstant.HISTORY_MSG_ID);
            turnOnTyping = openTurnOnTyping();
            initChildArguments();
        }
    }

    private void initView() {
        tvErrorMsg = findViewById(R.id.tv_error_msg);
        chatMessageList = findViewById(R.id.chat_message_list);
        inputMenu = findViewById(R.id.input_menu);
        voiceRecorderView = findViewById(R.id.voice_recorder);
        //子类做初始化布局
        initChildView();
        initInputMenu();
        mContext.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initListener() {
        tvErrorMsg.setOnClickListener(this);
        inputMenu.setChatInputMenuListener(this);
        inputMenu.getPrimaryMenu().getEditText().addTextChangedListener(this);
        chatMessageList.setOnMessageListListener(this);
        setMessageClickListener();
        addGroupListener();
        addChatRoomListener();
        initChildListener();
    }

    private void initData() {
        //此处排除chatRoom的目的是，加入聊天室后，再进行初始化
        if(chatType != EaseConstant.CHATTYPE_CHATROOM) {
            chatMessageList.init(toChatUsername, chatType);
            chatMessageList.setHistoryMsgId(historyMsgId);
            initConversation();
        }
        initChatType();
        hideNickname();
        sendForwardMsg();
        setTypingHandler();
        initChildData();
    }

    protected void refreshMessages() {
        chatMessageList.refreshMessages();
    }

    private void setMessageClickListener() {
        if(chatMessageList != null) {
            chatMessageList.setItemClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
       if(v.getId() == R.id.tv_error_msg) {
           onChatRoomViewCreation();
       }
    }

    @Override
    public void onChatExtendMenuItemClick(int itemId, View view) {
        switch (itemId) {
            case EaseChatInputMenu.ITEM_TAKE_PICTURE :
                selectPicFromCamera();
                break;
            case EaseChatInputMenu.ITEM_PICTURE :
                selectPicFromLocal();
                break;
            case EaseChatInputMenu.ITEM_LOCATION :
                EaseBaiduMapActivity.actionStartForResult(this, REQUEST_CODE_MAP);
                break;
            case EaseChatInputMenu.ITEM_VIDEO:
                selectVideoFromLocal();
                break;
            case EaseChatInputMenu.ITEM_FILE:
                selectFileFromLocal();
                break;
        }
    }

    /**
     * {@link EaseChatMessageList#setOnMessageListListener(EaseChatMessageList.OnMessageListListener)}
     * @param v
     * @param event
     */
    @Override
    public void onTouch(View v, MotionEvent event) {
        hideKeyboard();
        inputMenu.hideExtendMenuContainer();
    }

    /**
     * {@link EaseChatMessageList#setOnMessageListListener(EaseChatMessageList.OnMessageListListener)}
     */
    @Override
    public void onRefresh() {
        chatMessageList.loadMoreMessages(PAGE_SIZE, isRoaming);
    }

    /**
     * {@link EaseChatMessageList#setOnMessageListListener(EaseChatMessageList.OnMessageListListener)}
     * @param message
     */
    @Override
    public void onMessageListError(String message) {
        showMsgToast(message);
    }

    /**
     * {@link EaseChatMessageList#setOnMessageListListener(EaseChatMessageList.OnMessageListListener)}
     */
    @Override
    public void onLoadMore() {
        chatMessageList.loadMoreHistoryMessages(PAGE_SIZE, EMConversation.EMSearchDirection.DOWN);
    }

    /**
     * input menu listener
     * when typing on the edit-text layout.
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTyping(CharSequence s, int start, int before, int count) {
        // send action:TypingBegin cmd msg.
        typingHandler.sendEmptyMessage(MSG_TYPING_BEGIN);
    }

    /**
     * input menu listener
     * when send message button pressed
     * @param content
     */
    @Override
    public void onSendMessage(String content) {
        sendTextMessage(content);
    }

    /**
     * input menu listener
     * when big icon pressed
     * @param emojicon
     */
    @Override
    public void onBigExpressionClicked(EaseEmojicon emojicon) {
        sendBigExpressionMessage(emojicon.getName(), emojicon.getIdentityCode());
    }

    /**
     * input menu listener
     * when speak button is touched
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
        return voiceRecorderView.onPressToSpeakBtnTouch(v, event, (this::sendVoiceMessage));
    }

    /**
     * MessageListItemClickListener
     * @param message
     * @return
     */
    @Override
    public boolean onBubbleClick(EMMessage message) {
        return false;
    }

    /**
     * MessageListItemClickListener
     * @param message
     * @return
     */
    @Override
    public boolean onResendClick(EMMessage message) {
        EMLog.i(TAG, "onResendClick");
        new EaseAlertDialog(getContext(), R.string.resend, R.string.confirm_resend, null, new EaseAlertDialog.AlertDialogUser() {
            @Override
            public void onResult(boolean confirmed, Bundle bundle) {
                if (!confirmed) {
                    return;
                }
                message.setStatus(EMMessage.Status.CREATE);
                sendMessage(message);
            }
        }, true).show();
        return true;
    }

    /**
     * MessageListItemClickListener
     * @param v
     * @param message
     */
    @Override
    public void onBubbleLongClick(View v, EMMessage message) {

    }

    /**
     * MessageListItemClickListener
     * @param username
     */
    @Override
    public void onUserAvatarClick(String username) {
        // 跳转逻辑由开发者自行处理

    }

    /**
     * MessageListItemClickListener
     * @param username
     */
    @Override
    public void onUserAvatarLongClick(String username) {
        // 具体逻辑
        inputAtUsername(username, true);
    }

    /**
     * MessageListItemClickListener
     * @param message
     */
    @Override
    public void onMessageInProgress(EMMessage message) {
        message.setMessageStatusCallback(this);
    }

    /**
     * message status callback
     */
    @Override
    public void onSuccess() {
        if(messageChangeListener != null) {
            EaseEvent event = EaseEvent.create(EaseConstant.MESSAGE_CHANGE_SEND_SUCCESS, EaseEvent.TYPE.MESSAGE);
            messageChangeListener.onMessageChange(event);
        }
        EMLog.i(TAG, "send message success");
        refreshMessages();
    }

    /**
     * message status callback
     */
    @Override
    public void onError(int code, String error) {
        if(messageChangeListener != null) {
            EaseEvent event = EaseEvent.create(EaseConstant.MESSAGE_CHANGE_SEND_ERROR, EaseEvent.TYPE.MESSAGE);
            messageChangeListener.onMessageChange(event);
        }
        EMLog.i(TAG, "send message error = "+error);
        refreshMessages();
        if(getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "onError: " + code + ", error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * message status callback
     */
    @Override
    public void onProgress(int progress, String status) {
        if(messageChangeListener != null) {
            EaseEvent event = EaseEvent.create(EaseConstant.MESSAGE_CHANGE_SEND_PROGRESS, EaseEvent.TYPE.MESSAGE);
            messageChangeListener.onMessageChange(event);
        }
        EMLog.i(TAG, "send message on progress");
        refreshMessages();
    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        if(messageChangeListener != null) {
            EaseEvent event = EaseEvent.create(EaseConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE);
            messageChangeListener.onMessageChange(event);
        }
        boolean refresh = false;
        for (EMMessage message : messages) {
            String username = null;
            // group message
            if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                username = message.getTo();
            } else {
                // single chat message
                username = message.getFrom();
            }
            // if the message is for current conversation
            if (username.equals(toChatUsername) || message.getTo().equals(toChatUsername) || message.conversationId().equals(toChatUsername)) {
                refresh = true;
            }
        }
        if(refresh) {
            refreshToLatest();
        }

    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
        // 对方是否正在输入的消息回调
        for (final EMMessage msg : messages) {
            final EMCmdMessageBody body = (EMCmdMessageBody) msg.getBody();
            EMLog.i(TAG, "Receive cmd message: " + body.action() + " - " + body.isDeliverOnlineOnly());
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ACTION_TYPING_BEGIN.equals(body.action()) && msg.getFrom().equals(toChatUsername)) {
                        setTitleBarText(getString(R.string.alert_during_typing));
                    } else if (ACTION_TYPING_END.equals(body.action()) && msg.getFrom().equals(toChatUsername)) {
                        setTitleBarText(toChatUsername);
                    }
                }
            });
        }
    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageRead(List<EMMessage> messages) {
        refreshMessages();
    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageDelivered(List<EMMessage> messages) {
        refreshMessages();
    }

    /**
     * EMMessageListener
     * @param messages
     */
    @Override
    public void onMessageRecalled(List<EMMessage> messages) {
        if(messageChangeListener != null) {
            EaseEvent event = EaseEvent.create(EaseConstant.MESSAGE_CHANGE_RECALL, EaseEvent.TYPE.MESSAGE);
            messageChangeListener.onMessageChange(event);
        }
        refreshMessages();
    }

    /**
     * EMMessageListener
     * @param message
     * @param change
     */
    @Override
    public void onMessageChanged(EMMessage message, Object change) {
        if(messageChangeListener != null) {
            messageChangeListener.onMessageChange(EaseEvent.create(EaseConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));
        }
        refreshMessages();
    }

    /**
     * inputMenu addTextChangedListener
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * inputMenu addTextChangedListener
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(count == 1 && "@".equals(String.valueOf(s.charAt(start)))){
//            startActivityForResult(new Intent(getActivity(), PickAtUserActivity.class).
//                    putExtra("groupId", toChatUsername), REQUEST_CODE_SELECT_AT_USER);

        }
    }

    /**
     * inputMenu addTextChangedListener
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {

    }

//============================ child init start ================================
    /**
     * init child arguments
     */
    protected void initChildArguments() {}


    /**
     * init child view
     */
    protected void initChildView() {}

    /**
     * init child listener
     */
    protected void initChildListener() {}

    /**
     * init child data
     */
    protected void initChildData() {}

    /**
     * developer can override the method to change default chat extend menu items
     */
    protected void initInputMenu() {
        inputMenu.registerDefaultMenuItems(this);
        addExtendInputMenu(inputMenu);
    }

    /**
     * developer can add extend menu item by override the method
     * @param inputMenu
     */
    protected void addExtendInputMenu(EaseChatInputMenu inputMenu) {
        // inputMenu.registerExtendMenuItem(nameRes, drawableRes, itemId, listener);
    }

    /**
     * init chat conversation
     */
    protected void initConversation() {
        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername
                , EaseCommonUtils.getConversationType(chatType), true);
        // make all message as read
        if(messageChangeListener != null) {
            int count = conversation.getUnreadMsgCount();
            if(count > 0) {
                messageChangeListener.onMessageChange(EaseEvent.create(EaseConstant.CONVERSATION_READ, EaseEvent.TYPE.MESSAGE));
            }
        }

        conversation.markAllMessagesAsRead();
        isInitMsg = true;
        //如果设置为漫游
        if(isRoaming) {
            //第一次展示，如果本地数据足够，先不从服务器取数据
            if(chatMessageList != null) {
                chatMessageList.loadMoreServerMessages(PAGE_SIZE, true);
            }
            return;
        }
        // 非漫游，从本地数据库拉取数据
        if(chatMessageList != null) {
            chatMessageList.loadMessagesFromLocal(PAGE_SIZE);
        }
    }

//============================ child init end ================================

//============================== view control start ===========================


    private void initChatType() {
        if(isSingleChat()) {
            setTitleBarText(toChatUsername);
        }else if(isGroupChat()) {
            EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
            if (group != null){
                setTitleBarText(group.getGroupName());
            }
        }else if(isChatRoomChat()) {
            onChatRoomViewCreation();
        }
    }

    /**
     * set titleBar title
     * @param title
     */
    protected void setTitleBarText(String title) {
        if(titleProvider != null) {
            titleProvider.provideTitle(chatType, title);
        }
    }

    /**
     * provide recyclerView LayoutManager
     * @return
     */
    protected RecyclerView.LayoutManager provideLayoutManager() {
        return new LinearLayoutManager(mContext);
    }

    /**
     * show msg toast
     * @param message
     */
    protected void showMsgToast(String message) {
        // developer can show the message by your own style
    }

    public void setOnMessageChangeListener(OnMessageChangeListener listener) {
        this.messageChangeListener = listener;
    }

    /**
     * 用于监听消息的变化，发送消息及接收消息
     */
    public interface OnMessageChangeListener {
        void onMessageChange(EaseEvent change);
    }

    public void setIChatTitleProvider(IChatTitleProvider titleProvider) {
        this.titleProvider = titleProvider;
    }

    /**
     * 聊天标题
     */
    public interface IChatTitleProvider {
        /**
         * 标题
         * @param chatType
         * @param title
         */
        void provideTitle(int chatType, String title);
    }


//============================== view control end ===========================

//======================= choose resources start ============================

    /**
     * select local video
     */
    protected void selectVideoFromLocal() {

    }

    /**
     * select local file
     */
    protected void selectFileFromLocal() {
        Intent intent = new Intent();
        if(VersionUtils.isTargetQ(getActivity())) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    /**
     * select local image
     */
    protected void selectPicFromLocal() {
        EaseCompat.openImage(this, REQUEST_CODE_LOCAL);
    }

    /**
     * select picture from camera
     */
    protected void selectPicFromCamera() {
        if(!checkSdCardExist()) {
            return;
        }
        cameraFile = new File(PathUtil.getInstance().getImagePath(), EMClient.getInstance().getCurrentUser()
                + System.currentTimeMillis() + ".jpg");
        //noinspection ResultOfMethodCallIgnored
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, EaseCompat.getUriForFile(getContext(), cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    /**
     * 检查sd卡是否挂载
     * @return
     */
    protected boolean checkSdCardExist() {
        return EaseCommonUtils.isSdcardExist();
    }

//====================================== choose resources end =================================

//==================================== send message start ======================================
    /**
     * send image
     *
     * @param selectedImage
     */
    protected void sendPicByUri(Uri selectedImage) {
        String path = EaseCompat.getPath(getActivity(), selectedImage);
        if(!TextUtils.isEmpty(path) && new File(path).exists()) {
            sendImageMessage(path);
        }else {
            sendImageMessage(selectedImage);
        }
    }

    /**
     * send file
     * @param uri
     */
    protected void sendFileByUri(Uri uri){
        if(VersionUtils.isTargetQ(getContext())) {
            sendFileMessage(uri);
        }else {
            String filePath = EaseCompat.getPath(getActivity(), uri);
            EMLog.i(TAG, "sendFileByUri: " + filePath);
            if (filePath == null) {
                return;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(getActivity(), R.string.File_does_not_exist, Toast.LENGTH_SHORT).show();
                return;
            }
            sendFileMessage(filePath);
        }
    }

    /**
     * 发送文本消息
     * @param content
     */
    protected void sendTextMessage(String content) {
        if(EaseAtMessageHelper.get().containsAtUsername(content)) {
            sendAtMessage(content);
            return;
        }
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendMessage(message);
    }

    /**
     * send big expression message
     * @param name
     * @param identityCode
     */
    protected void sendBigExpressionMessage(String name, String identityCode){
        EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityCode);
        sendMessage(message);
    }

    /**
     * send voice message
     * @param filePath
     * @param length
     */
    protected void sendVoiceMessage(String filePath, int length) {
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
        sendMessage(message);
    }

    /**
     * send image message
     * @param imagePath
     */
    protected void sendImageMessage(String imagePath) {
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message);
    }

    protected void sendImageMessage(Uri imageUri) {
        EMMessage message = EMMessage.createImageSendMessage(imageUri, false, toChatUsername);
        sendMessage(message);
    }

    /**
     * send location message
     * @param latitude
     * @param longitude
     * @param locationAddress
     */
    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        sendMessage(message);
    }

    /**
     * send video message
     * @param videoPath
     * @param thumbPath
     * @param videoLength
     */
    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
        sendMessage(message);
    }

    protected void sendVideoMessage(Uri videoUri, String thumbPath, int videoLength) {
        EMMessage message = EMMessage.createVideoSendMessage(videoUri, thumbPath, videoLength, toChatUsername);
        sendMessage(message);
    }

    /**
     * send file message
     * @param filePath
     */
    protected void sendFileMessage(String filePath) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message);
    }

    protected void sendFileMessage(Uri fileUri) {
        EMMessage message = EMMessage.createFileSendMessage(fileUri, toChatUsername);
        sendMessage(message);
    }

    /**
     * send message
     * @param message
     */
    protected void sendMessage(EMMessage message) {
        addMessageAttributes(message);
        if (chatType == EaseConstant.CHATTYPE_GROUP){
            message.setChatType(EMMessage.ChatType.GroupChat);
        }else if(chatType == EaseConstant.CHATTYPE_CHATROOM){
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                EMLog.d("msg", "send message onSuccess");
                if(chatMessageList != null) {
                    chatMessageList.refreshMessages();
                }
            }

            @Override
            public void onError(int code, String error) {
                EMLog.d("msg", "send message onError");
                showMsgToast("error code:"+code+" error message:"+message);
                if(chatMessageList != null) {
                    chatMessageList.refreshMessages();
                }
            }

            @Override
            public void onProgress(int progress, String status) {
                EMLog.d("msg", "send message onProgress");
                if(chatMessageList != null) {
                    chatMessageList.refreshMessages();
                }
            }
        });
        // send message
        EMClient.getInstance().chatManager().sendMessage(message);
        // refresh messages
        refreshToLatest();
    }

    /**
     * forward message
     */
    protected void sendForwardMsg() {
        if(TextUtils.isEmpty(forwardMsgId)) {
            return;
        }
        final EMMessage forward_msg = EMClient.getInstance().chatManager().getMessage(forwardMsgId);
        EMMessage.Type type = forward_msg.getType();
        switch (type) {
            case TXT:
                if(forward_msg.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
                    sendBigExpressionMessage(((EMTextMessageBody) forward_msg.getBody()).getMessage(),
                            forward_msg.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, null));
                }else{
                    // get the content and send it
                    String content = ((EMTextMessageBody) forward_msg.getBody()).getMessage();
                    sendTextMessage(content);
                }
                break;
            case IMAGE:
                // send image
                String filePath = ((EMImageMessageBody) forward_msg.getBody()).getLocalUrl();
                if (filePath != null) {
                    File file = new File(filePath);
                    if (!file.exists()) {
                        // send thumb nail if original image does not exist
                        filePath = ((EMImageMessageBody) forward_msg.getBody()).thumbnailLocalPath();
                    }
                    sendImageMessage(filePath);
                }
                break;
            default:
                break;
        }

        if(forward_msg.getChatType() == EMMessage.ChatType.ChatRoom){
            EMClient.getInstance().chatroomManager().leaveChatRoom(forward_msg.getTo());
        }
    }

    /**
     * add message extension
     * 添加扩展消息
     * @param message
     */
    protected void addMessageAttributes(EMMessage message) {
        // set message extension, for example
        // message.setAttribute("em_robot_message", isRobot);
    }

//============================== send message end ==============================================

//============================== fragment life cycle start =====================================

    @Override
    public void onResume() {
        super.onResume();
        if(isInitMsg) {
            if(!isNotFirst) {
                refreshToLatest();
            }else {
                //判断是否有新的消息，如果有新的消息，则刷新到最近的一条消息
                if(chatMessageList != null && chatMessageList.haveNewMessages()) {
                    refreshToLatest();
                }else {
                    refreshMessages();
                }
            }
        }
        isNotFirst = true;
        // register the event listener when enter the foreground
        EMClient.getInstance().chatManager().addMessageListener(this);
        if(isGroupChat()) {
            EaseAtMessageHelper.get().removeAtMeGroup(toChatUsername);
        }
    }

    private void refreshToLatest() {
        if(chatMessageList != null) {
            chatMessageList.refreshToLatest();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister this event listener when this activity enters the
        // background
        EMClient.getInstance().chatManager().removeMessageListener(this);
        if(typingHandler != null) {
            typingHandler.sendEmptyMessage(MSG_TYPING_END);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                if (cameraFile != null && cameraFile.exists())
                    sendImageMessage(cameraFile.getAbsolutePath());
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        if(VersionUtils.isTargetQ(getContext())) {
                            sendImageMessage(selectedImage);
                        }else {
                            sendPicByUri(selectedImage);
                        }
                    }
                }
            } else if (requestCode == REQUEST_CODE_MAP) { // location
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
                    sendLocationMessage(latitude, longitude, locationAddress);
                } else {
                    showMsgToast(getResources().getString(R.string.unable_to_get_loaction));
                }

            } else if (requestCode == REQUEST_CODE_DING_MSG) { // To send the ding-type msg.
                String msgContent = data.getStringExtra("msg");
                EMLog.i(TAG, "To send the ding-type msg, content: " + msgContent);
                // Send the ding-type msg.
                EMMessage dingMsg = EaseDingMessageHelper.get().createDingMessage(toChatUsername, msgContent);
                sendMessage(dingMsg);
            }else if(requestCode == REQUEST_CODE_SELECT_FILE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        sendFileByUri(uri);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(groupListener != null) {
            EMClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
        }
        if(chatRoomListener != null) {
            EMClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomListener);
        }
        if(isChatRoomChat()) {
            EMClient.getInstance().chatroomManager().leaveChatRoom(toChatUsername);
        }
    }

//================================ fragment life cycle end ============================================

//================================= for single start ================================

    /**
     * 判断是否是single chat
     * @return
     */
    protected boolean isSingleChat() {
        return chatType == EaseConstant.CHATTYPE_SINGLE;
    }

    /**
     * 用于控制，是否告诉对方，你正在输入中
     * @return
     */
    protected boolean openTurnOnTyping() {
        return false;
    }

    /**
     * 不展示nickname
     */
    protected void hideNickname() {
        if(isSingleChat()) {
            chatMessageList.showUserNick(false);
        }
    }

    private void setTypingHandler() {
        typingHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_TYPING_BEGIN :
                        setTypingBeginMsg(this);
                        break;
                    case MSG_TYPING_END :
                        setTypingEndMsg(this);
                        break;
                }

            }
        };
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
        if (handler.hasMessages(MSG_TYPING_END)) {
            // reset the MSG_TYPING_END handler msg.
            handler.removeMessages(MSG_TYPING_END);
        } else {
            // Send TYPING-BEGIN cmd msg
            EMMessage beginMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
            EMCmdMessageBody body = new EMCmdMessageBody(ACTION_TYPING_BEGIN);
            // Only deliver this cmd msg to online users
            body.deliverOnlineOnly(true);
            beginMsg.addBody(body);
            beginMsg.setTo(toChatUsername);
            EMClient.getInstance().chatManager().sendMessage(beginMsg);
        }
        handler.sendEmptyMessageDelayed(MSG_TYPING_END, TYPING_SHOW_TIME);
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

        // remove all pedding msgs to avoid memory leak.
        handler.removeCallbacksAndMessages(null);
        // Send TYPING-END cmd msg
        EMMessage endMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody body = new EMCmdMessageBody(ACTION_TYPING_END);
        // Only deliver this cmd msg to online users
        body.deliverOnlineOnly(true);
        endMsg.addBody(body);
        endMsg.setTo(toChatUsername);
        EMClient.getInstance().chatManager().sendMessage(endMsg);
    }


//================================= for single end ================================

//================================== for group start ================================

    /**
     * only for group chat
     * @param content
     */
    protected void sendAtMessage(String content) {
        if(!isGroupChat()){
            EMLog.e(TAG, "only support group chat message");
            return;
        }
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
        if(EMClient.getInstance().getCurrentUser().equals(group.getOwner()) && EaseAtMessageHelper.get().containsAtAll(content)){
            message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL);
        }else {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG,
                    EaseAtMessageHelper.get().atListToJsonArray(EaseAtMessageHelper.get().getAtMessageUsernames(content)));
        }
        sendMessage(message);
    }

    /**
     * user war been kicked from group
     * @param groupId
     * @param groupName
     */
    protected void onUserRemoved(String groupId, String groupName) {
        if(isActivityDisable()) {
            return;
        }
        mContext.runOnUiThread(()-> mContext.finish());
    }

    /**
     * group was been destroyed
     * @param groupId
     * @param groupName
     */
    protected void onGroupDestroyed(String groupId, String groupName) {
        if(isActivityDisable()) {
            return;
        }
        mContext.runOnUiThread(()-> mContext.finish());
    }

    protected void addGroupListener() {
        if(!isGroupChat()) {
            return;
        }
        groupListener = new GroupListener();
        EMClient.getInstance().groupManager().addGroupChangeListener(groupListener);
    }

    /**
     * 判断是否是群组聊天
     * @return
     */
    protected boolean isGroupChat() {
        return chatType == EaseConstant.CHATTYPE_GROUP;
    }

    /**
     * input @
     * only for group chat
     * @param username
     */
    protected void inputAtUsername(String username, boolean autoAddAtSymbol){
        if(EMClient.getInstance().getCurrentUser().equals(username) ||
                !isGroupChat()){
            return;
        }
        EaseAtMessageHelper.get().addAtUser(username);
        EaseUser user = EaseUserUtils.getUserInfo(username);
        if (user != null){
            username = user.getNickname();
        }
        if(autoAddAtSymbol)
            inputMenu.insertText("@" + username + " ");
        else
            inputMenu.insertText(username + " ");
    }

    /**
     * group listener
     */
    protected class GroupListener extends EaseGroupListener {

        @Override
        public void onUserRemoved(String groupId, String groupName) {
            EaseChatFragment.this.onUserRemoved(groupId, groupName);
        }

        @Override
        public void onGroupDestroyed(String groupId, String groupName) {
            EaseChatFragment.this.onGroupDestroyed(groupId, groupName);
        }
    }

//=============================== for group end =======================================

//================================ for chat room start =====================================

    /**
     * join chat room
     */
    private void onChatRoomViewCreation() {
        if(!isChatRoomChat()) {
            return;
        }
        EMClient.getInstance().chatroomManager().joinChatRoom(toChatUsername, new EMValueCallBack<EMChatRoom>() {
            @Override
            public void onSuccess(EMChatRoom value) {
                if(isActivityDisable()) {
                    return;
                }
                if(!TextUtils.equals(toChatUsername, value.getId())) {
                    return;
                }
                mContext.runOnUiThread(()-> {
                    EMChatRoom room = EMClient.getInstance().chatroomManager().getChatRoom(toChatUsername);
                    String title = room != null ? room.getName() : toChatUsername;
                    setTitleBarText(title);
                    //初始化
                    chatMessageList.init(toChatUsername, chatType);
                    initConversation();
                    tvErrorMsg.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.d(TAG, "join room failure : "+error);
                if(!isActivityDisable()) {
                    mContext.finish();
                }
            }
        });
    }

    protected void addChatRoomListener() {
        if(!isChatRoomChat()) {
            return;
        }
        chatRoomListener = new ChatRoomListener();
        EMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomListener);
    }

    /**
     * 判断是否是chat room
     * @return
     */
    protected boolean isChatRoomChat() {
        return chatType == EaseConstant.CHATTYPE_CHATROOM;
    }

    protected void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
        if(isActivityDisable()) {
            return;
        }
        mContext.runOnUiThread(() -> {
            if(!TextUtils.equals(roomId, toChatUsername)) {
                return;
            }
            if(reason == EMAChatRoomManagerListener.BE_KICKED) {
                mContext.finish();
            }else {
                tvErrorMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    protected void onMemberJoined(String roomId, String participant) {
        if(isActivityDisable()) {
            return;
        }
    }

    protected void onMemberExited(String roomId, String roomName, String participant) {
        if (isActivityDisable()) {
            return;
        }
    }

    protected void onChatRoomDestroyed(String roomId, String roomName) {
        if(isActivityDisable()) {
            return;
        }
        mContext.runOnUiThread(() -> mContext.finish());
    }

    private class ChatRoomListener extends EaseChatRoomListener {

        @Override
        public void onChatRoomDestroyed(String roomId, String roomName) {
            EaseChatFragment.this.onChatRoomDestroyed(roomId, roomName);
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            EaseChatFragment.this.onRemovedFromChatRoom(reason, roomId,  roomName, participant);
        }

        @Override
        public void onMemberJoined(String roomId, String participant) {
            EaseChatFragment.this.onMemberJoined(roomId, participant);
        }

        @Override
        public void onMemberExited(String roomId, String roomName, String participant) {
            EaseChatFragment.this.onMemberExited(roomId, roomName, participant);
        }
    }

//================================ for chat room end =====================================

}
