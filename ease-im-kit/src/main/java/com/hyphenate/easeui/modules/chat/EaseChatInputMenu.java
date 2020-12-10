package com.hyphenate.easeui.modules.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.modules.chat.interfaces.ChatInputMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatExtendMenuItemClickListener;
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatPrimaryMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.EaseEmojiconMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatEmojiconMenu;
import com.hyphenate.easeui.modules.chat.interfaces.IChatExtendMenu;
import com.hyphenate.easeui.modules.chat.interfaces.IChatInputMenu;
import com.hyphenate.easeui.modules.chat.interfaces.IChatPrimaryMenu;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.util.EMLog;


public class EaseChatInputMenu extends LinearLayout implements IChatInputMenu, EaseChatPrimaryMenuListener, EaseEmojiconMenuListener, EaseChatExtendMenuItemClickListener {
    private static final String TAG = EaseChatInputMenu.class.getSimpleName();
    private LinearLayout chatMenuContainer;
    private FrameLayout primaryMenuContainer;
    private FrameLayout extendMenuContainer;

    private IChatPrimaryMenu primaryMenu;
    private IChatEmojiconMenu emojiconMenu;
    private IChatExtendMenu extendMenu;
    private ChatInputMenuListener menuListener;

    public EaseChatInputMenu(Context context) {
        this(context, null);
    }

    public EaseChatInputMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatInputMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_input_menu_container, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        chatMenuContainer = findViewById(R.id.chat_menu_container);
        primaryMenuContainer = findViewById(R.id.primary_menu_container);
        extendMenuContainer = findViewById(R.id.extend_menu_container);

