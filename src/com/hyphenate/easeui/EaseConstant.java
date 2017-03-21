/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui;

public class EaseConstant {
    public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";

    public static final String MESSAGE_ATTR_IS_BIG_EXPRESSION = "em_is_big_expression";
    public static final String MESSAGE_ATTR_EXPRESSION_ID = "em_expression_id";

    public static final String MESSAGE_ATTR_AT_MSG = "em_at_list";
    public static final String MESSAGE_ATTR_VALUE_AT_MSG_ALL = "ALL";

    // 置顶
    public static final String CONVERSATION_TOP = "conversation_top";
    // 草稿
    public static final String CONVERSATION_DRAFT = "conversation_draft";
    // 群组消息已读 Action
    public static final String GROUP_READ_ACTION = "group_read_action";
    public static final String GROUP_READ_MSG_ID_ARRAY = "group_read_msg_id_array";
    public static final String GROUP_READ_CONVERSATION_ID = "group_read_conversation_id";
    public static final String GROUP_READ_MEMBER_ARRAY = "group_read_member_array";

    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final int CHATTYPE_CHATROOM = 3;

    public static final String EXTRA_CHAT_TYPE = "chatType";
    public static final String EXTRA_USER_ID = "userId";

    // 撤回
    public static final String REVOKE_FLAG = "revoke_flag";
    // 消息id
    public static final String MSG_ID = "msg_id";
    // 输入状态
    public static final String INPUT_TYPE = "inputType";

    // 公告会话 id，这里默认是 admin
    public static final String AFFICHE_CONVERSATION_ID = "admin";

    /**
     * 自定义一些错误码，表示一些固定的错误
     */
    // 撤回消息错误码，超过时间限制
    public static final int ERROR_I_RECALL_TIME = 5001;
    // 撤回消息错误文字描述
    public static final String ERROR_S_RECALL_TIME = "max_time";
    // 消息允许撤回时间 3 分钟
    public static final int TIME_RECALL = 300000;
    // 输入状态检测时间
    public static final int TIME_INPUT_STATUS = 4000;
}
