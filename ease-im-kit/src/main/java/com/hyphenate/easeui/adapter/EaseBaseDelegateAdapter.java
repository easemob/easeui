package com.hyphenate.easeui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * （1）做为与{@link RecyclerView}关联的adapter的基类，实现了与{@link EaseAdapterDelegatesManager}的关联,
 * 将{@link RecyclerView.Adapter}中的{@link #getItemViewType(int)}, {@link #getViewHolder(ViewGroup, int)},
 * {@link #onViewRecycled(ViewHolder)}等与{@link EaseAdapterDelegatesManager}关联。
 * （2）需要注意的是{@link #onBindViewHolder(ViewHolder, int)}已经在父类{@link EaseBaseRecyclerViewAdapter}中将绑定
 * 数据的实现委托在ViewHolder中了，在本类中就不需要再和{@link EaseAdapterDelegatesManager}绑定。
 * （3）添加布局，获取ViewHolder均通过各自的delegate实现。见基类{@link EaseAdapterDelegate}。
 * @param <T>
 */
public abstract class EaseBaseDelegateAdapter<T> extends EaseBaseRecyclerViewAdapter<T> {
    private static final String TAG = "adapter";
    private EaseAdapterDelegatesManager delegatesManager;

    public EaseBaseDelegateAdapter() {
        this.delegatesManager = new EaseAdapterDelegatesManager(false);
    }

    public EaseBaseDelegateAdapter(EaseAdapterDelegatesManager delegatesManager) {
        this.delegatesManager = delegatesManager;
    }

    public EaseBaseDelegateAdapter addDelegate(EaseAdapterDelegate delegate) {
        delegatesManager.addDelegate(delegate, delegate.getTag());
        notifyDataSetChanged();
        return this;
    }

    public EaseBaseDelegateAdapter addDelegate(EaseAdapterDelegate delegate, String tag) {
        delegate.setTag(tag);
        return addDelegate(delegate);
    }

    public int getDelegateViewType(EaseAdapterDelegate delegate) {
        return delegatesManager.getDelegateViewType(delegate);
    }

    public EaseBaseDelegateAdapter setFallbackDelegate(EaseAdapterDelegate delegate, String tag) {
        delegate.setTag(tag);
        return setFallbackDelegate(delegate);
    }

    public EaseBaseDelegateAdapter setFallbackDelegate(EaseAdapterDelegate delegate) {
        delegatesManager.fallbackDelegate = delegate;
        return this;
    }

    public EaseAdapterDelegate getAdapterDelegate(int viewType) {
        return delegatesManager.getDelegate(viewType);
    }

    public List<EaseAdapterDelegate<Object, ViewHolder>> getAllDelegate() {
        return delegatesManager.getAllDelegates();
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        try {
            viewType = delegatesManager.getItemViewType(getItem(position), position);
        } catch (Exception e) {
            return super.getItemViewType(position);
        }
        return viewType;
    }

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(isEmptyViewType(position)) {
            return;
        }
        if(mData == null || mData.isEmpty()) {
            return;
        }
        if(!delegatesManager.getAllDelegates().isEmpty()) {
            delegatesManager.onBindViewHolder(holder, position, getItem(position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if(isEmptyViewType(position)) {
            return;
        }
        if(mData == null || mData.isEmpty()) {
            return;
        }
        if(!delegatesManager.getAllDelegates().isEmpty()) {
            delegatesManager.onBindViewHolder(holder, position, payloads, getItem(position));
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        delegatesManager.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        return delegatesManager.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        delegatesManager.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        delegatesManager.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        delegatesManager.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        delegatesManager.onDetachedFromRecyclerView(recyclerView);
    }

}
