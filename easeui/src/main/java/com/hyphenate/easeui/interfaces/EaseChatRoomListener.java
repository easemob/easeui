package com.hyphenate.easeui.interfaces;

import com.hyphenate.EMChatRoomChangeListener;

import java.util.List;

/**
 * 不需要实现的方法不再重写
 */
public abstract class EaseChatRoomListener implements EMChatRoomChangeListener {
    @Override
    public abstract void onChatRoomDestroyed(final String roomId, final String roomName);

    @Override
    public abstract void onRemovedFromChatRoom(final int reason, final String roomId, final String roomName, final String participant);

    @Override
    public abstract void onMemberJoined(final String roomId, final String participant);

    @Override
    public abstract void onMemberExited(final String roomId, final String roomName, final String participant);

    @Override
    public void onMuteListAdded(final String chatRoomId, final List<String> mutes, final long expireTime) {

    }

    @Override
    public void onMuteListRemoved(final String chatRoomId, final List<String> mutes) {

    }

    @Override
    public void onAdminAdded(final String chatRoomId, final String admin) {

    }

    @Override
    public void onAdminRemoved(final String chatRoomId, final String admin) {

    }

    @Override
    public void onOwnerChanged(final String chatRoomId, final String newOwner, final String oldOwner) {

    }

    @Override
    public void onAnnouncementChanged(final String chatroomId, final String announcement) {

    }

    @Override
    public void onWhiteListAdded(String chatRoomId, List<String> whitelist) {

    }

    @Override
    public void onWhiteListRemoved(String chatRoomId, List<String> whitelist) {

    }

    @Override
    public void onAllMemberMuteStateChanged(String chatRoomId, boolean isMuted) {

    }
}