        init();
    }

    private void init() {
        showPrimaryMenu();
        if(extendMenu == null) {
            extendMenu = new EaseChatExtendMenu(getContext());
            ((EaseChatExtendMenu)extendMenu).init();
        }
        if(emojiconMenu == null) {
            emojiconMenu = new EaseEmojiconMenu(getContext());
            ((EaseEmojiconMenu)emojiconMenu).init();
        }
    }

    @Override
    public void setCustomPrimaryMenu(IChatPrimaryMenu menu) {
        this.primaryMenu = menu;
        showPrimaryMenu();
    }

    @Override
    public void setCustomEmojiconMenu(IChatEmojiconMenu menu) {
        this.emojiconMenu = menu;
    }

    @Override
    public void setCustomExtendMenu(IChatExtendMenu menu) {
        this.extendMenu = menu;
    }

    @Override
    public void hideExtendContainer() {
        primaryMenu.showNormalStatus();
        extendMenuContainer.setVisibility(GONE);
    }

    @Override
    public void showEmojiconMenu(boolean show) {
        if(show) {
            showEmojiconMenu();
        }else {
           extendMenuContainer.setVisibility(GONE);
        }
    }

    @Override
    public void showExtendMenu(boolean show) {
        if(show) {
            showExtendMenu();
        }else {
            extendMenuContainer.setVisibility(GONE);
            if(primaryMenu != null) {
                primaryMenu.hideExtendStatus();
            }
        }
    }

    @Override
    public void hideSoftKeyboard() {
        if(primaryMenu != null) {
            primaryMenu.hideSoftKeyboard();
        }
    }

    @Override
    public void setChatInputMenuListener(ChatInputMenuListener listener) {
        this.menuListener = listener;
    }

    @Override
    public IChatPrimaryMenu getPrimaryMenu() {
        return primaryMenu;
    }

    @Override
    public IChatEmojiconMenu getEmojiconMenu() {
        return emojiconMenu;
    }

    @Override
    public IChatExtendMenu getChatExtendMenu() {
        return extendMenu;
    }

    @Override
    public boolean onBackPressed() {
        if(extendMenuContainer.getVisibility() == VISIBLE) {
            extendMenuContainer.setVisibility(GONE);
            return false;
        }
        return true;
    }

    private void showPrimaryMenu() {
        if(primaryMenu == null) {
            primaryMenu = new EaseChatPrimaryMenu(getContext());
        }
        if(primaryMenu instanceof View) {
            primaryMenuContainer.removeAllViews();
            primaryMenuContainer.addView((View) primaryMenu);
            primaryMenu.setEaseChatPrimaryMenuListener(this);
        }
        if(primaryMenu instanceof Fragment && getContext() instanceof AppCompatActivity) {
            FragmentManager manager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.primary_menu_container, (Fragment) primaryMenu).commitAllowingStateLoss();
            primaryMenu.setEaseChatPrimaryMenuListener(this);
        }
    }

    private void showExtendMenu() {
        if(extendMenu == null) {
            extendMenu = new EaseChatExtendMenu(getContext());
            ((EaseChatExtendMenu)extendMenu).init();
        }
        if(extendMenu instanceof View) {
            extendMenuContainer.setVisibility(VISIBLE);
            extendMenuContainer.removeAllViews();
            extendMenuContainer.addView((View) extendMenu);
            extendMenu.setEaseChatExtendMenuItemClickListener(this);
        }
        if(extendMenu instanceof Fragment && getContext() instanceof AppCompatActivity) {
            extendMenuContainer.setVisibility(VISIBLE);
            FragmentManager manager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.extend_menu_container, (Fragment) extendMenu).commitAllowingStateLoss();
            extendMenu.setEaseChatExtendMenuItemClickListener(this);
        }
    }

    private void showEmojiconMenu() {
        if(emojiconMenu == null) {
            emojiconMenu = new EaseEmojiconMenu(getContext());
            ((EaseEmojiconMenu)emojiconMenu).init();
        }
        if(emojiconMenu instanceof View) {
            extendMenuContainer.setVisibility(VISIBLE);
            extendMenuContainer.removeAllViews();
            extendMenuContainer.addView((View) emojiconMenu);
            emojiconMenu.setEmojiconMenuListener(this);
        }
        if(emojiconMenu instanceof Fragment && getContext() instanceof AppCompatActivity) {
            extendMenuContainer.setVisibility(VISIBLE);
            FragmentManager manager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.extend_menu_container, (Fragment) emojiconMenu).commitAllowingStateLoss();
            emojiconMenu.setEmojiconMenuListener(this);
        }
    }

    @Override
    public void onSendBtnClicked(String content) {
        EMLog.i(TAG, "onSendBtnClicked content:"+content);
        if(menuListener != null) {
            menuListener.onSendMessage(content);
        }
    }

    @Override
    public void onTyping(CharSequence s, int start, int before, int count) {
        EMLog.i(TAG, "onTyping: s = "+s);
        if(menuListener != null) {
            menuListener.onTyping(s, start, before, count);
        }
    }

    @Override
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
        if(menuListener != null) {
            return menuListener.onPressToSpeakBtnTouch(v, event);
        }
        return false;
    }

    @Override
    public void onToggleVoiceBtnClicked() {
        Log.e("TAG", "onToggleVoiceBtnClicked");
        showExtendMenu(false);
    }

    @Override
    public void onToggleTextBtnClicked() {
        EMLog.i(TAG, "onToggleTextBtnClicked");
        showExtendMenu(false);
    }

    @Override
    public void onToggleExtendClicked(boolean extend) {
        EMLog.i(TAG, "onToggleExtendClicked extend:"+extend);
        showExtendMenu(extend);
    }

    @Override
    public void onToggleEmojiconClicked(boolean extend) {
        EMLog.i(TAG, "onToggleEmojiconClicked extend:"+extend);
        showEmojiconMenu(extend);
    }

    @Override
    public void onEditTextClicked() {
        EMLog.i(TAG, "onEditTextClicked");
    }

    @Override
    public void onEditTextHasFocus(boolean hasFocus) {
        EMLog.i(TAG, "onEditTextHasFocus: hasFocus = "+hasFocus);
    }

    @Override
    public void onExpressionClicked(Object emojicon) {
        EMLog.i(TAG, "onExpressionClicked");
        if(emojicon instanceof EaseEmojicon) {
            EaseEmojicon easeEmojicon = (EaseEmojicon) emojicon;
            if(easeEmojicon.getType() != EaseEmojicon.Type.BIG_EXPRESSION){
                if(easeEmojicon.getEmojiText() != null){
                    primaryMenu.onEmojiconInputEvent(EaseSmileUtils.getSmiledText(getContext(),easeEmojicon.getEmojiText()));
                }
            }else{
                if(menuListener != null){
                    menuListener.onExpressionClicked(emojicon);
                }
            }
        }else {
            if(menuListener != null){
                menuListener.onExpressionClicked(emojicon);
            }
        }
    }

    @Override
    public void onDeleteImageClicked() {
        EMLog.i(TAG, "onDeleteImageClicked");
        primaryMenu.onEmojiconDeleteEvent();
    }

    @Override
    public void onChatExtendMenuItemClick(int itemId, View view) {
        EMLog.i(TAG, "onChatExtendMenuItemClick itemId = "+itemId);
        if(menuListener != null) {
            menuListener.onChatExtendMenuItemClick(itemId, view);
        }
    }
}

