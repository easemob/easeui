package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.interfaces.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 作为RecyclerView Adapter的基类，有默认空白布局
 * 如果要修改默认布局可以采用以下两种方式：1、在app layout中新建ease_layout_default_no_data.xml覆盖。
 * 2、继承EaseBaseRecyclerViewAdapter后，重写getEmptyLayoutId()方法，返回自定义的布局即可。
 * 3、{@link #VIEW_TYPE_EMPTY}建议设置成负值，以防占用{@link EaseAdapterDelegatesManager#addDelegate(EaseAdapterDelegate, String)}中相应的position
 * @param <T>
 */
public abstract class EaseBaseRecyclerViewAdapter<T> extends EaseBaseAdapter<EaseBaseRecyclerViewAdapter.ViewHolder> {
    public static final int VIEW_TYPE_EMPTY = -1;
    public static final int VIEW_TYPE_ITEM = 0;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    public Context mContext;
    public List<T> mData;
    private boolean hideEmptyView;
    private View emptyView;
    private int emptyViewId;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if(viewType == VIEW_TYPE_EMPTY) {
            return getEmptyViewHolder(parent);
        }
        ViewHolder holder = getViewHolder(parent, viewType);
        if(isItemClickEnable()) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickAction(v, holder.getBindingAdapterPosition());
                }
            });
        }
        if(isItemLongClickEnable()) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return itemLongClickAction(v, holder.getBindingAdapterPosition());
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EaseBaseRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.setAdapter(this);
        //增加viewType类型的判断
        if(isEmptyViewType(position)) {
            return;
        }
        if(mData == null || mData.isEmpty()) {
            return;
        }
        T item = getItem(position);
        holder.setData(item, position);
        holder.setDataList(mData, position);
    }

    /**
     * 判断是否是空布局类型
     * @param position
     * @return
     */
    public boolean isEmptyViewType(int position) {
        int viewType = getItemViewType(position);
        return viewType == VIEW_TYPE_EMPTY;
    }

    public boolean itemLongClickAction(View v, int position) {
        if(mOnItemLongClickListener != null) {
            return mOnItemLongClickListener.onItemLongClick(v, position);
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return (mData == null || mData.isEmpty()) ? 1 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (mData == null || mData.isEmpty()) ? VIEW_TYPE_EMPTY : VIEW_TYPE_ITEM;
    }

    /**
     * 条目单击事件是否可用
     * 默认为true，如果需要自己设置请设置为false
     * @return
     */
    public boolean isItemClickEnable() {
        return true;
    }

    /**
     * 条目长按事件是否可用
     * 默认为true
     * @return
     */
    public boolean isItemLongClickEnable() {
        return true;
    }


    /**
     * 点击事件
     * @param v
     * @param position
     */
    public void itemClickAction(View v, int position) {
        if(mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, position);
        }
    }

    /**
     * 返回数据为空时的布局
     * @param parent
     * @return
     */
    private ViewHolder getEmptyViewHolder(ViewGroup parent) {
        View emptyView = getEmptyView(parent);
        if(this.emptyView != null) {
            emptyView = this.emptyView;
        }
        if(this.emptyViewId > 0) {
            emptyView = LayoutInflater.from(mContext).inflate(this.emptyViewId, parent, false);
        }
        if(hideEmptyView) {
            emptyView = LayoutInflater.from(mContext).inflate(R.layout.ease_layout_no_data_show_nothing, parent, false);
        }
        return new ViewHolder<T>(emptyView) {

            @Override
            public void initView(View itemView) {

            }

            @Override
            public void setData(T item, int position) {

            }
        };
    }

    /**
     * 隐藏空白布局
     * @param hide
     */
    public void hideEmptyView(boolean hide) {
        hideEmptyView = hide;
        notifyDataSetChanged();
    }

    /**
     * 设置空白布局
     * @param emptyView
     */
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        notifyDataSetChanged();
    }

    /**
     * 设置空白布局
     * @param emptyViewId
     */
    public void setEmptyView(@LayoutRes int emptyViewId) {
        this.emptyViewId = emptyViewId;
        notifyDataSetChanged();
    }

    /**
     * 获取空白布局
     * @param parent
     * @return
     */
    private View getEmptyView(ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(getEmptyLayoutId(), parent, false);
    }

    /**
     * 获取ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    public abstract ViewHolder getViewHolder(ViewGroup parent, int viewType);

    /**
     * 根据position获取相应的data
     * @param position
     * @return
     */
    public T getItem(int position) {
        return mData == null ? null : mData.get(position);
    }

    /**
     * 添加数据
     * @param data
     */
    public void setData(List<T> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    /**
     * 添加单个数据
     * @param item
     */
    public void addData(T item) {
        synchronized (EaseBaseRecyclerViewAdapter.class) {
            if(this.mData == null) {
                this.mData = new ArrayList<>();
            }
            this.mData.add(item);
        }
        notifyDataSetChanged();
    }

    /**
     * 添加更多数据
     * @param data
     */
    public void addData(List<T> data) {
        synchronized (EaseBaseRecyclerViewAdapter.class) {
            if(data == null || data.isEmpty()) {
                return;
            }
            if(this.mData == null) {
                this.mData = data;
            }else {
                this.mData.addAll(data);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 添加更多数据
     * @param position 插入位置
     * @param data
     */
    public void addData(int position, List<T> data) {
        synchronized (EaseBaseRecyclerViewAdapter.class) {
            if(data == null || data.isEmpty()) {
                return;
            }
            if(this.mData == null) {
                this.mData = data;
            }else {
                this.mData.addAll(position, data);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 添加更多数据
     * @param position
     * @param data
     * @param refresh
     */
    public void addData(int position, List<T> data, boolean refresh) {
        synchronized (EaseBaseRecyclerViewAdapter.class) {
            if(data == null || data.isEmpty()) {
                return;
            }
            if(this.mData == null) {
                this.mData = data;
            }else {
                this.mData.addAll(position, data);
            }
        }
        if(refresh) {
            notifyDataSetChanged();
        }
    }

    /**
     * 获取数据
     * @return
     */
    public List<T> getData() {
        return mData;
    }

    /**
     * 清除数据
     */
    public void clearData() {
        if(mData != null) {
            mData.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * set item click
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * set item long click
     * @param longClickListener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        mOnItemLongClickListener = longClickListener;
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        private EaseBaseAdapter adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);
        }

        /**
         * 初始化控件
         * @param itemView
         */
        public abstract void initView(View itemView);

        /**
         * 设置数据
         * @param item
         * @param position
         */
        public abstract void setData(T item, int position);

        /**
         * @param id
         * @param <E>
         * @return
         */
        public  <E extends View> E findViewById(@IdRes int id) {
            return this.itemView.findViewById(id);
        }

        /**
         * 设置数据，提供数据集合
         * @param data
         * @param position
         */
        public void setDataList(List<T> data, int position) { }

        /**
         * 设置 adapter
         * @param adapter
         */
        private void setAdapter(EaseBaseRecyclerViewAdapter adapter) {
            this.adapter = adapter;
        }

        /**
         * get adapter
         * @return
         */
        public EaseBaseAdapter getAdapter() {
            return adapter;
        }
    }

    /**
     * 返回空白布局
     * @return
     */
    public int getEmptyLayoutId() {
        return R.layout.ease_layout_default_no_data;
    }


}
