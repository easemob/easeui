package com.hyphenate.easeui.manager;

import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.constants.EaseConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EaseSystemMsgManager {
    private static EaseSystemMsgManager instance;

    private EaseSystemMsgManager(){}

    public static EaseSystemMsgManager getInstance() {
        if(instance == null) {
            synchronized (EaseSystemMsgManager.class) {
                if(instance == null) {
                    instance = new EaseSystemMsgManager();
                }
            }
        }
        return instance;
    }

    /**
     * 创建系统消息
     * @param message
     * @param ext
     * @return
     */
    public EMMessage createMessage(String message, Map<String, Object> ext) {
        EMMessage emMessage = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        emMessage.setFrom(EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID);
        emMessage.setMsgId(UUID.randomUUID().toString());
        emMessage.setStatus(EMMessage.Status.SUCCESS);
        emMessage.addBody(new EMTextMessageBody(message));
        if(ext != null && !ext.isEmpty()) {
            Iterator<String> iterator = ext.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object value = ext.get(key);
                putObject(emMessage, key, value);
            }
        }
        emMessage.setUnread(true);
        EMClient.getInstance().chatManager().saveMessage(emMessage);
        return emMessage;
    }

    private void putObject(EMMessage message, String key, Object value) {
        if(TextUtils.isEmpty(key)) {
            return;
        }
        if(value instanceof String) {
            message.setAttribute(key, (String) value);
        }else if(value instanceof Byte) {
            message.setAttribute(key, (Integer) value);
        }else if(value instanceof Character) {
            message.setAttribute(key, (Integer) value);
        }else if(value instanceof Short) {
            message.setAttribute(key, (Integer) value);
        }else if(value instanceof Integer) {
            message.setAttribute(key, (Integer) value);
        }else if(value instanceof Boolean) {
            message.setAttribute(key, (Boolean) value);
        }else if(value instanceof Long) {
            message.setAttribute(key, (Long) value);
        }else if(value instanceof Float) {
            JSONObject object = new JSONObject();
            try {
                object.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            message.setAttribute(key, object);
        }else if(value instanceof Double) {
            JSONObject object = new JSONObject();
            try {
                object.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            message.setAttribute(key, object);
        }else if(value instanceof JSONObject) {
            message.setAttribute(key, (JSONObject) value);
        }else if(value instanceof JSONArray) {
            message.setAttribute(key, (JSONArray) value);
        }else {
            JSONObject object = new JSONObject();
            try {
                object.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            message.setAttribute(key, object);
        }
    }

    /**
     * 创建ext map
     * @return
     */
    public Map<String, Object> createMsgExt() {
        return new HashMap<>();
    }

    /**
     * 获取最近一条消息
     * @param con
     * @return
     */
    public EMMessage getLastMessageByConversation(EMConversation con) {
        if(con == null) {
            return null;
        }
        return con.getLastMessage();
    }

    /**
     * 获取系统会话
     * @return
     */
    public EMConversation getConversation() {
        return getConversation(true);
    }

    /**
     * 获取系统会话
     * @param createIfNotExists
     * @return
     */
    public EMConversation getConversation(boolean createIfNotExists) {
        return EMClient.getInstance().chatManager().getConversation(EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID
                , EMConversation.EMConversationType.Chat, createIfNotExists);
    }

    /**
     * 获取所有会话消息
     * @return
     */
    public List<EMMessage> getAllMessages() {
        return getConversation().getAllMessages();
    }

    /**
     * 判断是否是系统消息
     * @param message
     * @return
     */
    public boolean isSystemMessage(EMMessage message) {
        return message.getType() == EMMessage.Type.TXT
                && TextUtils.equals(message.getFrom(), EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID);
    }

    /**
     * 判断是否是系统会话
     * @param conversation
     * @return
     */
    public boolean isSystemConversation(EMConversation conversation) {
        return conversation.getType() == EMConversation.EMConversationType.Chat
                && TextUtils.equals(conversation.conversationId(), EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID);
    }

    /**
     * 获取系统消息消息体
     * @param message
     * @return
     */
    public String getMessageContent(EMMessage message) {
        return ((EMTextMessageBody)message.getBody()).getMessage();
    }

    /**
     * 更新系统消息
     * @param message
     * @return
     */
    public boolean updateMessage(EMMessage message) {
        if(message == null || !isSystemMessage(message)) {
            return false;
        }
        EMClient.getInstance().chatManager().updateMessage(message);
        return true;
    }

    /**
     * 移除系统消息
     * @param message
     * @return
     */
    public boolean removeMessage(EMMessage message) {
        if(message == null || !isSystemMessage(message)) {
            return false;
        }
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID);
        conversation.removeMessage(message.getMsgId());
        return true;
    }

    /**
     * 将会话置为已读
     */
    public void markAllMessagesAsRead() {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID);
        conversation.markAllMessagesAsRead();
    }
}

