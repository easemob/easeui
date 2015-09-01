/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easemob.easeui.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.easeui.R;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.easeui.widget.EaseContactList;
import com.easemob.exceptions.EaseMobException;

/**
 * 联系人列表页
 * 
 */
public class EaseContactListFragment extends EaseBaseFragment {
    private static final String TAG = "EaseContactListFragment";
    protected List<EaseUser> contactList;
    protected ListView listView;
    protected boolean hidden;
    protected List<String> blackList;
    protected ImageButton clearSearch;
    protected EditText query;
//    protected HXContactSyncListener contactSyncListener;
//    protected HXBlackListSyncListener blackListSyncListener;
    protected View progressBar;
    protected Handler handler = new Handler();
    protected EaseUser toBeProcessUser;
    protected String toBeProcessUsername;
    protected EaseContactList contactListLayout;
    protected boolean isConflict;
    
    protected EaseContactsProvider contactsProvider;

//    class HXContactSyncListener implements EaseSDKHelper.HXSyncListener {
//        @Override
//        public void onSyncSucess(final boolean success) {
//            EMLog.d(TAG, "on contact list sync success:" + success);
//            getActivity().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if(success){
//                            progressBar.setVisibility(View.GONE);
//                            refresh();
//                        }else{
//                            String s1 = getResources().getString(R.string.get_failed_please_check);
//                            Toast.makeText(getActivity(), s1, 1).show();
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    }
//                        
//            });
//        }
//    }
//    
//    class HXBlackListSyncListener implements HXSyncListener{
//
//        @Override
//        public void onSyncSucess(boolean success) {
//            getActivity().runOnUiThread(new Runnable(){
//
//                @Override
//                public void run() {
//                    blackList = EMContactManager.getInstance().getBlackListUsernames();
//                    refresh();
//                }
//                
//            });
//        }
//        
//    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ease_fragment_contact_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
        if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initView() {
        progressBar = (View) getView().findViewById(R.id.progress_bar);
        
        contactListLayout = (EaseContactList) getView().findViewById(R.id.contact_list);        
        listView = contactListLayout.getListView();
        
        //搜索框
        query = (EditText) getView().findViewById(R.id.query);
        clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
    }

    @Override
    protected void setUpView() {
        EMChatManager.getInstance().addConnectionListener(connectionListener);
        
        //黑名单列表
        blackList = EMContactManager.getInstance().getBlackListUsernames();
        contactList = new ArrayList<EaseUser>();
        // 获取设置contactlist
        getContactList();
        //init list
        contactListLayout.init(contactList);
        
        query.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactListLayout.filter(s);
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                    
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        clearSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                query.getText().clear();
                hideSoftKeyboard();
            }
        });
        
        listView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏软键盘
                hideSoftKeyboard();
                return false;
            }
        });


//        contactSyncListener = new HXContactSyncListener();
//        EaseSDKHelper.getInstance().addSyncContactListener(contactSyncListener);
//        
//        blackListSyncListener = new HXBlackListSyncListener();
//        EaseSDKHelper.getInstance().addSyncBlackListListener(blackListSyncListener);
//        
//        if (!EaseSDKHelper.getInstance().isContactsSyncedWithServer()) {
//            progressBar.setVisibility(View.VISIBLE);
//        } else {
//            progressBar.setVisibility(View.GONE);
//        }
        
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            refresh();
        }
    }


    /**
     * 把user移入到黑名单
     */
    protected void moveToBlacklist(final String username){
        final ProgressDialog pd = new ProgressDialog(getActivity());
        String st1 = getResources().getString(R.string.Is_moved_into_blacklist);
        final String st2 = getResources().getString(R.string.Move_into_blacklist_success);
        final String st3 = getResources().getString(R.string.Move_into_blacklist_failure);
        pd.setMessage(st1);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    //加入到黑名单
                    EMContactManager.getInstance().addUserToBlackList(username,false);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st2, 0).show();
                            refresh();
                        }
                    });
                } catch (EaseMobException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            Toast.makeText(getActivity(), st3, 0).show();
                        }
                    });
                }
            }
        }).start();
        
    }
    
    // 刷新ui
    public void refresh() {
        getContactList();
        contactListLayout.refresh();
    }

    @Override
    public void onDestroy() {
//        if (contactSyncListener != null) {
//            EaseSDKHelper.getInstance().removeSyncContactListener(contactSyncListener);
//            contactSyncListener = null;
//        }
//        
//        if(blackListSyncListener != null){
//            EaseSDKHelper.getInstance().removeSyncBlackListListener(blackListSyncListener);
//        }
        
        EMChatManager.getInstance().removeConnectionListener(connectionListener);
        
        super.onDestroy();
    }
    
    public void showProgressBar(boolean show) {
        if (progressBar != null) {
            if (show) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 获取联系人列表，并过滤掉黑名单和排序
     */
    protected void getContactList() {
        contactList.clear();
        synchronized (contactList) {
            //获取联系人列表
            if(contactsProvider == null){
                return;
            }
            Map<String, EaseUser> users = contactsProvider.getContactsMap();
            if(users == null){
                return;
            }
            Iterator<Entry<String, EaseUser>> iterator = users.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, EaseUser> entry = iterator.next();
                //兼容以前的通讯录里的已有的数据显示，加上此判断，如果是2.2.2后新集成的可以去掉此判断
                if (!entry.getKey().equals("item_new_friends")
                        && !entry.getKey().equals("item_groups")
                        && !entry.getKey().equals("item_chatroom")
                        && !entry.getKey().equals("item_robots")){
                    if(!blackList.contains(entry.getKey())){
                        //不显示黑名单中的用户
                        EaseUser user = entry.getValue();
                        EaseCommonUtils.setUserInitialLetter(user);
                        contactList.add(user);
                    }
                }
            }
            // 排序
            Collections.sort(contactList, new Comparator<EaseUser>() {

                @Override
                public int compare(EaseUser lhs, EaseUser rhs) {
                    if(lhs.getInitialLetter().equals(rhs.getInitialLetter())){
                        return lhs.getNick().compareTo(rhs.getNick());
                    }else{
                        return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                    }
                    
                }
            });
        }

    }
    
    
    
    protected EMConnectionListener connectionListener = new EMConnectionListener() {
        
        @Override
        public void onDisconnected(int error) {
            if (error == EMError.USER_REMOVED || error == EMError.CONNECTION_CONFLICT) {
                isConflict = true;
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        onConnectionDisconnected();
                    }

                });
            }
        }
        
        @Override
        public void onConnected() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    onConnectionConnected();
                }

            });
        }
    };
    
    
    protected void onConnectionDisconnected() {
        
    }
    
    protected void onConnectionConnected() {
        
    }
        
    interface EaseContactsProvider {
        Map<String, EaseUser> getContactsMap();
    }
    
    public void setContactsProvider(EaseContactsProvider contactsProvider){
        this.contactsProvider = contactsProvider;
    }
    
}
