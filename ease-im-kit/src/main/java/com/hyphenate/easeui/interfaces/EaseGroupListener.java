package com.hyphenate.easeui.interfaces;

import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMMucSharedFile;

import java.util.List;

public abstract class EaseGroupListener implements EMGroupChangeListener {

    @Override
    public abstract void onUserRemoved(String groupId, String groupName);

    @Override
    public abstract void onGroupDestroyed(String groupId, String groupName);

    @Override
    public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {

    }

    @Override
    public void onRequestToJoinReceived(String groupId, String groupName, String applicant, String reason) {

    }

    @Override
    public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {

    }

    @Override
    public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {

    }

    @Override
    public void onInvitationAccepted(String groupId, String invitee, String reason) {

    }

    @Override
    public void onInvitationDeclined(String groupId, String invitee, String reason) {

    }

    @Override
    public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {

    }

    @Override
    public void onMuteListAdded(String groupId, List<String> mutes, long muteExpire) {

    }

    @Override
    public void onMuteListRemoved(String groupId, List<String> mutes) {

    }

    @Override
    public void onAdminAdded(String groupId, String administrator) {

    }

    @Override
    public void onAdminRemoved(String groupId, String administrator) {

    }

    @Override
    public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {

    }

    @Override
    public void onMemberJoined(String groupId, String member) {

    }

    @Override
    public void onMemberExited(String groupId, String member) {

    }

    @Override
    public void onAnnouncementChanged(String groupId, String announcement) {

    }

    @Override
    public void onSharedFileAdded(String groupId, EMMucSharedFile sharedFile) {

    }

    @Override
    public void onSharedFileDeleted(String groupId, String fileId) {

    }

    @Override
    public void onWhiteListAdded(String groupId, List<String> whitelist) {

    }

    @Override
    public void onWhiteListRemoved(String groupId, List<String> whitelist) {

    }

    @Override
    public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {

    }
}
