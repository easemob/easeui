/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
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
package com.hyphenate.easeui.constants;

public interface EaseConstant {
    String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";


    String MESSAGE_ATTR_IS_BIG_EXPRESSION = "em_is_big_expression";
    String MESSAGE_ATTR_EXPRESSION_ID = "em_expression_id";

    String MESSAGE_ATTR_AT_MSG = "em_at_list";
    String MESSAGE_ATTR_VALUE_AT_MSG_ALL = "ALL";

    String FORWARD_MSG_ID = "forward_msg_id";
    String HISTORY_MSG_ID = "history_msg_id";

    int CHATTYPE_SINGLE = 1;
    int CHATTYPE_GROUP = 2;
    int CHATTYPE_CHATROOM = 3;

    String EXTRA_CHAT_TYPE = "chatType";
    String EXTRA_CONVERSATION_ID = "conversationId";
    String EXTRA_IS_ROAM = "isRoaming";

    String MESSAGE_TYPE_TXT = "txt";
    String MESSAGE_TYPE_EXPRESSION = "expression";
    String MESSAGE_TYPE_IMAGE = "image";
    String MESSAGE_TYPE_VIDEO = "video";
    String MESSAGE_TYPE_LOCATION = "location";
    String MESSAGE_TYPE_VOICE = "voice";
    String MESSAGE_TYPE_FILE = "file";
    String MESSAGE_TYPE_CMD = "cmd";
    String MESSAGE_TYPE_RECALL = "message_recall";
    String MESSAGE_TYPE_VOICE_CALL = "voice_call";
    String MESSAGE_TYPE_VIDEO_CALL = "video_call";
    String MESSAGE_TYPE_CONFERENCE_INVITE = "conference_invite";
    String MESSAGE_TYPE_LIVE_INVITE = "live_invite";

    String MESSAGE_FORWARD = "message_forward";

    String MESSAGE_CHANGE_RECEIVE = "message_receive";
    String MESSAGE_CHANGE_CMD_RECEIVE = "message_cmd_receive";
    String MESSAGE_CHANGE_SEND_SUCCESS = "message_success";
    String MESSAGE_CHANGE_SEND_ERROR = "message_error";
    String MESSAGE_CHANGE_SEND_PROGRESS = "message_progress";
    String MESSAGE_CHANGE_RECALL = "message_recall";
    String MESSAGE_CHANGE_CHANGE = "message_change";
    String MESSAGE_CHANGE_DELETE = "message_delete";
    String MESSAGE_CALL_SAVE = "message_call_save";
    String CONVERSATION_DELETE = "conversation_delete";
    String CONVERSATION_READ = "conversation_read";

    String GROUP_LEAVE = "group_leave";

    String DEFAULT_SYSTEM_MESSAGE_ID = "em_system";
    String DEFAULT_SYSTEM_MESSAGE_TYPE = "em_system_type";

    String USER_CARD_EVENT = "userCard";
}
