package com.hyphenate.easeui.viewholder;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVoice;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVoicePlayer;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.io.File;

import static com.hyphenate.chat.EMClient.TAG;

public class EaseVoiceViewHolder extends EaseChatRowViewHolder{
    private EaseChatRowVoicePlayer voicePlayer;

    public EaseVoiceViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
        voicePlayer = EaseChatRowVoicePlayer.getInstance(getContext());
    }

    public static EaseChatRowViewHolder create(ViewGroup parent,
                                               boolean isSender, MessageListItemClickListener itemClickListener) {
        return new EaseVoiceViewHolder(new EaseChatRowVoice(parent.getContext(), isSender), itemClickListener);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        super.onBubbleClick(message);
        String msgId = message.getMsgId();

        if (voicePlayer.isPlaying()) {
            // Stop the voice play first, no matter the playing voice item is this or others.
            voicePlayer.stop();
            // Stop the voice play animation.
            ((EaseChatRowVoice) getChatRow()).stopVoicePlayAnimation();

            // If the playing voice item is this item, only need stop play.
            String playingId = voicePlayer.getCurrentPlayingId();
            if (msgId.equals(playingId)) {
                return;
            }
        }

        if (message.direct() == EMMessage.Direct.SEND) {
            // Play the voice
            String localPath = ((EMVoiceMessageBody) message.getBody()).getLocalUrl();
            File file = new File(localPath);
            if (file.exists() && file.isFile()) {
                playVoice(message);
                // Start the voice play animation.
                ((EaseChatRowVoice) getChatRow()).startVoicePlayAnimation();
            } else {
                asyncDownloadVoice(message);
            }
        } else {
            final String st = getContext().getResources().getString(R.string.Is_download_voice_click_later);
            if (message.status() == EMMessage.Status.SUCCESS) {
                if (EMClient.getInstance().getOptions().getAutodownloadThumbnail()) {
                    play(message);
                } else {
                    EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
                    EMLog.i(TAG, "Voice body download status: " + voiceBody.downloadStatus());
                    switch (voiceBody.downloadStatus()) {
                        case PENDING:// Download not begin
                        case FAILED:// Download failed
                            getChatRow().updateView(message);
                            asyncDownloadVoice(message);
                            break;
                        case DOWNLOADING:// During downloading
                            Toast.makeText(getContext(), st, Toast.LENGTH_SHORT).show();
                            break;
                        case SUCCESSED:// Download success
                            play(message);
                            break;
                    }
                }
            } else if (message.status() == EMMessage.Status.INPROGRESS) {
                Toast.makeText(getContext(), st, Toast.LENGTH_SHORT).show();
            } else if (message.status() == EMMessage.Status.FAIL) {
                Toast.makeText(getContext(), st, Toast.LENGTH_SHORT).show();
                asyncDownloadVoice(message);
            }
        }
    }

    private void playVoice(EMMessage msg) {
        voicePlayer.play(msg, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Stop the voice play animation.
                ((EaseChatRowVoice) getChatRow()).stopVoicePlayAnimation();
            }
        });
    }

    private void asyncDownloadVoice(final EMMessage message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                EMClient.getInstance().chatManager().downloadAttachment(message);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                getChatRow().updateView(message);
            }
        }.execute();
    }

    private void play(EMMessage message) {
        String localPath = ((EMVoiceMessageBody) message.getBody()).getLocalUrl();
        File file = new File(localPath);
        if (file.exists() && file.isFile()) {
            ackMessage(message);
            playVoice(message);
            // Start the voice play animation.
            ((EaseChatRowVoice) getChatRow()).startVoicePlayAnimation();
        } else {
            EMLog.e(TAG, "file not exist");
        }
    }

    private void ackMessage(EMMessage message) {
        EMMessage.ChatType chatType = message.getChatType();
        if (!message.isAcked() && chatType == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        if (!message.isListened()) {
            EMClient.getInstance().chatManager().setVoiceMessageListened(message);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(voicePlayer.isPlaying()) {
            voicePlayer.stop();
            voicePlayer.release();
        }
    }
}
