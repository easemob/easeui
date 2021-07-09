package com.hyphenate.easeui.manager;

import com.hyphenate.easeui.adapter.EaseAdapterDelegate;
import com.hyphenate.easeui.adapter.EaseBaseDelegateAdapter;
import com.hyphenate.easeui.delegate.EaseCustomAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseExpressionAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseFileAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseImageAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseLocationAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseTextAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseVideoAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseVoiceAdapterDelegate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EaseMessageTypeSetManager {
    private static EaseMessageTypeSetManager mInstance;
    private EaseAdapterDelegate<?,?> defaultDelegate = new EaseTextAdapterDelegate();
    private Class<? extends EaseAdapterDelegate<?,?>> defaultDelegateCls;
    private Set<Class<? extends EaseAdapterDelegate<?, ?>>> delegates = new HashSet<>();
    private List<Class<? extends EaseAdapterDelegate<?, ?>>> delegateList = new ArrayList<>();
    private boolean hasConsistItemType;

    private EaseMessageTypeSetManager(){}

    public static EaseMessageTypeSetManager getInstance() {
        if(mInstance == null) {
            synchronized (EaseMessageTypeSetManager.class) {
                if(mInstance == null) {
                    mInstance = new EaseMessageTypeSetManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 是否使用自定义的item ViewType
     * @param hasConsistItemType
     * @return
     */
    public EaseMessageTypeSetManager setConsistItemType(boolean hasConsistItemType) {
        this.hasConsistItemType = hasConsistItemType;
        return this;
    }

    public EaseMessageTypeSetManager addMessageType(Class<? extends EaseAdapterDelegate<?, ?>> cls) {
        int size = delegates.size();
        delegates.add(cls);
        if(delegates.size() > size) {
            delegateList.add(cls);
        }
        return this;
    }

    /**
     * 设置默认的对话类型
     * @param cls
     * @return
     */
    public EaseMessageTypeSetManager setDefaultMessageType(Class<? extends EaseAdapterDelegate<?, ?>> cls) {
        this.defaultDelegateCls = cls;
        return this;
    }

    /**
     * 注册消息类型
     * @param adapter
     */
    public void registerMessageType(EaseBaseDelegateAdapter adapter) throws InstantiationException, IllegalAccessException{
        if(adapter == null) {
            return;
        }
        //如果没有注册聊天类型，则使用默认的
        if(delegateList.size() <= 0) {
            addMessageType(EaseExpressionAdapterDelegate.class)       //自定义表情
            .addMessageType(EaseFileAdapterDelegate.class)             //文件
            .addMessageType(EaseImageAdapterDelegate.class)            //图片
            .addMessageType(EaseLocationAdapterDelegate.class)         //定位
            .addMessageType(EaseVideoAdapterDelegate.class)            //视频
            .addMessageType(EaseVoiceAdapterDelegate.class)            //声音
            .addMessageType(EaseCustomAdapterDelegate.class)           //自定义消息
            .setDefaultMessageType(EaseTextAdapterDelegate.class);       //文本
        }
        for (Class<? extends EaseAdapterDelegate<?, ?>> cls : delegateList) {
            EaseAdapterDelegate delegate = cls.newInstance();
            adapter.addDelegate(delegate);
        }

        if(defaultDelegateCls == null) {
            defaultDelegate = new EaseTextAdapterDelegate();
        }else {
            defaultDelegate = defaultDelegateCls.newInstance();
        }
        adapter.setFallbackDelegate(defaultDelegate);
    }

    public boolean hasConsistItemType() {
        return this.hasConsistItemType;
    }

    public void release() {
        defaultDelegate = null;
    }

}
