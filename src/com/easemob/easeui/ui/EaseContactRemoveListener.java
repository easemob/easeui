package com.easemob.easeui.ui;

import com.easemob.chat.EMContactListener;

import java.util.List;

/**
 * Created by wei on 2017/1/10.
 */

public abstract class EaseContactRemoveListener implements EMContactListener{
    @Override
    public void onContactAdded(List<String> usernameList) {

    }

    @Override
    public void onContactInvited(String username, String reason) {

    }

    @Override
    public void onContactAgreed(String username) {

    }

    @Override
    public void onContactRefused(String username) {

    }
}
