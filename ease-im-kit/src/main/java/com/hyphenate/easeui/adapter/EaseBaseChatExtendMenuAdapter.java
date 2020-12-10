package com.hyphenate.easeui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class EaseBaseChatExtendMenuAdapter<VH extends RecyclerView.ViewHolder, T> extends RecyclerView.Adapter<VH> {
    public List<T> mData;

    /**
     * 设置数据
     * @param data
     */
    public void setData(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutId(), parent, false);
        return easeCreateViewHolder(view);
    }

    /**
     * 获取条目布局
     * @return
     */
    protected abstract int getItemLayoutId();

    /**
     * 获取ViewHolder
     * @param view
     * @return
     */
    protected abstract VH easeCreateViewHolder(View view);

}

