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
package com.easemob.easeui;

public class EaseConstant {
    public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";
    public static final String MESSAGE_ATTR_IS_DYNAMIC_EXPRESSION = "em_is_expression";
    /**
     * app自带的动态表情，直接去resource里获取图片
     */
    public static final String MESSAGE_ATTR_DYNAMIC_EXPRESSION_ICON = "em_expression_icon";
    /**
     * 动态下载的表情图片，需要知道表情url
     */
    public static final String MESSAGE_ATTR_DYNAMIC_EXPRESSION_URL = "em_is_expression_url";
    
    public static final String MESSAGE_ATTR_DYNAMIC_EXPRESSION_PATH = "em_is_expression_path";
    
	public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final int CHATTYPE_CHATROOM = 3;
    
    public static final String EXTRA_CHAT_TYPE = "chatType";
    public static final String EXTRA_USER_ID = "userId";
}
