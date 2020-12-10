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
	boolean onBubbleLongClick(View v, EMMessage message);

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
	 * message is create status
	 * @param message
	 */
	void onMessageCreate(EMMessage message);

	/**
	 * message send success
	 * @param message
	 */
	void onMessageSuccess(EMMessage message);

	/**
	 * message send fail
	 * @param message
	 * @param code
	 * @param error
	 */
	void onMessageError(EMMessage message, int code, String error);

	/**
	 * message in sending progress
	 * @param message
	 * @param progress
	 */
	void onMessageInProgress(EMMessage message, int progress);
}