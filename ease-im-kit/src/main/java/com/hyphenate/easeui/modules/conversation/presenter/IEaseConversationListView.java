package com.hyphenate.easeui.modules.conversation.presenter;

import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.modules.ILoadDataView;

import java.util.List;

public interface IEaseConversationListView extends ILoadDataView {
    /**
     * 获取会话列表数据成功
     * @param data
     */
    void loadConversationListSuccess(List<EaseConversationInfo> data);

    /**
     * 没有获取到会话列表数据
     */
    void loadConversationListNoData();

    /**
     * 获取失败
     * @param message
     */
    void loadConversationListFail(String message);

    /**
     * 对数据排序完成
     * @param data
     */
    void sortConversationListSuccess(List<EaseConversationInfo> data);

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
     * 删除列表中某条
     * @param position
     */
    void deleteItem(int position);

    /**
     * 删除条目失败
     * @param position
     * @param message
     */
    void deleteItemFail(int position, String message);
}
