package com.hyphenate.easeui.model;

import android.content.Context;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.util.LruCache;

import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.adapter.message.EMAMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.hyphenate.easeui.model.EaseDingMessageHelper.KEY_CONVERSATION_ID;
import static com.hyphenate.easeui.model.EaseDingMessageHelper.KEY_DING;
import static com.hyphenate.easeui.model.EaseDingMessageHelper.KEY_DING_ACK;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by zhangsong on 18-1-25.
 */
public class EaseDingMessageHelperTest {
    private EaseDingMessageHelper helper;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        assertNotNull(context);
        helper = new EaseDingMessageHelper(context);
        assertNotNull(helper);

        // Load this for EMMessage native creation.
        System.loadLibrary("hyphenate");
    }

    @Test
    public void testHandleAckMessage() throws Exception {
        assertNotNull(helper);

        // Data will store in memory.
        helper.handleAckMessage(createDingAckMessage("1", "-1", "user1"));
        assertEquals(helper.getDataCache().size(), 1);
        LruCache<String, List<String>> msgCache = helper.getDataCache().get("1");
        assertNotNull(msgCache);
        List<String> userList = msgCache.get("-1");
        assertNotNull(userList);
        assertEquals(userList.size(), 1);
        assertEquals(userList.get(0), "user1");
        // Data will store in SharedPreferences too.
        assertTrue(helper.getDataPrefs().contains(helper.generateKey("1", "-1")));
    }

    @Test
    public void testGetAckUsers() throws Exception {
        // Clear memory cache first.
        helper.getDataCache().evictAll();
        assertEquals(helper.getDataCache().size(), 0);
        // After this action, the data will store in memory.
        helper.getAckUsers(createDingMessage("1", "-1"));
        assertEquals(helper.getDataCache().size(), 1);
    }

    @Test
    public void testDelete() throws Exception {
        testHandleAckMessage();

        helper.delete(createDingMessage("1", "-1"));
        // Data removed from memory.
        LruCache<String, List<String>> msgCache = helper.getDataCache().get("1");
        assertTrue(msgCache.get("-1") == null);
        // Data removed from SharedPreferences.
        assertTrue(!helper.getDataPrefs().contains(helper.generateKey("1", "-1")));
    }

    @Test
    public void testLruCache() throws Exception {
        /**
         * {@link EaseDingMessageHelper#dataCache} dataCache can contains {@link EaseDingMessageHelper#CACHE_SIZE_CONVERSATION}
         * size of conversations at most.
         */
        // Clear memory cache first.
        helper.getDataCache().evictAll();
        // Create size + 1 ding-type ack msg and add to dataCache.
        for (int i = 0; i <= EaseDingMessageHelper.CACHE_SIZE_CONVERSATION; i++) {
            helper.handleAckMessage(createDingAckMessage(String.valueOf(i), String.valueOf(-i), String.valueOf("user" + i)));
        }
        assertEquals(helper.getDataCache().size(), EaseDingMessageHelper.CACHE_SIZE_CONVERSATION);

        /**
         * Every conversation can contains {@link EaseDingMessageHelper#CACHE_SIZE_MESSAGE} size of ding-type msg at most.
         */
        // Clear memory cache first.
        helper.getDataCache().evictAll();
        for (int i = 0; i <= EaseDingMessageHelper.CACHE_SIZE_MESSAGE; i++) {
            helper.handleAckMessage(createDingAckMessage("1", String.valueOf(-i), String.valueOf("user" + i)));
        }
        LruCache<String, List<String>> msgCache = helper.getDataCache().get("1");
        assertEquals(msgCache.size(), EaseDingMessageHelper.CACHE_SIZE_MESSAGE);
    }

    @Test
    public void testWeakReference() throws Exception {
        for (int i = 0; i < 10; i++) {
            helper.setUserUpdateListener(createDingMessage(String.valueOf(i), String.valueOf(-i)), new EaseDingMessageHelper.IAckUserUpdateListener() {
                @Override
                public void onUpdate(List<String> list) {

                }
            });
        }

        Runtime.getRuntime().gc();

        SystemClock.sleep(1000);

        WeakReference<EaseDingMessageHelper.IAckUserUpdateListener> listenerRefs =
                helper.getListenerMap().get("-1");
        System.out.println("listenerRefs get() -> " + listenerRefs.get());

        assertTrue(true);
    }

    @After
    public void tearDown() throws Exception {
    }

    private EMMessage createDingMessage(String conversationId, String originalMsgId) {
        EMAMessage _msg = EMAMessage.createSendMessage("", "", null, EMMessage.ChatType.Chat.ordinal());
        _msg.setTo(conversationId);
        EMMessage msg = new EMMessage(_msg);
        msg.setMsgId(originalMsgId);
        EMTextMessageBody txtBody = new EMTextMessageBody("1");
        msg.addBody(txtBody);
        msg.setAttribute(KEY_DING, true);
        msg.setChatType(EMMessage.ChatType.GroupChat);
        return msg;
    }

    private EMMessage createDingAckMessage(String conversationId, String originalMsgId, String from) {
        EMAMessage _msg = EMAMessage.createSendMessage(from, "", null, EMMessage.ChatType.Chat.ordinal());
        EMMessage msg = new EMMessage(_msg);
        msg.setAttribute(KEY_CONVERSATION_ID, conversationId);
        msg.setAttribute(KEY_DING_ACK, true);
        msg.addBody(new EMCmdMessageBody(originalMsgId));
        return msg;
    }
}