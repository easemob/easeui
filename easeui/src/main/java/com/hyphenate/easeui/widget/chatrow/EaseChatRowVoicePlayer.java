package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.EaseUI;

import java.io.IOException;

/**
 * Created by zhangsong on 17-10-20.
 */

public class EaseChatRowVoicePlayer {
    private static final String TAG = "ConcurrentMediaPlayer";

    private static EaseChatRowVoicePlayer instance = null;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private String playingId;

    private MediaPlayer.OnCompletionListener onCompletionListener;

    public static EaseChatRowVoicePlayer getInstance(Context context) {
        if (instance == null) {
            synchronized (EaseChatRowVoicePlayer.class) {
                if (instance == null) {
                    instance = new EaseChatRowVoicePlayer(context);
                }
            }
        }
        return instance;
    }

    public MediaPlayer getPlayer() {
        return mediaPlayer;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * May null, please consider.
     *
     * @return
     */
    public String getCurrentPlayingId() {
        return playingId;
    }

    public void play(final EMMessage msg, final MediaPlayer.OnCompletionListener listener) {
        if (!(msg.getBody() instanceof EMVoiceMessageBody)) return;

        if (mediaPlayer.isPlaying()) {
            stop();
        }

        playingId = msg.getMsgId();
        onCompletionListener = listener;

        try {
            setSpeaker();
            EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) msg.getBody();
            mediaPlayer.setDataSource(voiceBody.getLocalUrl());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();

                    playingId = null;
                    onCompletionListener = null;
                }
            });
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.reset();

        /**
         * This listener is to stop the voice play animation currently, considered the following 3 conditions:
         *
         * 1.A new voice item is clicked to play, to stop the previous playing voice item animation.
         * 2.The voice is play complete, to stop it's voice play animation.
         * 3.Press the voice record button will stop the voice play and must stop voice play animation.
         *
         */
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion(mediaPlayer);
        }
    }

    private EaseChatRowVoicePlayer(Context cxt) {
        Context baseContext = cxt.getApplicationContext();
        audioManager = (AudioManager) baseContext.getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
    }

    private void setSpeaker() {
        boolean speakerOn = EaseUI.getInstance().getSettingsProvider().isSpeakerOpened();
        if (speakerOn) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);// 关闭扬声器
            // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
    }
}
