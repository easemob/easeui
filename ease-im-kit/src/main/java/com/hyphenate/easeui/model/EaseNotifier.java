/************************************************************
 *  * Hyphenate CONFIDENTIAL 
 * __________________ 
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved. 
 *
 * NOTICE: All information contained herein is, and remains 
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material 
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.hyphenate.easeui.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.provider.EaseSettingsProvider;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * new message notifier class
 * <p>
 * this class is subject to be inherited and implement the relative APIs
 * <p>
 * <p>
 * 在Android 8.0之前的设备上:
 * 通知栏通知的声音和震动可以被demo设置中的'声音'和'震动'开关控制
 * 在Android 8.0设备上:
 * 通知栏通知的声音和震动不受demo设置中的'声音'和'震动'开关控制
 */
public class EaseNotifier {
    private final static String TAG = "EaseNotifier";

    protected final static String MSG_ENG = "%s contacts sent %s messages";
    protected final static String MSG_CH = "%s个联系人发来%s条消息";

    protected static int NOTIFY_ID = 0525; // start notification id

    protected static final String CHANNEL_ID = "hyphenate_chatuidemo_notification";
    protected static final long[] VIBRATION_PATTERN = new long[]{0, 180, 80, 120};

    protected NotificationManager notificationManager = null;

    protected HashSet<String> fromUsers = new HashSet<>();
    protected int notificationNum = 0;

    protected Context appContext;
    protected String packageName;
    protected String msg;
    protected long lastNotifyTime;
    protected Ringtone ringtone = null;
    protected AudioManager audioManager;
    protected Vibrator vibrator;
    protected EaseNotificationInfoProvider notificationInfoProvider;

    public EaseNotifier(Context context) {
        appContext = context.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            // Create the notification channel for Android 8.0
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "hyphenate chatuidemo message default channel.", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(VIBRATION_PATTERN);
            notificationManager.createNotificationChannel(channel);
        }

        packageName = appContext.getApplicationInfo().packageName;
        if (Locale.getDefault().getLanguage().equals("zh")) {
            msg = MSG_CH;
        } else {
            msg = MSG_ENG;
        }

        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * this function can be override
     */
    public void reset() {
        resetNotificationCount();
        cancelNotification();
    }

    void resetNotificationCount() {
        notificationNum = 0;
        fromUsers.clear();
    }

    void cancelNotification() {
        if (notificationManager != null)
            notificationManager.cancel(NOTIFY_ID);
    }

    /**
     * handle the new message
     * this function can be override
     *
     * @param message
     */
    public synchronized void notify(EMMessage message) {
        if (EaseCommonUtils.isSilentMessage(message)) {
            return;
        }

        EaseSettingsProvider settingsProvider = EaseIM.getInstance().getSettingsProvider();
        if (!settingsProvider.isMsgNotifyAllowed(message)) {
            return;
        }

        // check if app running background
        if (!EasyUtils.isAppRunningForeground(appContext)) {
            EMLog.d(TAG, "app is running in background");
            notificationNum++;
            fromUsers.add(message.getFrom());
            handleMessage(message);
        }
    }

    public synchronized void notify(List<EMMessage> messages) {
        if (EaseCommonUtils.isSilentMessage(messages.get(messages.size() - 1))) {
            return;
        }

        EaseSettingsProvider settingsProvider = EaseIM.getInstance().getSettingsProvider();
        if (!settingsProvider.isMsgNotifyAllowed(null)) {
            return;
        }

        // check if app running background
        if (!EasyUtils.isAppRunningForeground(appContext)) {
            EMLog.d(TAG, "app is running in background");
            for (EMMessage message : messages) {
                notificationNum++;
                fromUsers.add(message.getFrom());
            }
            handleMessage(messages.get(messages.size() - 1));
        }
    }

