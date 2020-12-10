package com.hyphenate.easeui.modules.conversation.interfaces;

import com.hyphenate.easeui.modules.conversation.adapter.EaseConversationListAdapter;
import com.hyphenate.easeui.modules.conversation.presenter.EaseConversationPresenter;
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo;
import com.hyphenate.easeui.modules.interfaces.IRecyclerView;

public interface IConversationListLayout extends IRecyclerView {

    /**
     * 添加其他类型的代理类
     * @param delegate
     */
    //void addDelegate(EaseBaseConversationDelegate delegate);

    /**
     * 设置presenter
     * @param presenter
     */
    void setPresenter(EaseConversationPresenter presenter);

    /**
     * 是否展示默认的条目菜单
     * @param showDefault
     */
    void showItemDefaultMenu(boolean showDefault);

    /**
     * 获取数据适配器
     * @return
     */
    EaseConversationListAdapter getListAdapter();

    /**
     * 获取条目数据
     * @param position
     * @return
     */
    EaseConversationInfo getItem(int position);


    /**
     * 将对话置为已读
     * @param position
     * @param info
     */
    void makeConversionRead(int position, EaseConversationInfo info);

    /**
     * 置顶
     * @param position
     * @param info
     */
    void makeConversationTop(int position, EaseConversationInfo info);

    /**
     * 取消置顶
     * @param position
     * @param info
     */
    void cancelConversationTop(int position, EaseConversationInfo info);

    /**
     * 删除会话
     * @param position
     * @param info
     */
    void deleteConversation(int position, EaseConversationInfo info);

    /**
     * 设置会话变化的监听
     * @param listener
     */
    void setOnConversationChangeListener(OnConversationChangeListener listener);

    /**
     * 设置加载会话状态监听
     * @param loadListener
     */
    void setOnConversationLoadListener(OnConversationLoadListener loadListener);
}
