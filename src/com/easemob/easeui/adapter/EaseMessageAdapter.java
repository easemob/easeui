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
package com.easemob.easeui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.easemob.easeui.widget.chatrow.EaseChatRowBigExpression;
import com.easemob.easeui.widget.chatrow.EaseChatRowFile;
import com.easemob.easeui.widget.chatrow.EaseChatRowImage;
import com.easemob.easeui.widget.chatrow.EaseChatRowLocation;
import com.easemob.easeui.widget.chatrow.EaseChatRowRecall;
import com.easemob.easeui.widget.chatrow.EaseChatRowText;
import com.easemob.easeui.widget.chatrow.EaseChatRowVideo;
import com.easemob.easeui.widget.chatrow.EaseChatRowVoice;
import com.easemob.easeui.widget.chatrow.EaseCustomChatRowProvider;

public class EaseMessageAdapter extends BaseAdapter{

	private final static String TAG = "msg";

	private Context context;
	
	private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
	private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
    private static final int HANDLER_MESSAGE_SEEK_TO = 2;

	private static final int MESSAGE_TYPE_RECV_TXT = 0;
	private static final int MESSAGE_TYPE_SENT_TXT = 1;
	private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
	private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
	private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
	private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
	private static final int MESSAGE_TYPE_SENT_VOICE = 6;
	private static final int MESSAGE_TYPE_RECV_VOICE = 7;
	private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
	private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
	private static final int MESSAGE_TYPE_SENT_FILE = 10;
	private static final int MESSAGE_TYPE_RECV_FILE = 11;
	private static final int MESSAGE_TYPE_SENT_EXPRESSION = 12;
	private static final int MESSAGE_TYPE_RECV_EXPRESSION = 13;

    private static final int MESSAGE_TYPE_RECALL = 14;
	
	
	public int itemTypeCount; 
	
	// reference to conversation object in chatsdk
	private EMConversation conversation;
	EMMessage[] messages = null;
	
    private String toChatUsername;

    private MessageListItemClickListener itemClickListener;
    private EaseCustomChatRowProvider customRowProvider;
    
    private boolean showUserNick;
    private boolean showAvatar;
    private Drawable myBubbleBg;
    private Drawable otherBuddleBg;

    private ListView listView;

	public EaseMessageAdapter(Context context, String username, int chatType, ListView listView) {
		this.context = context;
		this.listView = listView;
		toChatUsername = username;
		this.conversation = EMChatManager.getInstance().getConversation(username);
	}
	
	Handler handler = new Handler() {
		private void refreshList() {
			// UI线程不能直接使用conversation.getAllMessages()
			// 否则在UI刷新过程中，如果收到新的消息，会导致并发问题
			messages = (EMMessage[]) conversation.getAllMessages().toArray(new EMMessage[conversation.getAllMessages().size()]);
			for (int i = 0; i < messages.length; i++) {
				// getMessage will set message as read status
				conversation.getMessage(i);
			}
			notifyDataSetChanged();
		}
		
		@Override
		public void handleMessage(android.os.Message message) {
			switch (message.what) {
			case HANDLER_MESSAGE_REFRESH_LIST:
				refreshList();
				break;
			case HANDLER_MESSAGE_SELECT_LAST:
                if (messages.length > 0) {
                    listView.setSelection(messages.length - 1);
                }
                break;
            case HANDLER_MESSAGE_SEEK_TO:
                int position = message.arg1;
                listView.setSelection(position);
                break;
			default:
				break;
			}
		}
	};


