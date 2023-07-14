package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMTranslationResult;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.manager.EaseDingMessageHelper;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.widget.EaseChatQuoteView;
import com.hyphenate.util.EMLog;

import org.json.JSONException;
import org.json.JSONObject;

public class EaseChatRowText extends EaseChatRow {
	private TextView contentView;
    private TextView translationContentView;
    private ImageView translationStatusView;
    private View translationContainer;
    private EaseChatQuoteView quoteView;

    public EaseChatRowText(Context context, boolean isSender) {
		super(context, isSender);
	}

    public EaseChatRowText(Context context, EMMessage message, int position, Object adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflateView() {
		inflater.inflate(!showSenderType ? R.layout.ease_row_received_message
                : R.layout.ease_row_sent_message, this);
	}

	@Override
	protected void onFindViewById() {
		contentView = (TextView) findViewById(R.id.tv_chatcontent);
        translationContentView = (TextView) findViewById(R.id.tv_subContent);
        translationStatusView = (ImageView) findViewById(R.id.translation_status);
        translationContainer = (View) findViewById(R.id.subBubble);
        quoteView = (EaseChatQuoteView)findViewById(R.id.chat_quote_view);
	}

    @Override
    public void onSetUpView() {
        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        if(txtBody != null){
            Spannable span = EaseSmileUtils.getSmiledText(context, txtBody.getMessage());
            // 设置内容
            contentView.setText(span, BufferType.SPANNABLE);
            contentView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    contentView.setTag(R.id.action_chat_long_click,true);
                    if (itemClickListener != null) {
                        return itemClickListener.onBubbleLongClick(v, message);
                    }
                    return false;
                }
            });
            replaceSpan();
            EMTranslationResult result = EMClient.getInstance().translationManager().getTranslationResult(message.getMsgId());
            if(result != null){
                if(result.showTranslation()) {
                    translationContainer.setVisibility(View.VISIBLE);
                    translationContentView.setText(result.translatedText());
                    translationContainer.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            contentView.setTag(R.id.action_chat_long_click,true);
                            if (itemClickListener != null) {
                                return itemClickListener.onBubbleLongClick(v, message);
                            }
                            return false;
                        }
                    });
                    translationStatusView.setImageResource(R.drawable.translation_success);
                } else {
                    translationContainer.setVisibility(View.GONE);
                }
            } else {
                translationContainer.setVisibility(View.GONE);
            }
        }

        quoteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener == null){
                    return;
                }
                String msgQuote = message.getStringAttribute(EaseConstant.QUOTE_MSG_QUOTE,"");
                if (!TextUtils.isEmpty(msgQuote)){
                    try {
                        JSONObject jsonObject = new JSONObject(msgQuote);
                        String quoteMsgID = jsonObject.getString(EaseConstant.QUOTE_MSG_ID);
                        EMMessage showMsg = EMClient.getInstance().chatManager().getMessage(quoteMsgID);
                        if(showMsg == null) {
                            itemClickListener.onMessageError(null, EMError.GENERAL_ERROR, context.getString(R.string.ease_error_message_not_exist));
                            return;
                        }
                        itemClickListener.onQuoteViewClick(showMsg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        quoteView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    return itemClickListener.onQuoteViewLongClick(v, message);
                }
                return false;
            }
        });

    }

    /**
     * 解决长按事件与relink冲突，参考：https://www.jianshu.com/p/d3bef8449960
     */
    private void replaceSpan() {
        Spannable spannable = (Spannable) contentView.getText();
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (int i = 0; i < spans.length; i++) {
            String url = spans[i].getURL();
            int index = spannable.toString().indexOf(url);
            int end = index + url.length();
            if (index == -1) {
                if (url.contains("http://")) {
                    url = url.replace("http://", "");
                } else if (url.contains("https://")) {
                    url = url.replace("https://", "");
                } else if (url.contains("rtsp://")) {
                    url = url.replace("rtsp://", "");
                }
                index = spannable.toString().indexOf(url);
                end = index + url.length();
            }
            if (index != -1) {
                spannable.removeSpan(spans[i]);
                spannable.setSpan(new AutolinkSpan(spans[i].getURL()), index
                        , end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
    }

    @Override
    protected void onMessageCreate() {
        setStatus(View.VISIBLE, View.GONE);
    }

    @Override
    protected void onMessageSuccess() {
        setStatus(View.GONE, View.GONE);

        // Show "1 Read" if this msg is a ding-type msg.
        if (isSender() && EaseDingMessageHelper.get().isDingMessage(message) && ackedView != null) {
            ackedView.setVisibility(VISIBLE);
            int count = message.groupAckCount();
            ackedView.setText(String.format(getContext().getString(R.string.group_ack_read_count), count));
        }

        // Set ack-user list change listener.
        // Only use the group ack count from message. - 2022.04.27
        //EaseDingMessageHelper.get().setUserUpdateListener(message, userUpdateListener);
        onSetUpQuoteView(message);
    }


    @Override
    protected void onMessageError() {
        super.onMessageError();
        setStatus(View.GONE, View.VISIBLE);
    }

    @Override
    protected void onMessageInProgress() {
        setStatus(View.VISIBLE, View.GONE);
    }

    public void onSetUpQuoteView(EMMessage message) {
        if(quoteView == null) {
            EMLog.e(TAG, "view is null, don't setup quote view");
            return;
        }
        quoteView.setVisibility(GONE);
        if (null == message) {
            EMLog.e(TAG, "message is null, don't setup quote view");
            return;
        }
        quoteView.clear();
        quoteView.updateMessageInfo(message);
    }

    /**
     * set progress and status view visible or gone
     * @param progressVisible
     * @param statusVisible
     */
    private void setStatus(int progressVisible, int statusVisible) {
        if(progressBar != null) {
            progressBar.setVisibility(progressVisible);
        }
        if(statusView != null) {
            statusView.setVisibility(statusVisible);
        }
    }

    private EaseDingMessageHelper.IAckUserUpdateListener userUpdateListener = list -> onAckUserUpdate(list.size());

    public void onAckUserUpdate(final int count) {
        if(ackedView == null) {
            return;
        }
        ackedView.post(()->{
            if (isSender()) {
                ackedView.setVisibility(VISIBLE);
                ackedView.setText(String.format(getContext().getString(R.string.group_ack_read_count), count));
            }
        });
    }
}
