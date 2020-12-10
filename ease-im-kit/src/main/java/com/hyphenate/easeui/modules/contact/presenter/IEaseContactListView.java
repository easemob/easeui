package com.hyphenate.easeui.modules.contact.presenter;

import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.modules.ILoadDataView;

import java.util.List;

public interface IEaseContactListView extends ILoadDataView {
    /**
     * 获取会话列表数据成功
     * @param data
     */
    void loadContactListSuccess(List<EaseUser> data);

    /**
     * 没有获取到会话列表数据
     */
    void loadContactListNoData();

    /**
     * 获取失败
     * @param message
     */
    void loadContactListFail(String message);

    /**
     * 对数据排序完成
     * @param data
     */
    void sortContactListSuccess(List<EaseUser> data);

    /**
     * 刷新列表
     */
    void refreshList();

    /**
     * 刷新列表
     * @param position
     */
    void refreshList(int position);

    /**
     * 添加备注
     * @param position
     */
    void addNote(int position);

    /**
     * 添加备注失败
     * @param position
     * @param message
     */
    void addNoteFail(int position, String message);
}
