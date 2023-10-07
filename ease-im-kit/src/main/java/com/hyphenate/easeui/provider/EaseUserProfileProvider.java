package com.hyphenate.easeui.provider;
import com.hyphenate.easeui.domain.EaseUser;

/**
 * User profile provider
 * @author wei
 *
 */
public interface EaseUserProfileProvider {
    /**
     * return EaseUser for input username
     * @param username
     * @return
     */
    EaseUser getUser(String username);

    /**
     * If not overridden will return the info from {@link #getUser(String)}
     * @param groupId
     * @param userId
     * @return
     */
    default EaseUser getGroupUser(String groupId,String userId){
        return getUser(userId);
    }

    default String getContactRemark(String username){
        return null;
    }
}