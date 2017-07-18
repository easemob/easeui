package com.easemob.easeui.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatRoom;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMConversation.EMConversationType;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.R;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.easeui.utils.EaseSmileUtils;
import com.easemob.easeui.utils.EaseUserUtils;
import com.easemob.easeui.widget.EaseConversationList.EaseConversationListHelper;
import com.easemob.easeui.widget.EaseImageView;
import com.easemob.util.DateUtils;

/**
 * 会话列表adapter
 *
 */
public class EaseConversationAdapater extends ArrayAdapter<EMConversation> {
    
    private Context mContext;
    
	private static final String TAG = "ChatAllHistoryAdapter";
	private List<EMConversation> conversationList;
	private List<EMConversation> copyConversationList;
	private ConversationFilter conversationFilter;
	private boolean notiyfyByFilter;

	protected int primaryColor;
	protected int secondaryColor;
	protected int timeColor;
	protected int primarySize;
	protected int secondarySize;
	protected float timeSize;

	// 自定义ImageView 的属性值 默认是不设置
	private int avatarShape = -1;
	private int borderWidth = -1;
	private int borderColor = -1;
	private int avatarRadius = -1;

	public EaseConversationAdapater(Context context, int resource, List<EMConversation> objects) {
		super(context, resource, objects);
		mContext = context;
		conversationList = objects;
		copyConversationList = new ArrayList<EMConversation>();
		copyConversationList.addAll(objects);
	}
	
	public void setAvatarShape(int shape) {
		this.avatarShape = shape;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}

	public void setAvatarRadius(int avatarRadius) {
		this.avatarRadius = avatarRadius;
	}


