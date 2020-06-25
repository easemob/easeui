package com.hyphenate.easeui;

import android.content.Context;

import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.model.EaseNotifier;
import com.hyphenate.easeui.provider.EaseEmojiconInfoProvider;
import com.hyphenate.easeui.provider.EaseSettingsProvider;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;

public class EaseUI {
    private static EaseUI instance;

    private EaseSettingsProvider settingsProvider;

    private EaseEmojiconInfoProvider mEmojiconInfoProvider;

    private EaseUserProfileProvider userProvider;
    /**
     * chat avatar options which we can easily control the style
     */
    private EaseAvatarOptions avatarOptions;
    /**
     * application context
     */
    private Context appContext = null;
    /**
     * the notifier
     */
    private EaseNotifier notifier = null;

    public boolean isVoiceCalling;
    public boolean isVideoCalling;
    private EaseChatPresenter presenter;

    private EaseUI() {}

    public static EaseUI getInstance() {
        if(instance == null) {
            synchronized (EaseUI.class) {
                if(instance == null) {
                    instance = new EaseUI();
                }
            }
        }
        return instance;
    }

    public synchronized void init(Context context) {
        appContext = context.getApplicationContext();
        initNotifier();
        presenter = new EaseChatPresenter();
        presenter.attachApp(appContext);
    }

    private void initNotifier(){
        notifier = new EaseNotifier(appContext);
    }

    public void addChatPresenter(EaseChatPresenter presenter) {
        this.presenter = presenter;
        this.presenter.attachApp(appContext);
    }

    public EaseChatPresenter getChatPresenter() {
        return presenter;
    }

    /**
     * get emojicon provider
     * @return
     */
    public EaseEmojiconInfoProvider getEmojiconInfoProvider() {
        return mEmojiconInfoProvider;
    }

    /**
     * set emojicon provider
     * @param emojiconInfoProvider
     * @return
     */
    public EaseUI setEmojiconInfoProvider(EaseEmojiconInfoProvider emojiconInfoProvider) {
        mEmojiconInfoProvider = emojiconInfoProvider;
        return this;
    }

    /**
     * get settings provider
     * @return
     */
    public EaseSettingsProvider getSettingsProvider() {
        return settingsProvider;
    }

    /**
     * set settting provider
     * @param settingsProvider
     * @return
     */
    public EaseUI setSettingsProvider(EaseSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
        return this;
    }

    /**
     * get user profile provider
     * @return
     */
    public EaseUserProfileProvider getUserProvider() {
        return userProvider;
    }

    public EaseNotifier getNotifier(){
        return notifier;
    }

    /**
     * set user profile provider
     * @param userProvider
     * @return
     */
    public EaseUI setUserProvider(EaseUserProfileProvider userProvider) {
        this.userProvider = userProvider;
        return this;
    }

    /**
     * get avatar options
     * @return
     */
    public EaseAvatarOptions getAvatarOptions() {
        return avatarOptions;
    }

    /**
     * set avatar options
     * @param avatarOptions
     */
    public EaseUI setAvatarOptions(EaseAvatarOptions avatarOptions) {
        this.avatarOptions = avatarOptions;
        return this;
    }

    public Context getContext() {
        return appContext;
    }
}
