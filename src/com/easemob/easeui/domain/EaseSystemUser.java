package com.easemob.easeui.domain;

public class EaseSystemUser extends EaseUser{
    protected int unreadMsgCount;
    
    public EaseSystemUser(String username) {
        super(username);
    }
    
    public int getUnreadMsgCount() {
        return unreadMsgCount;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }

}
