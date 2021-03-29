package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.manager.EaseDingMessageHelper;
import com.hyphenate.easeui.utils.EaseSmileUtils;

public class EaseChatRowText extends EaseChatRow {
	private TextView contentView;

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
	}

    @Override
    public void onSetUpView() {
        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        //解析html超链接
        String content = txtBody.getMessage().replace("\n", "<br />");
        //fromHtml method will ignore \n in string
        CharSequence htmpTxt;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            htmpTxt = Html.fromHtml(content.replace("<", "&lt;"), Html.FROM_HTML_MODE_LEGACY);
        } else {
            htmpTxt = Html.fromHtml(content.replace("<", "&lt;"));
        }

        String new_content = htmpTxt.toString().replace("<br />", "\n");
        //解析表情
        Spannable span = EaseSmileUtils.getSmiledText(context, new_content);

        //给超链接添加响应
        URLSpan[] urlSpans = span.getSpans(0, htmpTxt.length(), URLSpan.class);
        for (URLSpan span1 : urlSpans) {
            int start = span.getSpanStart(span1);
            int end = span.getSpanEnd(span1);
            int flag = span.getSpanFlags(span1);
            final String link = span1.getURL();
            span.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    //打开超链接
                    if (link != null && link.startsWith("http")) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(link);
                        intent.setData(content_url);
                        getContext().startActivity(intent);
                    }
                }
            }, start, end, flag);
            span.removeSpan(span1);
        }

        contentView.setLinksClickable(true);
        contentView.setMovementMethod(LinkMovementMethod.getInstance());
        // 设置内容
        contentView.setText(span, TextView.BufferType.SPANNABLE);
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
        EaseDingMessageHelper.get().setUserUpdateListener(message, userUpdateListener);
    }

    @Override
    protected void onMessageError() {
        setStatus(View.GONE, View.VISIBLE);
    }

    @Override
    protected void onMessageInProgress() {
        setStatus(View.VISIBLE, View.GONE);
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
