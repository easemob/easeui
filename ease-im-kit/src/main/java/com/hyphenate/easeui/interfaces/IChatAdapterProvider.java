package com.hyphenate.easeui.interfaces;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.adapter.EaseBaseMessageAdapter;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;

public interface IChatAdapterProvider {
    /**
     * provide chat message's adapter
     * if is null , will use default {@link com.hyphenate.easeui.adapter.EaseMessageAdapter}
     * @return
     */
    EaseBaseMessageAdapter<EMMessage> provideMessageAdaper();
}
