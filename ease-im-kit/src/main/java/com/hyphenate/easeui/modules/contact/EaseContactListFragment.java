package com.hyphenate.easeui.modules.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.modules.menu.EasePopupMenuHelper;
import com.hyphenate.easeui.modules.menu.OnPopupMenuItemClickListener;
import com.hyphenate.easeui.modules.menu.OnPopupMenuPreShowListener;
import com.hyphenate.easeui.ui.base.EaseBaseFragment;

public class EaseContactListFragment extends EaseBaseFragment implements OnPopupMenuItemClickListener, OnPopupMenuPreShowListener, OnItemClickListener {
    public LinearLayout llRoot;
    public EaseContactLayout contactLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), null);
    }

    public int getLayoutId() {
        return R.layout.ease_fragment_contacts;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(savedInstanceState);
        initListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    public void initView(Bundle savedInstanceState) {
        llRoot = findViewById(R.id.ll_root);
        contactLayout = findViewById(R.id.contact_layout);
    }

    public void initListener() {
        contactLayout.getContactList().setOnPopupMenuPreShowListener(this);
        contactLayout.getContactList().setOnPopupMenuItemClickListener(this);
        contactLayout.getContactList().setOnItemClickListener(this);
    }

    public void initData() {
        contactLayout.loadDefaultData();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item, int position) {
        return false;
    }

    @Override
    public void onMenuPreShow(EasePopupMenuHelper menuHelper, int position) {

    }

    @Override
    public void onItemClick(View view, int position) {

    }
}

