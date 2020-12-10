package com.hyphenate.easeui.modules.contact.presenter;


import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EaseContactPresenterImpl extends EaseContactPresenter {
    @Override
    public void loadData() {
        runOnIO(() -> {
            try {
                List<String> usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                List<String> ids = EMClient.getInstance().contactManager().getSelfIdsOnOtherPlatform();
                if((usernames == null || usernames.isEmpty()) && (ids == null || ids.isEmpty())) {
                    if(!isDestroy()) {
                        runOnUI(() -> mView.loadContactListNoData());
                    }
                    return;
                }
                if(usernames == null) {
                    usernames = new ArrayList<>();
                }
                if(ids != null && !ids.isEmpty()) {
                    usernames.addAll(ids);
                }
                List<EaseUser> easeUsers = EaseUser.parse(usernames);
                if(easeUsers != null && !easeUsers.isEmpty()) {
                    List<String> blackListFromServer = EMClient.getInstance().contactManager().getBlackListFromServer();
                    if(blackListFromServer != null && !blackListFromServer.isEmpty()) {
                        for (EaseUser user : easeUsers) {
                            if(blackListFromServer != null && !blackListFromServer.isEmpty()) {
                                if(blackListFromServer.contains(user.getUsername())) {
                                    user.setContact(1);
                                }
                            }
                        }
                    }
                }
                runOnUI(() -> {
                    if(!isDestroy()) {
                        mView.loadContactListSuccess(easeUsers);
                    }
                });
            } catch (HyphenateException e) {
                e.printStackTrace();
                if(!isDestroy()) {
                    runOnUI(()-> mView.loadContactListFail(e.getDescription()));
                }

            }
        });
    }

    @Override
    public void sortData(List<EaseUser> data) {
        if(data != null) {
            checkUserProvider(data);
            sortList(data);
            if(!isDestroy()) {
                runOnUI(() -> mView.sortContactListSuccess(data));
            }
        }
    }

    private void checkUserProvider(List<EaseUser> data) {
        EaseUserProfileProvider provider = EaseIM.getInstance().getUserProvider();
        if(provider != null) {
            for (EaseUser user : data) {
                EaseUser setUser = provider.getUser(user.getUsername());
                if(setUser != null) {
                    if(TextUtils.isEmpty(user.getNickname()) || TextUtils.equals(user.getNickname(), user.getUsername())) {
                        user.setNickname(setUser.getNickname());
                    }
                    if(TextUtils.isEmpty(user.getAvatar())) {
                        user.setAvatar(setUser.getAvatar());
                    }
                }
            }
        }
    }

    /**
     * 排序
     * @param list
     */
    private void sortList(List<EaseUser> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        Collections.sort(list, new Comparator<EaseUser>() {
            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if(lhs.getInitialLetter().equals(rhs.getInitialLetter())){
                    return lhs.getNickname().compareTo(rhs.getNickname());
                }else{
                    if("#".equals(lhs.getInitialLetter())){
                        return 1;
                    }else if("#".equals(rhs.getInitialLetter())){
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }
            }
        });
    }

    @Override
    public void addNote(int position, EaseUser user) {
        if(!isDestroy()) {
            runOnUI(() -> mView.addNoteFail(position, mView.context().getString(R.string.ease_contact_add_note_developing)));
        }
    }
}