    public synchronized void notify(String content) {
        if (!EasyUtils.isAppRunningForeground(appContext)) {
            try {
                NotificationCompat.Builder builder = generateBaseBuilder(content);
                Notification notification = builder.build();
                notificationManager.notify(NOTIFY_ID, notification);

                if (Build.VERSION.SDK_INT < 26) {
                    vibrateAndPlayTone(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 适用于android10以后，从后台启动 Activity 的限制
     * @param fullScreenIntent
     * @param title
     * @param content
     */
    public synchronized void notify(Intent fullScreenIntent, String title, String content) {
        if (!EasyUtils.isAppRunningForeground(appContext)) {
            try {
                NotificationCompat.Builder builder = generateBaseFullIntentBuilder(fullScreenIntent, content);
                if(!TextUtils.isEmpty(title)) {
                    builder.setContentTitle(title);
                }
                Notification notification = builder.build();
                notificationManager.notify(NOTIFY_ID, notification);

                if (Build.VERSION.SDK_INT < 26) {
                    vibrateAndPlayTone(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * send it to notification bar
     * This can be override by subclass to provide customer implementation
     *
     * @param message
     */
    protected void handleMessage(EMMessage message) {
        try {
            int fromUsersNum = fromUsers.size();
            String notifyText = String.format(msg, fromUsersNum, notificationNum);

            NotificationCompat.Builder builder = generateBaseBuilder(notifyText);
            if (notificationInfoProvider != null) {
                String contentTitle = notificationInfoProvider.getTitle(message);
                if (contentTitle != null) {
                    builder.setContentTitle(contentTitle);
                }

                notifyText = notificationInfoProvider.getDisplayedText(message);
                if (notifyText != null) {
                    builder.setTicker(notifyText);
                }

                Intent i = notificationInfoProvider.getLaunchIntent(message);
                if (i != null) {
                    PendingIntent pendingIntent = PendingIntent.getActivity(appContext, NOTIFY_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);
                }

                notifyText = notificationInfoProvider.getLatestText(message, fromUsersNum, notificationNum);
                if (notifyText != null) {
                    builder.setContentText(notifyText);
                }

                int smallIcon = notificationInfoProvider.getSmallIcon(message);
                if (smallIcon != 0) {
                    builder.setSmallIcon(smallIcon);
                }
            }
            Notification notification = builder.build();
            notificationManager.notify(NOTIFY_ID, notification);

            if (Build.VERSION.SDK_INT < 26) {
                vibrateAndPlayTone(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a base Notification#Builder, contains:
     * 1.Use the app icon as default icon
     * 2.Use the app name as default title
     * 3.This notification would be sent immediately
     * 4.Can be cancelled by user
     * 5.Would launch the default activity when be clicked
     *
     * @return
     */
    private NotificationCompat.Builder generateBaseBuilder(String content) {
        PackageManager pm = appContext.getPackageManager();
        String title = pm.getApplicationLabel(appContext.getApplicationInfo()).toString();
        Intent i = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, NOTIFY_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(appContext.getApplicationInfo().icon)
                .setContentTitle(title)
                .setTicker(content)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
    }

    /**
     * Generate a base Notification#Builder to replace of start background activity.
     * @param fullScreenIntent
     * @param content
     * @return
     */
    private NotificationCompat.Builder generateBaseFullIntentBuilder(Intent fullScreenIntent, String content) {
        PackageManager pm = appContext.getPackageManager();
        String title = pm.getApplicationLabel(appContext.getApplicationInfo()).toString();
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(appContext, NOTIFY_ID,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(appContext.getApplicationInfo().icon)
                .setContentTitle(title)
                .setTicker(content)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true);
    }

    /**
     * vibrate and  play tone
     */
    public void vibrateAndPlayTone(EMMessage message) {
        if (message != null) {
            if (EaseCommonUtils.isSilentMessage(message)) {
                return;
            }
        }

        final EaseSettingsProvider settingsProvider = EaseIM.getInstance().getSettingsProvider();
        if (!settingsProvider.isMsgNotifyAllowed(null)) {
            return;
        }

        if (System.currentTimeMillis() - lastNotifyTime < 1000) {
            // received new messages within 2 seconds, skip play ringtone
            return;
        }

        try {
            lastNotifyTime = System.currentTimeMillis();

            // check if in silent mode
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                EMLog.e(TAG, "in slient mode now");
                return;
            }
            if (settingsProvider.isMsgVibrateAllowed(message)) {
                vibrator.vibrate(VIBRATION_PATTERN, -1);
            }

            if (settingsProvider.isMsgSoundAllowed(message)) {
                if (ringtone == null) {
                    Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
                    if (ringtone == null) {
                        EMLog.d(TAG, "cant find ringtone at:" + notificationUri.getPath());
                        return;
                    }
                }

                if (!ringtone.isPlaying()) {
                    String vendor = Build.MANUFACTURER;

                    ringtone.play();
                    // for samsung S3, we meet a bug that the phone will
                    // continue ringtone without stop
                    // so add below special handler to stop it after 3s if
                    // needed
                    if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                        Thread ctlThread = new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    if (ringtone.isPlaying()) {
                                        ringtone.stop();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        };
                        ctlThread.run();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * set notification info Provider
     *
     * @param provider
     */
    public void setNotificationInfoProvider(EaseNotificationInfoProvider provider) {
        notificationInfoProvider = provider;
    }

    public interface EaseNotificationInfoProvider {
        /**
         * set the notification content, such as "you received a new image from xxx"
         *
         * @param message
         * @return null-will use the default text
         */
        String getDisplayedText(EMMessage message);

        /**
         * set the notification content: such as "you received 5 message from 2 contacts"
         *
         * @param message
         * @param fromUsersNum- number of message sender
         * @param messageNum    -number of messages
         * @return null-will use the default text
         */
        String getLatestText(EMMessage message, int fromUsersNum, int messageNum);

        /**
         * 设置notification标题
         *
         * @param message
         * @return null- will use the default text
         */
        String getTitle(EMMessage message);

        /**
         * set the small icon
         *
         * @param message
         * @return 0- will use the default icon
         */
        int getSmallIcon(EMMessage message);

        /**
         * set the intent when notification is pressed
         *
         * @param message
         * @return null- will use the default icon
         */
        Intent getLaunchIntent(EMMessage message);
    }
}
