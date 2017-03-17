package com.hyphenate.easeui.utils;

import android.text.TextUtils;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.EaseConstant;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lzan13
 * 会话扩展处理类，用来处理会话对象的扩展信息，
 * 包括：
 * 会话置顶，
 * 会话最后操作时间，
 * 会话草稿，
 * TODO 群组@，
 * TODO 会话名称
 */
public class EaseConversationExtUtils {

    /**
     * 会话实体类构造函数
     */
    private EaseConversationExtUtils() {

    }

    /**
     * 设置会话置顶状态
     *
     * @param conversation 要置顶的会话对象
     * @param top      设置会话是否置顶
     */
    public static void setConversationTop(EMConversation conversation, boolean top) {
        // 获取当前会话对象的扩展
        String ext = conversation.getExtField();
        JSONObject jsonObject = null;
        try {
            // 判断扩展内容是否为空
            if (TextUtils.isEmpty(ext)) {
                jsonObject = new JSONObject();
            } else {
                jsonObject = new JSONObject(ext);
            }
            // 将扩展信息设置给外层的 JSONObject 对象
            jsonObject.put(EaseConstant.CONVERSATION_TOP, top);
            // 将扩展信息保存到 Conversation 对象的扩展中去
            conversation.setExtField(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前会话是否置顶
     *
     * @param conversation 需要操作的会话对象
     * @return 返回当前会话是否置顶
     */
    public static boolean getConversationTop(EMConversation conversation) {
        // 获取当前会话对象的扩展
        String ext = conversation.getExtField();
        // 判断扩展内容是否为空
        if (TextUtils.isEmpty(ext)) {
            return false;
        }
        try {
            // 根据扩展获取Json对象，然后获取置顶的属性，
            JSONObject jsonObject = new JSONObject(ext);
            return jsonObject.optBoolean(EaseConstant.CONVERSATION_TOP);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置当前会话草稿
     *
     * @param conversation 需要设置的会话对象
     * @param draft        需要设置的草稿内容
     */
    public static void setConversationDraft(EMConversation conversation, String draft) {
        // 获取当前会话对象的扩展
        String ext = conversation.getExtField();
        JSONObject jsonObject = null;
        try {
            // 判断扩展内容是否为空
            if (TextUtils.isEmpty(ext)) {
                jsonObject = new JSONObject();
            } else {
                jsonObject = new JSONObject(ext);
            }
            // 将扩展信息设置给 JSONObject 对象
            jsonObject.put(EaseConstant.CONVERSATION_DRAFT, draft);
            // 将扩展信息保存到 EMConversation 对象扩展中去
            conversation.setExtField(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前会话的草稿内容
     *
     * @param conversation 当前会话
     * @return 返回草稿内容
     */
    public static String getConversationDraft(EMConversation conversation) {
        // 获取当前会话对象的扩展
        String ext = conversation.getExtField();
        JSONObject jsonObject = null;
        try {
            // 判断扩展内容是否为空
            if (TextUtils.isEmpty(ext)) {
                jsonObject = new JSONObject();
            } else {
                jsonObject = new JSONObject(ext);
            }
            // 根据扩展的key获取扩展的值
            return jsonObject.optString(EaseConstant.CONVERSATION_DRAFT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

}
