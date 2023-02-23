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

    default EaseUser getGroupUser(String groupId,String userId){
        return getUser(userId);
    }
}