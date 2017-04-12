package com.hyphenate.easeui.ui;

import com.hyphenate.EMGroupChangeListener;

import java.util.List;

/**
 * group change listener
 *
 */
public abstract class EaseGroupListener implements EMGroupChangeListener{

    @Override
    public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {

    }

    @Override
    public void onRequestToJoinReceived(String groupId, String groupName, String applyer, String reason) {

    }

    @Override
    public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {

    }

    @Override
    public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {

    }

    @Override
    public void onInvitationAccepted(String groupId, String inviter, String reason) {

    }

    @Override
    public void onInvitationDeclined(String groupId, String invitee, String reason) {

    }
    
    @Override
    public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {

    }

    @Override
    public void onMuteListAdded(String groupId, final List<String> mutes, final long muteExpire) {

    }

    @Override
    public void onMuteListRemoved(String groupId, final List<String> mutes) {

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
    public void onMemberJoined(final String groupId,  final String member){
        
    }
    @Override
    public void onMemberExited(final String groupId, final String member) {
        
    }
}
