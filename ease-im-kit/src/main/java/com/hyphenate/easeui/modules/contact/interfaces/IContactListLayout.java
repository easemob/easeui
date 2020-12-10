package com.hyphenate.easeui.modules.contact.interfaces;

import com.hyphenate.easeui.modules.contact.adapter.EaseContactListAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.modules.contact.presenter.EaseContactPresenter;
import com.hyphenate.easeui.modules.interfaces.IRecyclerView;

public interface IContactListLayout extends IRecyclerView {

    /**
     * 添加其他类型的代理类
     * @param delegate
     */
    //void addDelegate(EaseBaseConversationDelegate delegate);

    /**
     * 设置presenter
     * @param presenter
     */
    void setPresenter(EaseContactPresenter presenter);

    /**
     * 是否展示默认的条目菜单
     * @param showDefault
     */
    void showItemDefaultMenu(boolean showDefault);

    /**
     * 是否展示条目头部字母
     * @param showItemHeader
     */
    void showItemHeader(boolean showItemHeader);

    /**
     * 获取数据适配器
     * @return
     */
    EaseContactListAdapter getListAdapter();

    /**
     * 获取条目数据
     * @param position
     * @return
     */
    EaseUser getItem(int position);

}
