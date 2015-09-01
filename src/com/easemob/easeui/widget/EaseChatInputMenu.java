package com.easemob.easeui.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.easemob.easeui.R;
import com.easemob.easeui.widget.EaseChatExtendMenu.ChatExtendMenuItemClickListener;
import com.easemob.easeui.widget.EaseChatPrimaryMenu.ChatPrimaryMenuListener;
import com.easemob.easeui.widget.EaseEmojiconMenu.EmojiconListener;

/**
 * 聊天页面底部的聊天输入输出菜单栏
 * <br/>主要包含3个控件:EaseChatPrimaryMenu(主菜单栏，即为包含文字输入、发送等控件),
 * <br/>EaseChatExtendMenu(扩展栏，点击加号按钮出来的小宫格的菜单栏),
 * <br/>以及EaseEmojiconMenu(表情栏)
 */
public class EaseChatInputMenu extends LinearLayout{
    protected EaseChatPrimaryMenu chatPrimaryMenu;
    protected EaseChatExtendMenu chatExtendMenu;
    protected FrameLayout chatExtendMenuContainer;
    protected EaseEmojiconMenu emojicon;
    private Context context;
    
    private Handler handler = new Handler();
    private ChatInputMenuListener listener;

    public EaseChatInputMenu(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseChatInputMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EaseChatInputMenu(Context context) {
        super(context);
        init(context, null);
    }
    
    private void init(Context context, AttributeSet attrs){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_input_menu, this);
        
        //主按钮菜单栏
        chatPrimaryMenu = (EaseChatPrimaryMenu) findViewById(R.id.primary_menu);
        
        // 扩展按钮栏
        chatExtendMenu = (EaseChatExtendMenu) findViewById(R.id.extend_menu);
        
        chatExtendMenuContainer = (FrameLayout)findViewById(R.id.extend_menu_container);
        
        // 表情
        emojicon = (EaseEmojiconMenu) findViewById(R.id.emojicon);
        
        processChatMenu();
        
    }
    
    
    /**
     * 注册扩展菜单的item
     * 
     * @param name
     *            item名字
     * @param drawableRes
     *            item背景
     * @param itemId
     *             id
     * @param listener
     *            item点击事件
     */
    public void registerExtendMenuItem(String name, int drawableRes, int itemId, ChatExtendMenuItemClickListener listener){
        chatExtendMenu.registerMenuItem(name, drawableRes, itemId, listener);
    }
    
    /**
     * 注册扩展菜单的item
     * 
     * @param name
     *            item名字
     * @param drawableRes
     *            item背景
     * @param itemId
     *             id
     * @param listener
     *            item点击事件
     */
    public void registerExtendMenuItem(int nameRes, int drawableRes, int itemId, ChatExtendMenuItemClickListener listener){
        chatExtendMenu.registerMenuItem(nameRes, drawableRes, itemId, listener);
    }
    
    /**
     * 设置长按说话录制控件
     * @param voiceRecorderView
     */
    public void setPressToSpeakRecorderView(EaseVoiceRecorderView voiceRecorderView){
        chatPrimaryMenu.setPressToSpeakRecorderView(voiceRecorderView);
    }
    
    /**
     * init view
     * 此方法需放在registerExtendMenuItem后面
     */
    public void init(){
        //初始化extendmenu
        chatExtendMenu.init();
    }
    
    
    protected void processChatMenu() {
        //发送消息栏
        chatPrimaryMenu.setChatPrimaryMenuListener(new ChatPrimaryMenuListener() {

            @Override
            public void onSendBtnClicked(String content) {
                if(listener != null)
                    listener.onSendMessage(content);
            }

            @Override
            public void onToggleVoiceBtnClicked() {
                hideExtendMenuContainer();
            }

            @Override
            public void onToggleExtendClicked() {
                toggleMore();
            }

            @Override
            public void onToggleEmojiconClicked() {
                toggleEmojicon();
            }

            @Override
            public void onEditTextClicked() {
                hideExtendMenuContainer();
            }

            @Override
            public void onVoiceRecorded(String filePath, String fileName, int length) {
                if(listener != null)
                    listener.onSendVoiceMessage(filePath, fileName, length);
            }
        });

        //emojicon
        emojicon.setEmojiconListener(new EmojiconListener() {

            @Override
            public void onExpressionClicked(CharSequence emojiContent) {
                chatPrimaryMenu.onEmojiconInputEvent(emojiContent);
            }

            @Override
            public void onDeleteImageClicked() {
                chatPrimaryMenu.onEmojiconDeleteEvent();
            }
        });

    }
    
    /**
     * 显示或隐藏图标按钮页
     * 
     */
    protected void toggleMore() {
        if (chatExtendMenuContainer.getVisibility() == View.GONE) {
            hideKeyboard();
            handler.postDelayed(new Runnable() {
                public void run() {
                    chatExtendMenuContainer.setVisibility(View.VISIBLE);
                    chatExtendMenu.setVisibility(View.VISIBLE);
                    emojicon.setVisibility(View.GONE);
                }
            }, 50);
        } else {
            if (emojicon.getVisibility() == View.VISIBLE) {
                emojicon.setVisibility(View.GONE);
                chatExtendMenu.setVisibility(View.VISIBLE);
            } else {
                chatExtendMenuContainer.setVisibility(View.GONE);
            }

        }

    }
    
    /**
     * 显示或隐藏表情页
     */
    protected void toggleEmojicon(){
        if (chatExtendMenuContainer.getVisibility() == View.GONE) {
            hideKeyboard();
            handler.postDelayed(new Runnable() {
                public void run() {
                    chatExtendMenuContainer.setVisibility(View.VISIBLE);
                    chatExtendMenu.setVisibility(View.GONE);
                    emojicon.setVisibility(View.VISIBLE);
                }
            }, 50);
        } else {
            if (emojicon.getVisibility() == View.VISIBLE) {
                chatExtendMenuContainer.setVisibility(View.GONE);
                emojicon.setVisibility(View.GONE);
            } else {
                chatExtendMenu.setVisibility(View.GONE);
                emojicon.setVisibility(View.VISIBLE);
            }

        }
    }
    
    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        chatPrimaryMenu.hideKeyboard();
    }
    
    /**
     * 隐藏整个扩展按钮栏
     */
    public void hideExtendMenuContainer(){
        chatExtendMenu.setVisibility(View.GONE);
        emojicon.setVisibility(View.GONE);
        chatExtendMenuContainer.setVisibility(View.GONE);
        chatPrimaryMenu.showNormalFaceImage();
    }
    
    /**
     * 系统返回键被按时调用此方法
     * @return 返回false表示返回键时扩展菜单栏时打开状态，true则表示按返回键时扩展栏是关闭状态<br/>
     * 如果返回时打开状态状态，会先关闭扩展栏再返回值
     */
    public boolean onBackPressed() {
        if(chatExtendMenuContainer.getVisibility() == View.VISIBLE){
            hideExtendMenuContainer();
            return false;
        }else{
            return true;
        }
        
    }
    
    
    public void setChatInputMenuListener(ChatInputMenuListener listener){
        this.listener = listener;
    }
    
    public interface ChatInputMenuListener{
        /**
         * 发送消息按钮点击
         * @param content 文本内容
         */
        void onSendMessage(String content);
        
        /**
         * 发送语音消息事件
         * @param length 
         * @param fileName 
         * @param filePath 
         */
        void onSendVoiceMessage(String filePath, String fileName, int length);
    }
    
}
