/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.easeui.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;

public class EaseCommonUtils {
    private static final String TAG = "CommonUtils";

    /**
     * 检测网络是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }

        return false;
    }

    /**
     * 检测Sdcard是否存在
     * 
     * @return
     */
    public static boolean isExitsSdcard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    public static EMMessage createExpressionMessage(String toChatUsername, String expressioName, String identityCode) {
        EMMessage message = EMMessage.createTxtSendMessage("[" + expressioName + "]", toChatUsername);
        if (identityCode != null) {
            message.setAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, identityCode);
        }
        message.setAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, true);
        return message;
    }

    /**
     * 根据消息内容和消息类型获取消息内容提示
     * 
     * @param message
     * @param context
     * @return
     */
    public static String getMessageDigest(EMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
        case LOCATION: // 位置消息
            if (message.direct == EMMessage.Direct.RECEIVE) {
                // 从sdk中提到了ui中，使用更简单不犯错的获取string方法
                // digest = EasyUtils.getAppResourceString(context,
                // "location_recv");
                digest = getString(context, R.string.location_recv);
                digest = String.format(digest, message.getFrom());
                return digest;
            } else {
                // digest = EasyUtils.getAppResourceString(context,
                // "location_prefix");
                digest = getString(context, R.string.location_prefix);
            }
            break;
        case IMAGE: // 图片消息
            digest = getString(context, R.string.picture);
            break;
        case VOICE:// 语音消息
            digest = getString(context, R.string.voice_prefix);
            break;
        case VIDEO: // 视频消息
            digest = getString(context, R.string.video);
            break;
        case TXT: // 文本消息
            TextMessageBody txtBody = (TextMessageBody) message.getBody();
            /*
             * if(((DemoHXSDKHelper)HXSDKHelper.getInstance()).
             * isRobotMenuMessage(message)){ digest =
             * ((DemoHXSDKHelper)HXSDKHelper.getInstance()).
             * getRobotMenuMessageDigest(message); }else
             */if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                digest = getString(context, R.string.voice_call) + txtBody.getMessage();
            } else if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                if (!TextUtils.isEmpty(txtBody.getMessage())) {
                    digest = txtBody.getMessage();
                } else {
                    digest = getString(context, R.string.dynamic_expression);
                }
            } else {
                digest = txtBody.getMessage();
            }
            break;
        case FILE: // 普通文件消息
            digest = getString(context, R.string.file);
            break;
        default:
            EMLog.e(TAG, "error, unknow type");
            return "";
        }

        return digest;
    }

    static String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    /**
     * 获取栈顶的activity
     * 
     * @param context
     * @return
     */
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null)
            return runningTaskInfos.get(0).topActivity.getClassName();
        else
            return "";
    }

    /**
     * 设置user昵称(没有昵称取username)的首字母属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...
     * 字母栏快速定位联系人
     * 
     * @param username
     * @param user
     */
    public static void setUserInitialLetter(EaseUser user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (Character.isDigit(headerName.charAt(0))) {
            user.setInitialLetter("#");
        } else {
            user.setInitialLetter(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target
                    .substring(0, 1).toUpperCase());
            char header = user.getInitialLetter().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setInitialLetter("#");
            }
        }
    }

    /**
     * 发送一条撤回消息的透传，这里需要和接收方协商定义，通过一个透传，并加上扩展去实现消息的撤回
     *
     * @param message
     *            需要撤回的消息
     * @param callBack
     *            发送消息的回调，通知调用方发送撤回消息的结果
     */
    public static void sendRevokeMessage(final Context context, final EMMessage message, final EMCallBack callBack) {
        if (message.status != EMMessage.Status.SUCCESS) {
            callBack.onError(0, "sending");
            return;
        }
        // 获取当前时间，用来判断后边撤回消息的时间点是否合法，这个判断不需要在接收方做，
        // 因为如果接收方之前不在线，很久之后才收到消息，将导致撤回失败
        long currTime = System.currentTimeMillis();
        long msgTime = message.getMsgTime();
        if (currTime < msgTime || (currTime - msgTime) > 120000) {
            callBack.onError(1, "maxtime");
            return;
        }
        String msgId = message.getMsgId();
        EMMessage cmdMessage = EMMessage.createSendMessage(EMMessage.Type.CMD);
        if (message.getChatType() == EMMessage.ChatType.GroupChat) {
            cmdMessage.setChatType(EMMessage.ChatType.GroupChat);
        }
        cmdMessage.setReceipt(message.getTo());
        // 创建CMD 消息的消息体 并设置 action 为 revoke
        CmdMessageBody body = new CmdMessageBody(EaseConstant.EASE_ATTR_REVOKE);
        cmdMessage.addBody(body);
        cmdMessage.setAttribute(EaseConstant.EASE_ATTR_REVOKE_MSG_ID, msgId);
        // 确认无误，开始发送撤回消息的透传
        EMChatManager.getInstance().sendMessage(cmdMessage, new EMCallBack() {
            @Override
            public void onSuccess() {
                // 更改要撤销的消息的内容，替换为消息已经撤销的提示内容
                TextMessageBody body = new TextMessageBody(context.getString(R.string.revoke_message_by_self));
                message.addBody(body);
                // 这里需要把消息类型改为 TXT 类型
                message.setType(EMMessage.Type.TXT);
                // 设置扩展为撤回消息类型，是为了区分消息的显示
                message.setAttribute(EaseConstant.EASE_ATTR_REVOKE, true);
                // 返回修改消息结果
                EMChatManager.getInstance().updateMessageBody(message);
                callBack.onSuccess();
            }

            @Override
            public void onError(int i, String s) {
                callBack.onError(i, s);
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    /**
     * 收到撤回消息，这里需要和发送方协商定义，通过一个透传，并加上扩展去实现消息的撤回
     *
     * @param revokeMsg
     *            收到的透传消息，包含需要撤回的消息的 msgId
     * @return 返回撤回结果是否成功
     */
    public static boolean receiveRevokeMessage(Context context, EMMessage revokeMsg) {
        String username = revokeMsg.getChatType() == ChatType.Chat ? revokeMsg.getFrom() : revokeMsg.getTo();
        EMConversation conversation = EMChatManager.getInstance().getConversation(username);
        boolean result = false;
        // 从cmd扩展中获取要撤回消息的id
        String msgId = revokeMsg.getStringAttribute(EaseConstant.EASE_ATTR_REVOKE_MSG_ID, null);
        if (msgId == null) {
            return result;
        }
        // 根据得到的msgId 去本地查找这条消息，如果本地已经没有这条消息了，就不用撤回
        // 这里为了防止消息没有加载到内存中，使用Conversation的loadMessage方法加载消息
        EMMessage message = EMChatManager.getInstance().getMessage(msgId);
        if (message == null) {
            message = conversation.loadMessage(msgId);
        }
        // 更改要撤销的消息的内容，替换为消息已经撤销的提示内容
        TextMessageBody body = new TextMessageBody(
                String.format(context.getString(R.string.revoke_message_by_user), message.getFrom()));
        message.addBody(body);
        // 这里需要把消息类型改为 TXT 类型
        message.setType(EMMessage.Type.TXT);
        // 设置扩展为撤回消息类型，是为了区分消息的显示
        message.setAttribute(EaseConstant.EASE_ATTR_REVOKE, true);
        // 返回修改消息结果
        result = EMChatManager.getInstance().updateMessageBody(message);
        // 因为Android这边没有修改消息未读数的方法，这里只能通过conversation的getMessage方法来实现未读数减一
        conversation.getMessage(msgId);
        message.isAcked = true;
        removeAtToConversationExt(conversation, msgId);
        return result;
    }

    /**
     * 根据得到的消息设置会话的扩展 定义添加到Conversation对象的扩展内容 { "ease_group_at_members": { //
     * 这里表示@ 类型的扩展 "ease_msg_id": "132423423425", "ease_msg_time": 14567823801 }
     * "ease_top": 1 // TODO 这里表示会话置顶扩展 }
     * 
     * @param message
     *            接收到的消息
     */
    public static void saveAtToConversationExt(EMMessage message) {
        try {
            // 解析消息扩展，判断当前消息是否有@群成员的扩展，如果没有会直接进入catch
            JSONArray jsonArray = message.getJSONArrayAttribute(EaseConstant.EASE_ATTR_GROUP_AT_MEMBERS);
            // 获取当前登录账户的 username
            String currUser = EMChatManager.getInstance().getCurrentUser();
            for (int i = 0; i < jsonArray.length(); i++) {
                // 循环遍历数组，判断是否有@当前账户的 username，如果有则进一步处理
                if (jsonArray.getString(i).equals(currUser)) {
                    // 获取当前群组会话，因为是群组，所以要根据getTo() 获取群组id
                    EMConversation conversation = EMChatManager.getInstance().getConversation(message.getTo(), true);
                    // 获取会话的扩展
                    String extField = conversation.getExtField();
                    JSONObject extObject = null;
                    if(TextUtils.isEmpty(extField)){
                        extObject = new JSONObject();
                    }else{
                        extObject = new JSONObject(extField);
                    }
                    
                    // 获取保存带有@扩展的的消息id
                    JSONArray atArray = extObject.optJSONArray(EaseConstant.EASE_KEY_HAVE_AT);
                    if (atArray == null) {
                        atArray = new JSONArray();
                    }
                    // 将包含有@ 扩展的消息添加到Conversation的扩展中去
                    atArray.put(message.getMsgId());
                    // 将内层@类型的json数据设置给外层obj json对象
                    extObject.put(EaseConstant.EASE_KEY_HAVE_AT, atArray);
                    // 将json对象转为String保存在conversation的ext扩展中
                    conversation.setExtField(extObject.toString());
                }
            }
        } catch (EaseMobException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("melove", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除包含@扩展的消息id，当撤回一条包含@扩展的消息时需要把@的状态改变下
     * 
     * @param msgId
     *            要撤回的消息id
     */
    public static void removeAtToConversationExt(EMConversation conversation, String msgId) {
        try {
            // 获取会话的扩展
            String extField = conversation.getExtField();
            if (!TextUtils.isEmpty(extField)) {
                JSONObject extObject = new JSONObject(extField);
                // 获取保存带有@扩展的的消息id
                JSONArray atArray = extObject.optJSONArray(EaseConstant.EASE_KEY_HAVE_AT);
                if(atArray != null && atArray.length() > 0){
                    List<String> atList = new ArrayList<String>(); 
                    for(int i=0; i<atArray.length(); i++){
                        atList.add(atArray.getString(i));
                    }
                    atList.remove(msgId);
                    JSONArray atArray2 = new JSONArray();
                    for(int i=0; i<atList.size(); i++){
                        atArray2.put(atList.get(i));
                    }
                    extObject.put(EaseConstant.EASE_KEY_HAVE_AT, atArray2);
                    conversation.setExtField(extObject.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送输入状态，这里通过cmd消息来进行发送，告知对方自己正在输入
     * 
     * @param to
     *            接收方的名字
     */
    public static void sendInputStatus(String to) {
        EMMessage cmdMessage = EMMessage.createSendMessage(EMMessage.Type.CMD);
        cmdMessage.setReceipt(to);
        // 创建CMD 消息的消息体 并设置 action 为 revoke
        CmdMessageBody body = new CmdMessageBody(EaseConstant.EASE_ATTR_INPUT_STATUS);
        cmdMessage.addBody(body);
        // 确认无误，开始发送撤回消息的透传
        try {
            EMChatManager.getInstance().sendMessage(cmdMessage);
        } catch (EaseMobException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