	@Override
	public EMConversation getItem(int arg0) {
		if (arg0 < conversationList.size()) {
			return conversationList.get(arg0);
		}
		return null;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.ease_row_chat_history, parent, false);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.unreadLabel = (TextView) convertView.findViewById(R.id.unread_msg_number);
			holder.message = (TextView) convertView.findViewById(R.id.message);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.avatar = (EaseImageView) convertView.findViewById(R.id.avatar);
			holder.msgState = convertView.findViewById(R.id.msg_state);
			holder.list_itease_layout = (RelativeLayout) convertView.findViewById(R.id.list_itease_layout);
			convertView.setTag(holder);
		}
		holder.list_itease_layout.setBackgroundResource(R.drawable.ease_mm_listitem);

		// 获取与此用户/群组的会话
		EMConversation conversation = getItem(position);
		// 获取用户username或者群组groupid
		String username = conversation.getUserName();

		if (conversation.getType() == EMConversationType.GroupChat) {
			// 群聊消息，显示群聊头像
			holder.avatar.setImageResource(R.drawable.ease_group_icon);
			EMGroup group = EMGroupManager.getInstance().getGroup(username);
			holder.name.setText(group != null ? group.getGroupName() : username);
		} else if (conversation.getType() == EMConversationType.ChatRoom) {
			holder.avatar.setImageResource(R.drawable.ease_group_icon);
			EMChatRoom room = EMChatManager.getInstance().getChatRoom(username);
			holder.name.setText(room != null && !TextUtils.isEmpty(room.getName()) ? room.getName() : username);
		} else {
			EaseUserUtils.setUserAvatar(getContext(), username, holder.avatar);
			EaseUserUtils.setUserNick(username, holder.name);
		}

		if (conversation.getUnreadMsgCount() > 0) {
			// 显示与此用户的消息未读数
			holder.unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
			holder.unreadLabel.setVisibility(View.VISIBLE);
		} else {
			holder.unreadLabel.setVisibility(View.INVISIBLE);
		}

		if (conversation.getMsgCount() != 0) {
			// 把最后一条消息的内容作为item的message内容
			EMMessage lastMessage = conversation.getLastMessage();
			String content = null;
			if(cvsListHelper != null){
				content = cvsListHelper.onSetItemSecondaryText(lastMessage);
			}
			// 这里判断下当前消息是否为阅后即焚类型，如果是并且同时是最后一条消息，在会话列表就要替换下消息显示内容
			if(lastMessage.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
					&& lastMessage.direct == Direct.RECEIVE){
				holder.message.setText(R.string.readfire_message);
			}else{
				holder.message.setText(EaseSmileUtils.getSmiledText(getContext(),
						EaseCommonUtils.getMessageDigest(lastMessage, (this.getContext()))), BufferType.SPANNABLE);
			}
			if(content != null){
				holder.message.setText(content);
			}
			holder.time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
			if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
				holder.msgState.setVisibility(View.VISIBLE);
			} else {
				holder.msgState.setVisibility(View.GONE);
			}
		}
		/**
		 * 设置自定义 ImageView 的属性
		 */
		if (avatarShape != -1) {
			holder.avatar.setShapeType(avatarShape);
		}
		if (borderColor != -1) {
			holder.avatar.setBorderColor(borderColor);
		}
		if (borderWidth != -1) {
			holder.avatar.setBorderWidth(borderWidth);
		}
		if (avatarRadius != -1) {
			holder.avatar.setRadius(avatarRadius);
		}
		// 设置自定义属性
		holder.name.setTextColor(primaryColor);
		holder.message.setTextColor(secondaryColor);
		holder.time.setTextColor(timeColor);
		if (primarySize != 0)
			holder.name.setTextSize(TypedValue.COMPLEX_UNIT_PX, primarySize);
		if (secondarySize != 0)
			holder.message.setTextSize(TypedValue.COMPLEX_UNIT_PX, secondarySize);
		if (timeSize != 0)
			holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);

		return convertView;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (!notiyfyByFilter) {
			copyConversationList.clear();
			copyConversationList.addAll(conversationList);
			notiyfyByFilter = false;
		}
	}

	@Override
	public Filter getFilter() {
		if (conversationFilter == null) {
			conversationFilter = new ConversationFilter(conversationList);
		}
		return conversationFilter;
	}

	public void setPrimaryColor(int primaryColor) {
		this.primaryColor = primaryColor;
	}

	public void setSecondaryColor(int secondaryColor) {
		this.secondaryColor = secondaryColor;
	}

	public void setTimeColor(int timeColor) {
		this.timeColor = timeColor;
	}

	public void setPrimarySize(int primarySize) {
		this.primarySize = primarySize;
	}

	public void setSecondarySize(int secondarySize) {
		this.secondarySize = secondarySize;
	}

	public void setTimeSize(float timeSize) {
		this.timeSize = timeSize;
	}

	private class ConversationFilter extends Filter {
		List<EMConversation> mOriginalValues = null;

		public ConversationFilter(List<EMConversation> mList) {
			mOriginalValues = mList;
		}

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				mOriginalValues = new ArrayList<EMConversation>();
			}
			if (prefix == null || prefix.length() == 0) {
				results.values = copyConversationList;
				results.count = copyConversationList.size();
			} else {
				
				if (copyConversationList.size() > mOriginalValues.size()) {
                    			mOriginalValues = copyConversationList;
                		}
				
				String prefixString = prefix.toString();
				final int count = mOriginalValues.size();
				final ArrayList<EMConversation> newValues = new ArrayList<EMConversation>();

				for (int i = 0; i < count; i++) {
					final EMConversation value = mOriginalValues.get(i);
					String username = value.getUserName();

					EMGroup group = EMGroupManager.getInstance().getGroup(username);
					if (group != null) {
						username = group.getGroupName();
					} else {
						EaseUser user = EaseUserUtils.getUserInfo(username);
						if (user != null && user.getNick() != null)
							username = user.getNick();
					}

					// First match against the whole ,non-splitted value
					if (username.startsWith(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = username.split(" ");
						final int wordCount = words.length;

						// Start at index 0, in case valueText starts with
						// space(s)
						for (int k = 0; k < wordCount; k++) {
							if (words[k].startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			conversationList.clear();
			conversationList.addAll((List<EMConversation>) results.values);
			if (results.count > 0) {
				notiyfyByFilter = true;
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}

		}

	}
	
	private EaseConversationListHelper cvsListHelper;
	public void setCvsListHelper(EaseConversationListHelper cvsListHelper){
		this.cvsListHelper = cvsListHelper;
	}

	private static class ViewHolder {
		/** 和谁的聊天记录 */
		TextView name;
		/** 消息未读数 */
		TextView unreadLabel;
		/** 最后一条消息的内容 */
		TextView message;
		/** 最后一条消息的时间 */
		TextView time;
		/** 用户头像 */
		EaseImageView avatar;
		/** 最后一条消息的发送状态 */
		View msgState;
		/** 整个list中每一行总布局 */
		RelativeLayout list_itease_layout;

	}
	
}
