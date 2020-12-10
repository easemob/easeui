package com.hyphenate.easeui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 作为其他常规delegate的基类
 * @param <T>
 * @param <VH>
 */
public abstract class EaseBaseDelegate<T, VH extends EaseBaseRecyclerViewAdapter.ViewHolder> extends EaseAdapterDelegate<T, VH> {

    @Override
    public VH onCreateViewHolder(ViewGroup parent, String tag) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        return createViewHolder(view);
    }

    protected abstract int getLayoutId();

    protected abstract VH createViewHolder(View view);
}
