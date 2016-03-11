package com.easemob.easeui.widget.chatrow;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseMessageAdapter;
import com.easemob.easeui.utils.EaseACKUtil;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;

public class EaseChatRowVoice extends EaseChatRowFile{

    private ImageView voiceImageView;
    private TextView voiceHintTextView;
    private TextView voiceLengthView;
    private ImageView readStutausView;

    public EaseChatRowVoice(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_voice : R.layout.ease_row_sent_voice, this);
    }

    @Override
    protected void onFindViewById() {
        voiceImageView = ((ImageView) findViewById(R.id.iv_voice));
        voiceHintTextView = (TextView) findViewById(R.id.text_hint);
        voiceLengthView = (TextView) findViewById(R.id.tv_length);
        readStutausView = (ImageView) findViewById(R.id.iv_unread_voice);
    }

    @Override
    protected void onSetUpView() {
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
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
            if (message.direct == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(R.drawable.voice_from_icon);
            } else {
                voiceImageView.setImageResource(R.drawable.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) voiceImageView.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                voiceImageView.setImageResource(R.drawable.ease_chatfrom_voice_playing);
            } else {
                voiceImageView.setImageResource(R.drawable.ease_chatto_voice_playing);
            }
        }
        
        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.isListened()) {
                // 隐藏语音未听标志
                readStutausView.setVisibility(View.INVISIBLE);
            } else {
                readStutausView.setVisibility(View.VISIBLE); 
            }
            EMLog.d(TAG, "it is receive msg");
            if (message.status == EMMessage.Status.INPROGRESS) {
                progressBar.setVisibility(View.VISIBLE);
                setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
            if(message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
                    && message.direct == Direct.RECEIVE){
            	if(message.isListened()){
                    voiceImageView.setVisibility(View.VISIBLE);
                    voiceHintTextView.setVisibility(View.GONE);
            	}else{
                    voiceHintTextView.setVisibility(View.VISIBLE);
                    voiceHintTextView.setText(R.string.readfire_message);
                    voiceImageView.setVisibility(View.GONE);
            	}
            }else{
                voiceHintTextView.setVisibility(View.GONE);
                voiceImageView.setVisibility(View.VISIBLE);
            }
            return;
        }

        // until here, deal with send voice msg
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
        if(message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
                && message.direct == Direct.RECEIVE){
            voiceImageView.setVisibility(View.VISIBLE);
            voiceHintTextView.setVisibility(View.GONE);
            onUpdateView();
        }
        new EaseChatRowVoicePlayClickListener(message, voiceImageView, readStutausView, adapter, activity).onClick(bubbleLayout);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EaseChatRowVoicePlayClickListener.currentPlayListener != null && EaseChatRowVoicePlayClickListener.isPlaying) {
            // 停止语音播放
            EaseChatRowVoicePlayClickListener.currentPlayListener.stopPlayVoice();
        }
    }

}
