package com.easemob.easeuisimpledemo.ui;

import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.easemob.chat.EMConversation;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.easeui.ui.EaseBaseActivity;
import com.easemob.easeui.ui.EaseContactListFragment;
import com.easemob.easeui.ui.EaseContactListFragment.EaseContactListItemClickListener;
import com.easemob.easeui.ui.EaseConversationListFragment;
import com.easemob.easeui.ui.EaseConversationListFragment.EaseConversationListItemClickListener;
import com.easemob.easeuisimpledemo.R;

public class MainActivity extends EaseBaseActivity{
    private TextView unreadLabel;
    private Button[] mTabs;
    private EaseConversationListFragment conversationListFragment;
    private EaseContactListFragment contactListFragment;
    private SettingsFragment settingFragment;
    private Fragment[] fragments;
    private int index;
    private int currentTabIndex;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_main);
        
        unreadLabel = (TextView) findViewById(R.id.unread_msg_number);
        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_conversation);
        mTabs[1] = (Button) findViewById(R.id.btn_address_list);
        mTabs[2] = (Button) findViewById(R.id.btn_setting);
        // 把第一个tab设为选中状态
        mTabs[0].setSelected(true);
        
        conversationListFragment = new EaseConversationListFragment();
        contactListFragment = new EaseContactListFragment();
        settingFragment = new SettingsFragment();
        contactListFragment.setContactsMap(getContacts());
        conversationListFragment.setConversationListItemClickListener(new EaseConversationListItemClickListener() {
            
            @Override
            public void onListItemClicked(EMConversation conversation) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, conversation.getUserName()));
            }
        });
        contactListFragment.setContactListItemClickListener(new EaseContactListItemClickListener() {
            
            @Override
            public void onListItemClicked(EaseUser user) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, user.getUsername()));
            }
        });
        fragments = new Fragment[] { conversationListFragment, contactListFragment, settingFragment };
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, conversationListFragment)
                .add(R.id.fragment_container, contactListFragment).hide(contactListFragment).show(conversationListFragment)
                .commit();
    }
    
    /**
     * button点击事件
     * 
     * @param view
     */
    public void onTabClicked(View view) {
        switch (view.getId()) {
        case R.id.btn_conversation:
            index = 0;
            break;
        case R.id.btn_address_list:
            index = 1;
            break;
        case R.id.btn_setting:
            index = 2;
            break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        mTabs[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        mTabs[index].setSelected(true);
        currentTabIndex = index;
    }
    
    /**
     * 临时生成的数据，密码皆为123456，可以登录测试接发消息
     * @return
     */
    private Map<String, EaseUser> getContacts(){
        Map<String, EaseUser> contacts = new HashMap<String, EaseUser>();
        for(int i = 1; i <= 10; i++){
            EaseUser user = new EaseUser("easeuitest" + i);
            contacts.put("easeuitest" + i, user);
        }
        return contacts;
    }
}
