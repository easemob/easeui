package com.hyphenate.easeui.interfaces;

import android.text.SpannableString;

import com.hyphenate.chat.EMMessage;

/**
 * Quote message provider
 */
public interface ChatQuoteMessageProvider {
    /**
     * Provide the content to display by quoting the message, the sender of the quoting message, and the quoting content.
     * @param quoteMessage  The local quote message, may be null.
     * @param quoteMsgType  The type of quote message.
     * @param quoteSender   The sender of quote message.
     * @param quoteContent  The content of quote message.
     * @return
     */
    SpannableString providerQuoteMessageContent(EMMessage quoteMessage, EMMessage.Type quoteMsgType, String quoteSender, String quoteContent);
}
