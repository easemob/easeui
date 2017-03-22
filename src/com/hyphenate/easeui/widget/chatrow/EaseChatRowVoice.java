package com.hyphenate.easeui.widget.chatrow;

import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.util.EMLog;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EaseChatRowVoice extends EaseChatRowFile{

    private ImageView voiceImageView;
    private TextView voiceHintTextView;
    private TextView voiceLengthView;
    private ImageView readStatusView;

    public EaseChatRowVoice(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_voice : R.layout.ease_row_sent_voice, this);
    }

    @Override
    protected void onFindViewById() {
        voiceImageView = ((ImageView) findViewById(R.id.iv_voice));
        voiceHintTextView = (TextView) findViewById(R.id.text_hint);
        voiceLengthView = (TextView) findViewById(R.id.tv_length);
        readStatusView = (ImageView) findViewById(R.id.iv_unread_voice);
    }

    @Override
    protected void onSetUpView() {
        EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
        int len = voiceBody.getLength();
        if(len>0){
            voiceLengthView.setText(voiceBody.getLength() + "\"");
            voiceLengthView.setVisibility(View.VISIBLE);
        }else{
            voiceLengthView.setVisibility(View.INVISIBLE);
        }
        if (EaseChatRowVoicePlayClickListener.playMsgId != null
                && EaseChatRowVoicePlayClickListener.playMsgId.equals(message.getMsgId()) && EaseChatRowVoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (message.direct() == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(R.anim.voice_from_icon);
            } else {
                voiceImageView.setImageResource(R.anim.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) voiceImageView.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.direct() == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(R.drawable.ease_chatfrom_voice_playing);
            } else {
                voiceImageView.setImageResource(R.drawable.ease_chatto_voice_playing);
            }
        }
        
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (message.isListened()) {
                // hide the unread icon
                readStatusView.setVisibility(View.INVISIBLE);
            } else {
                readStatusView.setVisibility(View.VISIBLE);
            }
            EMLog.d(TAG, "it is receive msg");
            if (voiceBody.downloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    voiceBody.downloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
                progressBar.setVisibility(View.VISIBLE);
                setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.INVISIBLE);

            }
            if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_BURN, false)
                    && message.direct() == EMMessage.Direct.RECEIVE){
                if(message.isListened()){
                    voiceImageView.setVisibility(View.VISIBLE);
                    voiceHintTextView.setVisibility(View.GONE);
                }else{
                    voiceHintTextView.setVisibility(View.VISIBLE);
                    voiceHintTextView.setText(R.string.attach_burn);
                    voiceImageView.setVisibility(View.GONE);
                }
            }else{
                voiceHintTextView.setVisibility(View.GONE);
                voiceImageView.setVisibility(View.VISIBLE);
            }
            return;
        }

        // until here, handle sending voice message
        handleSendMessage();
    }

    @Override
    protected void onUpdateView() {
        // 这里必须进行强转一下然后调用adapter的 refresh方法，
        if (adapter instanceof EaseMessageAdapter) {
            ((EaseMessageAdapter) adapter).refresh();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onBubbleClick() {
        if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_BURN, false)
                && message.direct() == EMMessage.Direct.RECEIVE){
            voiceImageView.setVisibility(View.VISIBLE);
            voiceHintTextView.setVisibility(View.GONE);
            onUpdateView();
        }
        new EaseChatRowVoicePlayClickListener(message, voiceImageView, readStatusView, adapter, activity).onClick(bubbleLayout);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EaseChatRowVoicePlayClickListener.currentPlayListener != null && EaseChatRowVoicePlayClickListener.isPlaying) {
            EaseChatRowVoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }
    }
    
}
