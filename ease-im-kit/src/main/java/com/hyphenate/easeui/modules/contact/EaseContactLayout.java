package com.hyphenate.easeui.modules.contact;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.SidebarPresenter;
import com.hyphenate.easeui.modules.contact.interfaces.IContactLayout;
import com.hyphenate.easeui.modules.contact.interfaces.OnContactLoadListener;
import com.hyphenate.easeui.widget.EaseSidebar;

import java.util.List;

public class EaseContactLayout extends RelativeLayout implements IContactLayout, SwipeRefreshLayout.OnRefreshListener, OnContactLoadListener {
    private SwipeRefreshLayout srlContactRefresh;
    private EaseContactListLayout contactList;
    private EaseSidebar sideBarContact;
    private TextView floatingHeader;

    private SidebarPresenter sidebarPresenter;
    private boolean canUseRefresh = true;

    public EaseContactLayout(Context context) {
        this(context, null);
    }

    public EaseContactLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseContactLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ease_layout_contact, this);
        initViews();
        initListener();
    }

    private void initViews() {
        srlContactRefresh = findViewById(R.id.srl_contact_refresh);
        contactList = findViewById(R.id.contact_list);
        sideBarContact = findViewById(R.id.side_bar_contact);
        floatingHeader = findViewById(R.id.floating_header);

        srlContactRefresh.setEnabled(canUseRefresh);

        sidebarPresenter = new SidebarPresenter();
        sidebarPresenter.setupWithRecyclerView(contactList, contactList.getListAdapter(), floatingHeader);
        sideBarContact.setOnTouchEventListener(sidebarPresenter);
    }

    private void initListener() {
        srlContactRefresh.setOnRefreshListener(this);
        contactList.setOnContactLoadListener(this);
    }

    public void loadDefaultData() {
        contactList.loadDefaultData();
    }

    /**
     * 展示简洁模式
     */
    public void showSimple() {
        contactList.showItemHeader(false);
        sideBarContact.setVisibility(GONE);
    }

    /**
     * 展示常规模式
     */
    public void showNormal() {
        contactList.showItemHeader(true);
        sideBarContact.setVisibility(VISIBLE);
    }

    @Override
    public void canUseRefresh(boolean canUseRefresh) {
        this.canUseRefresh = canUseRefresh;
        srlContactRefresh.setEnabled(canUseRefresh);
    }

    @Override
    public EaseContactListLayout getContactList() {
        return contactList;
    }

    @Override
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return srlContactRefresh;
    }

    @Override
    public void onRefresh() {
        contactList.loadDefaultData();
    }

    @Override
    public void loadDataFinish(List<EaseUser> data) {
        finishRefresh();
    }

    @Override
    public void loadDataFail(String message) {
        finishRefresh();
    }

    private void finishRefresh() {
        if(srlContactRefresh != null) {
            post(() -> srlContactRefresh.setRefreshing(false));
        }
    }
}

