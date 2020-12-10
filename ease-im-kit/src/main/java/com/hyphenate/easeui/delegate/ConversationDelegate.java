package com.hyphenate.easeui.delegate;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseDelegate;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.manager.EaseAtMessageHelper;
import com.hyphenate.easeui.manager.EasePreferenceManager;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseDateUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.widget.EaseImageView;

import java.util.Date;

/**
 * 会话条目代理
 */
public class ConversationDelegate extends EaseBaseDelegate<EMConversation, ConversationDelegate.ViewHolder> {
    @Override
    public boolean isForViewType(EMConversation item, int position) {
        return item != null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ease_item_row_chat_history;
    }

    @Override
    protected ConversationDelegate.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    public class ViewHolder extends EaseBaseRecyclerViewAdapter.ViewHolder<EMConversation> {
        private ConstraintLayout listIteaseLayout;
        private EaseImageView avatar;
        private TextView mUnreadMsgNumber;
        private TextView name;
        private TextView time;
        private ImageView mMsgState;
        private TextView mentioned;
        private TextView message;
        private Context mContext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mContext = itemView.getContext();
            listIteaseLayout = findViewById(R.id.list_itease_layout);
            avatar = findViewById(R.id.avatar);
            mUnreadMsgNumber = findViewById(R.id.unread_msg_number);
            name = findViewById(R.id.name);
            time = findViewById(R.id.time);
            mMsgState = findViewById(R.id.msg_state);
            mentioned = findViewById(R.id.mentioned);
            message = findViewById(R.id.message);
            EaseAvatarOptions avatarOptions = EaseIM.getInstance().getAvatarOptions();
            if(avatarOptions != null) {
                avatar.setShapeType(avatarOptions.getAvatarShape());
            }

        }

        @Override
        public void setData(EMConversation object, int position) {
            EMConversation item = (EMConversation) object;
            String username = item.conversationId();
            listIteaseLayout.setBackground(!TextUtils.isEmpty(item.getExtField())
                    ? ContextCompat.getDrawable(mContext, R.drawable.ease_conversation_top_bg)
                    : null);
            mentioned.setVisibility(View.GONE);
            if(item.getType() == EMConversation.EMConversationType.GroupChat) {
                if(EaseAtMessageHelper.get().hasAtMeMsg(username)) {
                    mentioned.setText(R.string.were_mentioned);
                    mentioned.setVisibility(View.VISIBLE);
                }
                avatar.setImageResource(R.drawable.ease_group_icon);
                EMGroup group = EMClient.getInstance().groupManager().getGroup(username);
                name.setText(group != null ? group.getGroupName() : username);
            }else if(item.getType() == EMConversation.EMConversationType.ChatRoom) {
                avatar.setImageResource(R.drawable.ease_chat_room_icon);
                EMChatRoom chatRoom = EMClient.getInstance().chatroomManager().getChatRoom(username);
                name.setText(chatRoom != null && !TextUtils.isEmpty(chatRoom.getName()) ? chatRoom.getName() : username);
            }else {
                avatar.setImageResource(R.drawable.ease_default_avatar);
                name.setText(username);
            }

            if(item.getUnreadMsgCount() > 0) {
                mUnreadMsgNumber.setText(String.valueOf(item.getUnreadMsgCount()));
                mUnreadMsgNumber.setVisibility(View.VISIBLE);
            }else {
                mUnreadMsgNumber.setVisibility(View.GONE);
            }

            if(item.getAllMsgCount() != 0) {
                EMMessage lastMessage = item.getLastMessage();
                message.setText(EaseSmileUtils.getSmiledText(mContext, EaseCommonUtils.getMessageDigest(lastMessage, mContext)));
                time.setText(EaseDateUtils.getTimestampString(mContext, new Date(lastMessage.getMsgTime())));
                if (lastMessage.direct() == EMMessage.Direct.SEND && lastMessage.status() == EMMessage.Status.FAIL) {
                    mMsgState.setVisibility(View.VISIBLE);
                } else {
                    mMsgState.setVisibility(View.GONE);
                }
            }

            if(mentioned.getVisibility() != View.VISIBLE) {
                String unSendMsg = EasePreferenceManager.getInstance().getUnSendMsgInfo(username);
                if(!TextUtils.isEmpty(unSendMsg)) {
                    mentioned.setText(R.string.were_not_send_msg);
                    message.setText(unSendMsg);
                    mentioned.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
