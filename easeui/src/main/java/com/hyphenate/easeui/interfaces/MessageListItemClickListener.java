package com.hyphenate.easeui.interfaces;

import android.view.View;

import com.hyphenate.chat.EMMessage;

/**
 * 消息列表中的点击事件
 */
public interface MessageListItemClickListener{
	/**
	 * there is default handling when bubble is clicked, if you want handle it, return true
	 * another way is you implement in onBubbleClick() of chat row
	 * @param message
	 * @return
	 */
	boolean onBubbleClick(EMMessage message);

	/**
	 * click resend view
	 * @param message
	 * @return
	 */
	boolean onResendClick(EMMessage message);

	/**
	 * on long click for bubble
	 * @param v
	 * @param message
	 */
	void onBubbleLongClick(View v, EMMessage message);

	/**
	 * click the user avatar
	 * @param username
	 */
	void onUserAvatarClick(String username);

	/**
	 * long click for user avatar
	 * @param username
	 */
	void onUserAvatarLongClick(String username);

	/**
	 * message in sending progress
	 * @param message
	 */
	void onMessageInProgress(EMMessage message);
}