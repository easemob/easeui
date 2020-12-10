package com.hyphenate.easeui.modules.contact.presenter;

import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.modules.EaseBasePresenter;
import com.hyphenate.easeui.modules.ILoadDataView;

import java.util.List;

public abstract class EaseContactPresenter extends EaseBasePresenter {
    public IEaseContactListView mView;

    @Override
    public void attachView(ILoadDataView view) {
        mView = (IEaseContactListView) view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachView();
    }

    /**
     * 加载数据
     */
    public abstract void loadData();

    /**
     * 对数据排序
     * @param data
     */
    public abstract void sortData(List<EaseUser> data);


    /**
     * 添加备注
     * @param position
     * @param user
     */
    public abstract void addNote(int position, EaseUser user);

}
