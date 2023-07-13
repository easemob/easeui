package com.hyphenate.easeui.modules.chat;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.manager.EaseDingMessageHelper;
import com.hyphenate.easeui.modules.chat.interfaces.OnAddMsgAttrsBeforeSendEvent;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatFinishListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatLayoutListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnChatRecordTouchListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnModifyMessageListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnMenuChangeListener;
import com.hyphenate.easeui.modules.chat.interfaces.OnTranslateMessageListener;
import com.hyphenate.easeui.modules.menu.EaseChatFinishReason;
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper;
import com.hyphenate.easeui.modules.menu.MenuItemBean;
import com.hyphenate.easeui.ui.EaseBaiduMapActivity;
import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import com.hyphenate.easeui.ui.EaseShowNormalFileActivity;
import com.hyphenate.easeui.ui.EaseShowVideoActivity;
import com.hyphenate.easeui.ui.base.EaseBaseFragment;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseCompat;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import com.hyphenate.util.PathUtil;
import com.hyphenate.util.VersionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EaseChatFragment extends EaseBaseFragment implements OnChatLayoutListener, OnMenuChangeListener,
        OnAddMsgAttrsBeforeSendEvent, OnChatRecordTouchListener, OnTranslateMessageListener, OnChatFinishListener, OnModifyMessageListener {
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected static final int REQUEST_CODE_DING_MSG = 4;
    protected static final int REQUEST_CODE_SELECT_VIDEO = 11;
    protected static final int REQUEST_CODE_SELECT_FILE = 12;
    private static final String TAG = EaseChatFragment.class.getSimpleName();
    public EaseChatLayout chatLayout;
    public String conversationId;
    public int chatType;
    public String historyMsgId;
    public boolean isRoam;
    public boolean isMessageInit;
    private OnChatLayoutListener listener;
    private JSONObject quoteObject = null;
    private boolean isQuote;
    private int retrievalSize = 100;
    protected File cameraFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initArguments();
        return inflater.inflate(getLayoutId(), null);
    }

    private int getLayoutId() {
        return R.layout.ease_fragment_chat_list;
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

    public void initArguments() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            conversationId = bundle.getString(EaseConstant.EXTRA_CONVERSATION_ID);
            chatType = bundle.getInt(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
            historyMsgId = bundle.getString(EaseConstant.HISTORY_MSG_ID);
            isRoam = bundle.getBoolean(EaseConstant.EXTRA_IS_ROAM, false);
        }
    }

    public void initView() {
        chatLayout = findViewById(R.id.layout_chat);
        chatLayout.getChatMessageListLayout().setItemShowType(EaseChatMessageListLayout.ShowType.NORMAL);
        chatLayout.getChatMessageListLayout().setBackgroundColor(ContextCompat.getColor(mContext, R.color.gray));
        chatLayout.getChatInputMenu().getPrimaryMenu().setShowDefaultQuote(true);
        chatLayout.getChatInputMenu().getPrimaryMenu().getQuoteLayout();
    }

    public void initListener() {
        chatLayout.setOnChatLayoutListener(this);
        chatLayout.setOnPopupWindowItemClickListener(this);
        chatLayout.setOnAddMsgAttrsBeforeSendEvent(this);
        chatLayout.setOnChatRecordTouchListener(this);
        chatLayout.setOnTranslateListener(this);
        chatLayout.setOnChatFinishListener(this);
        chatLayout.setOnEditMessageListener(this);
    }

    public void initData() {
        if(!TextUtils.isEmpty(historyMsgId)) {
            chatLayout.init(EaseChatMessageListLayout.LoadDataType.HISTORY, conversationId, chatType);
            chatLayout.loadData(historyMsgId);
        }else {
            if(isRoam) {
                chatLayout.init(EaseChatMessageListLayout.LoadDataType.ROAM, conversationId, chatType);
            }else {
                chatLayout.init(conversationId, chatType);
            }
            chatLayout.loadDefaultData();
        }
        isMessageInit = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isMessageInit && chatLayout != null) {
            isQuote = false;
            chatLayout.getChatMessageListLayout().refreshMessages();
            chatLayout.getChatInputMenu().getPrimaryMenu().hideQuoteSelect();
        }
    }

    public void setOnChatLayoutListener(OnChatLayoutListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onBubbleClick(EMMessage message) {
        if(listener != null) {
            return listener.onBubbleClick(message);
        }
        return false;
    }

    @Override
    public boolean onBubbleLongClick(View v, EMMessage message) {
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
        if(listener != null) {
            listener.onUserAvatarLongClick(username);
        }
    }

    @Override
    public void onChatExtendMenuItemClick(View view, int itemId) {
        if(itemId == R.id.extend_item_take_picture) {
            selectPicFromCamera();
        }else if(itemId == R.id.extend_item_picture) {
            selectPicFromLocal();
        }else if(itemId == R.id.extend_item_location) {
            startMapLocation(REQUEST_CODE_MAP);
        }else if(itemId == R.id.extend_item_video) {
            selectVideoFromLocal();
        }else if(itemId == R.id.extend_item_file) {
            selectFileFromLocal();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void onChatSuccess(EMMessage message) {
        isQuote = false;
        // you can do something after sending a successful message
        if (message.getType() == EMMessage.Type.TXT){
            chatLayout.getChatInputMenu().getPrimaryMenu().hideQuoteSelect();
        }
    }

    @Override
    public void onChatError(int code, String errorMsg) {
        if(listener != null) {
            listener.onChatError(code, errorMsg);
        }
    }

    @Override
    public void onQuoteClick(EMMessage message) {
        EMConversation currentConversation = chatLayout.getChatMessageListLayout().getCurrentConversation();
        if (message.getType() == EMMessage.Type.TXT || message.getType() == EMMessage.Type.VOICE){
            //如果是文本类型或者语音类型消息 先在当前缓存消息中查看是否能找到
            int size = chatLayout.getChatMessageListLayout().getMessageAdapter().getData().size();
            int position = chatLayout.getChatMessageListLayout().getMessageAdapter().getData().lastIndexOf(message);
            //如果找不到 在从db加载数据 之后再查询
            if(position == -1){
                chatLayout.getChatMessageListLayout().loadMorePreviousData(retrievalSize, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        List<EMMessage> currentData = currentConversation.getAllMessages();
                        if (currentData != null && currentData.size() > 0){
                            int dataSize = currentData.size();
                            int position = chatLayout.getChatMessageListLayout().getMessageAdapter().getData().lastIndexOf(message);
                            //如果查到了
                            if (position != -1){
                                //如果 position 再限制条数以内 则直接跳转指定位置
                                if (position - (dataSize - retrievalSize)  > 0){
                                    chatLayout.getChatMessageListLayout().moveToPosition(position);
                                }else {
                                    runOnUiThread(new Runnable() {
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
                                runOnUiThread(new Runnable() {
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
                    chatLayout.getChatMessageListLayout().moveToPosition(position);
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getContext() != null){
                                Toast.makeText(getContext(),getContext().getString(R.string.quote_limitation),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }else {
            showQuoteByType(message);
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

    protected void addCustomQuote(EMMessage message){}

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
            case LOCATION:
                EMLocationMessageBody locBody = (EMLocationMessageBody) message.getBody();
                EaseBaiduMapActivity.actionStart(getContext(),
                        locBody.getLatitude(),
                        locBody.getLongitude(),
                        locBody.getAddress());
                break;
            case CUSTOM:
                addCustomQuote(message);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onQuoteLongClick(View v, EMMessage message) {
        if (listener != null){
            return listener.onQuoteLongClick(v,message);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            chatLayout.getChatInputMenu().hideExtendContainer();
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                onActivityResultForCamera(data);
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                onActivityResultForLocalPhotos(data);
            } else if (requestCode == REQUEST_CODE_MAP) { // location
                onActivityResultForMapLocation(data);
            } else if (requestCode == REQUEST_CODE_DING_MSG) { // To send the ding-type msg.
                onActivityResultForDingMsg(data);
            }else if(requestCode == REQUEST_CODE_SELECT_FILE) {
                onActivityResultForLocalFiles(data);
            }else if(requestCode == REQUEST_CODE_SELECT_VIDEO) {
                onActivityResultForLocalVideos(data);
            }
        }
    }

    private void onActivityResultForLocalVideos(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mContext,uri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int duration = mediaPlayer.getDuration();
            EMLog.d(TAG, "path = "+uri.getPath()+",duration="+duration );
            EaseFileUtils.saveUriPermission(mContext, uri, data);
            chatLayout.sendVideoMessage(uri, duration);
        }
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
     * select local image
     */
    protected void selectPicFromLocal() {
        EaseCompat.openImage(this, REQUEST_CODE_LOCAL);
    }

    /**
     * 启动定位
     * @param requestCode
     */
    protected void startMapLocation(int requestCode) {
        EaseBaiduMapActivity.actionStartForResult(this, requestCode);
    }

    /**
     * select local video
     */
    protected void selectVideoFromLocal() {
        Intent intent = new Intent();
        if(VersionUtils.isTargetQ(getActivity())) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);

    }

    /**
     * select local file
     */
    protected void selectFileFromLocal() {
        Intent intent = new Intent();
        if(VersionUtils.isTargetQ(getActivity())) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    /**
     * 相机返回处理结果
     * @param data
     */
    protected void onActivityResultForCamera(Intent data) {
        if (cameraFile != null && cameraFile.exists()) {
            //检查图片是否被旋转并调整回来
            Uri restoreImageUri = ImageUtils.checkDegreeAndRestoreImage(mContext, Uri.parse(cameraFile.getAbsolutePath()));
            chatLayout.sendImageMessage(restoreImageUri);
        }
    }

    /**
     * 选择本地图片处理结果
     * @param data
     */
    protected void onActivityResultForLocalPhotos(@Nullable Intent data) {
        if (data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                String filePath = EaseFileUtils.getFilePath(mContext, selectedImage);
                if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    chatLayout.sendImageMessage(Uri.parse(filePath));
                }else {
                    EaseFileUtils.saveUriPermission(mContext, selectedImage, data);
                    chatLayout.sendImageMessage(selectedImage);
                }
            }
        }
    }

    /**
     * 地图定位结果处理
     * @param data
     */
    protected void onActivityResultForMapLocation(@Nullable Intent data) {
        if(data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String locationAddress = data.getStringExtra("address");
            String buildingName = data.getStringExtra("buildingName");
            if (locationAddress != null && !locationAddress.equals("")) {
                chatLayout.sendLocationMessage(latitude, longitude, locationAddress, buildingName);
            } else {
                if(listener != null) {
                    listener.onChatError(-1, getResources().getString(R.string.unable_to_get_loaction));
                }
            }
        }
    }

    protected void onActivityResultForDingMsg(@Nullable Intent data) {
        if(data != null) {
            String msgContent = data.getStringExtra("msg");
            EMLog.i(TAG, "To send the ding-type msg, content: " + msgContent);
            // Send the ding-type msg.
            EMMessage dingMsg = EaseDingMessageHelper.get().createDingMessage(conversationId, msgContent);
            chatLayout.sendMessage(dingMsg);
        }
    }

    /**
     * 本地文件选择结果处理
     * @param data
     */
    protected void onActivityResultForLocalFiles(@Nullable Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = EaseFileUtils.getFilePath(mContext, uri);
                if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    chatLayout.sendFileMessage(Uri.parse(filePath));
                }else {
                    EaseFileUtils.saveUriPermission(mContext, uri, data);
                    chatLayout.sendFileMessage(uri);
                }
            }
        }
    }

    /**
     * 检查sd卡是否挂载
     * @return
     */
    protected boolean checkSdCardExist() {
        return EaseCommonUtils.isSdcardExist();
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, EMMessage message, View v) {

    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, EMMessage message) {
        return false;
    }

    @Override
    public void addMsgAttrsBeforeSend(EMMessage message) {
        if (message.getType() == EMMessage.Type.TXT && isQuote){
            message.setAttribute(EaseConstant.QUOTE_MSG_QUOTE,quoteObject);
        }
    }

    /**
     * Set whether can touch voice button
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onRecordTouch(View v, MotionEvent event) {
        return true;
    }

    @Override
    public void translateMessageSuccess(EMMessage message) {

    }

    @Override
    public void translateMessageFail(EMMessage message, int code, String error) {

    }

    @Override
    public void onChatFinish(EaseChatFinishReason reason, String id) {
        if(mContext != null) {
            mContext.finish();
        }
    }

    /**
     * 添加自定义引用展示逻辑
     * @param message
     */
    @Override
    public void showCustomQuote(EMMessage message) {

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
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,getResources().getString(R.string.quote_card));
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"custom");
                }else {
                    quoteObject.put(EaseConstant.QUOTE_MSG_PREVIEW,"");
                    quoteObject.put(EaseConstant.QUOTE_MSG_TYPE,"txt");
                }
                quoteObject.put(EaseConstant.QUOTE_MSG_SENDER,message.getFrom());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        chatLayout.getChatInputMenu().getPrimaryMenu().primaryStartQuote(message);
    }
          
    @Override
    public void onModifyMessageSuccess(String messageId) {

    }

    @Override
    public void onModifyMessageFailure(String messageId, int code, String error) {

    }
}

