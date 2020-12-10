package com.hyphenate.easeui.modules.contact.interfaces;

import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.modules.contact.adapter.EaseContactCustomAdapter;

public interface IContactCustomListLayout {
    /**
     * 添加头部条目
     * @param id
     * @param imageResource
     * @param name
     */
    void addCustomItem(int id, int imageResource, String name);

    /**
     * 添加头部条目
     * @param id
     * @param image
     * @param name
     */
    void addCustomItem(int id, String image, String name);

    /**
     * 设置点击事件
     * @param listener
     */
    void setOnCustomItemClickListener(OnItemClickListener listener);

    /**
     * 设置联系人加载结果监听
     * @param loadListener
     */
    void setOnContactLoadListener(OnContactLoadListener loadListener);

    /**
     * 获取头部适配器
     */
    EaseContactCustomAdapter getCustomAdapter();
}