	/**
	 * 刷新页面
	 */
	public void refresh() {
		if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
			return;
		}
		android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
		handler.sendMessage(msg);
	}
	
	/**
     * 刷新页面, 选择最后一个
     */
    public void refreshSelectLast() {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_SELECT_LAST));
    }
    
    /**
     * 刷新页面, 选择Position
     */
    public void refreshSeekTo(int position) {
        handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
        android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
        msg.arg1 = position;
        handler.sendMessage(msg);
    }
	

	public EMMessage getItem(int position) {
		if (messages != null && position < messages.length) {
			return messages[position];
		}
		return null;
	}

	public long getItemId(int position) {
		return position;
	}
	
	/**
     * 获取item数
     */
    public int getCount() {
        return messages == null ? 0 : messages.length;
    }
	
	/**
	 * 获取item类型数
	 */
	public int getViewTypeCount() {
	    if(customRowProvider != null && customRowProvider.getCustomChatRowTypeCount() > 0){
	        return customRowProvider.getCustomChatRowTypeCount() + 15;
	    }
        return 15;
    }
	

	/**
	 * 获取item类型
	 */
	public int getItemViewType(int position) {
		EMMessage message = getItem(position); 
		if (message == null) {
			return -1;
		}
		
		if(customRowProvider != null && customRowProvider.getCustomChatRowType(message) > 0){
		    return customRowProvider.getCustomChatRowType(message) + 13;
		}
		
		if (message.getType() == EMMessage.Type.TXT) {
		    if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
		        return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_EXPRESSION : MESSAGE_TYPE_SENT_EXPRESSION;
		    }else if(message.getStringAttribute(EaseConstant.EASE_ATTR_TYPE, "null").equals(EaseConstant.EASE_ATTR_TYPE_RECALL)){
            	return MESSAGE_TYPE_RECALL;
            }
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
		}
		if (message.getType() == EMMessage.Type.IMAGE) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;

		}
		if (message.getType() == EMMessage.Type.LOCATION) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
		}
		if (message.getType() == EMMessage.Type.VOICE) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
		}
		if (message.getType() == EMMessage.Type.VIDEO) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
		}
		if (message.getType() == EMMessage.Type.FILE) {
			return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
		}

		return -1;// invalid
	}
	
	protected EaseChatRow createChatRow(Context context, EMMessage message, int position) {
        EaseChatRow chatRow = null;
        if(customRowProvider != null && customRowProvider.getCustomChatRow(message, position, this) != null){
            return customRowProvider.getCustomChatRow(message, position, this);
        }
        switch (message.getType()) {
        case TXT:
            if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
                chatRow = new EaseChatRowBigExpression(context, message, position, this);
            }else if(message.getStringAttribute(EaseConstant.EASE_ATTR_TYPE, "null").equals(EaseConstant.EASE_ATTR_TYPE_RECALL)){
            	return new EaseChatRowRecall(context, message, position, this);
            }else{
                chatRow = new EaseChatRowText(context, message, position, this);
            }
            break;
        case LOCATION:
            chatRow = new EaseChatRowLocation(context, message, position, this);
            break;
        case FILE:
            chatRow = new EaseChatRowFile(context, message, position, this);
            break;
        case IMAGE:
            chatRow = new EaseChatRowImage(context, message, position, this);
            break;
        case VOICE:
            chatRow = new EaseChatRowVoice(context, message, position, this);
            break;
        case VIDEO:
            chatRow = new EaseChatRowVideo(context, message, position, this);
            break;
        default:
            break;
        }

        return chatRow;
    }
    

	@SuppressLint("NewApi")
	public View getView(final int position, View convertView, ViewGroup parent) {
		EMMessage message = getItem(position);
		if(convertView == null){
			convertView = createChatRow(context, message, position);
		}
		
		if(message.getStringAttribute(EaseConstant.EASE_ATTR_TYPE, "null").equals(EaseConstant.EASE_ATTR_TYPE_RECALL)){
			((EaseChatRowRecall)convertView).setUpView(message, position, itemClickListener);
        }else{
    		//缓存的view的message很可能不是当前item的，传入当前message和position更新ui
    		((EaseChatRow)convertView).setUpView(message, position, itemClickListener);
        }
		return convertView;
	}


	public String getToChatUsername(){
	    return toChatUsername;
	}
	
	
	
	public void setShowUserNick(boolean showUserNick) {
        this.showUserNick = showUserNick;
    }


    public void setShowAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
    }


    public void setMyBubbleBg(Drawable myBubbleBg) {
        this.myBubbleBg = myBubbleBg;
    }


    public void setOtherBuddleBg(Drawable otherBuddleBg) {
        this.otherBuddleBg = otherBuddleBg;
    }


    public void setItemClickListener(MessageListItemClickListener listener){
	    itemClickListener = listener;
	}
	
	public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider){
	    customRowProvider = rowProvider;
	}


    public boolean isShowUserNick() {
        return showUserNick;
    }


    public boolean isShowAvatar() {
        return showAvatar;
    }


    public Drawable getMyBubbleBg() {
        return myBubbleBg;
    }


    public Drawable getOtherBuddleBg() {
        return otherBuddleBg;
    }
	
	
	
}
