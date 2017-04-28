package com.hyphenate.easeui.ui;

import com.hyphenate.EMGroupChangeListener;

import java.util.List;

/**
 * group change listener
 *
 */
public abstract class EaseGroupRemoveListener implements EMGroupChangeListener{

    /**
     * \~chinese
     * 当前用户收到加入群组邀请
     * @param groupId 	要加入的群的id
     * @param groupName 要加入的群的名称
     * @param inviter 	邀请人的id
     * @param reason 	邀请加入的reason
     *
     * \~english
     * when receiving a group invitation
     *
     * @param groupId		group id
     * @param groupName		group's subject
     * @param inviter		Who invite you join the group
     * @param reason		Literal message coming with the invitation
     */
    public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {}

    /**
     * \~chinese
     * 用户申请加入群
     * @param groupId 	要加入的群的id
     * @param groupName 要加入的群的名称
     * @param applicant 申请人的username
     * @param reason 	申请加入的reason
     *
     * \~english
     * when the group owner receives a group request from user
     *
     * @param groupId		group id
     * @param groupName		group's name
     * @param applicant		The applicant want to join the group
     * @param reason		Literal message coming with the application
     */
    public void onRequestToJoinReceived(String groupId, String groupName, String applicant, String reason) {}


    /**
     * \~chinese
     * 加群申请被对方接受
     * @param groupId	群组的id
     * @param groupName 群组的名字
     * @param accepter 	同意人得username
     *
     * \~english
     * when the group invitation is accepted
     *
     * @param groupId 		group id
     * @param groupName 	group's name
     * @param accepter 		who approve the application
     */
    public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {}

    /**
     * \~chinese
     * 加群申请被拒绝
     * @param groupId 	群组id
     * @param groupName 群组名字
     * @param decliner 	拒绝人得username
     * @param reason 	拒绝理由
     *
     * \~english
     * when the group invitation is declined
     *
     * @param groupId 		group id
     * @param groupName 	group name
     * @param decliner 		decliner's username
     * @param reason 		reason of declining
     */
    public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {}

    /**
     * \~chinese
     * 群组邀请被接受
     * @param groupId
     * @param invitee
     * @param reason
     *
     * \~english
     * when the group invitation is accepted
     *
     * @param groupId 		group id
     * @param invitee
     * @param reason
     */
    public void onInvitationAccepted(String groupId, String invitee, String reason) {}

    /**
     * \~chinese
     * 群组邀请被拒绝
     * @param groupId
     * @param invitee
     * @param reason
     *
     * \~english
     * when the group invitation is declined
     *
     * @param groupId 		group id
     * @param invitee
     * @param reason 		reason of declining
     */
    public void onInvitationDeclined(String groupId, String invitee, String reason) {}

    /**
     * \~chinese
     * 当前登录用户被管理员移除出群组
     * @param groupId
     * @param groupName
     *
     * \~english
     * current user has been removed from the group
     *
     * @param groupId
     * @param groupName
     */
    public void onUserRemoved(String groupId, String groupName) {}

    /**
     * \~chinese
     * 群组被解散。
     * sdk 会先删除本地的这个群组，之后通过此回调通知应用，此群组被删除了
     * @param groupId 	群组的ID
     * @param groupName 群组的名称
     *
     * \~english
     * group dissolution
     * SDK will delete the group from local DB and local cache, then notify user that the group is destroyed
     *
     * @param groupId		group id
     * @param groupName 	group name
     */
    public void onGroupDestroyed(String groupId, String groupName) {}

    /**
     * \~chinese
     * 自动同意加入群组
     * sdk会先加入这个群组，并通过此回调通知应用
     * 参考{@link com.hyphenate.chat.EMOptions#setAutoAcceptGroupInvitation(boolean value)}
     *
     * @param groupId
     * @param inviter
     * @param inviteMessage
     *
     * \~english
     * When received group join invitation, will auto accept it and join the group
     * Please refer to {@link com.hyphenate.chat.EMOptions#setAutoAcceptGroupInvitation(boolean value)}
     *
     * @param groupId			group id
     * @param inviter
     * @param inviteMessage
     */
    public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {}

    /**
     * \~chinese
     * 有成员被禁言，此处不同于blacklist
     *
     * @param groupId 	产生禁言的群组id
     * @param mutes 	被禁言的成员列表
     *              	Map.entry.key 是禁言的成员id，Map.entry.value是禁言动作存在的时间。
     * @return NA
     *
     * \~english
     * Callback when group member(s) is muted (added to mute list), and is not allowed to post message temporarily based on muted time duration
     *
     * @param groupId		group id
     * @param mutes 		member(s) added to mute list
     *	 			        Map.entry.key is muted username. Map.entry.value is the duration of muted time, in milliseconds
     * @return NA
     */
    public void onMuteListAdded(String groupId, final List<String> mutes, final long muteExpire) {}

    /**
     * \~chinese
     * 有成员从禁言列表中移除，恢复发言权限，此处不同于blacklist
     *
     * @param groupId 	产生禁言的群组id
     * @param mutes 	有成员从群组禁言列表中移除
     * @return NA
     *
     * \~english
     * Callback when group member(s) is unmuted (removed from mute list), and allow to post message now
     *
     * @param groupId		group id
     * @param mutes 		members that be removed from mute list
     * @return NA
     */
    public void onMuteListRemoved(String groupId, final List<String> mutes) {}

    /**
     * \~chinese
     * 添加成员管理员权限
     *
     * @param groupId 		添加管理员权限对应的群组
     * @param administrator 被添加为管理员的成员
     * @return NA
     *
     * \~english
     * Callback when a member has been changed to admin
     *
     * @param groupId		group id
     * @param administrator member who has been changed to admin
     */
    public void onAdminAdded(String groupId, String administrator) {}

    /**
     * \~chinese
     * 取消某管理员权限
     * @param groupId 		取消管理员权限事件发生的群id
     * @param administrator 被取消管理员权限的成员
     *
     * \~english
     * Callback when member is removed from admin
     *
     * @param groupId 		group id
     * @param administrator the member whose admin permission is removed
     * @return NA
     */
    public void onAdminRemoved(String groupId, String administrator) {}

    /**
     * \~chinese
     * 转移群组所有者权限
     * @param groupId 	转移群组所有者权限的群id
     * @param newOwner 	新的群组所有者
     * @param oldOwner 	原群组所有者
     *
     * \~english
     * Callback when chat room ownership has been transferred
     *
     * @param groupId 		group id
     * @param newOwner 		new owner
     * @param oldOwner 		previous owner
     * @return NA
     */
    public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {}

    /**
     * \~chinese
     * 群组加入新成员事件
     *
     * @param groupId
     *          群组id
     * @param member
     *          新成员id
     *
     * \~english
     * Callback when a member join the group.
     *
     * @param groupId        group id
     * @param member   new member's id
     */
    public void onMemberJoined(final String groupId, final String member) {}

    /**
     * \~chinese
     * 群组成员主动退出事件
     *
     * @param groupId
     *          群组id
     * @param member
     *          退出的成员的id
     *
     * \~english
     * Callback when a member exited the group
     *
     * @param groupId       group id
     * @param member  the member who exited the group
     */
    public void onMemberExited(final String groupId,  final String member) {}
}
