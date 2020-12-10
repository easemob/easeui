package com.hyphenate.easeui.adapter;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * （1）作为代理类的基类，通过{@link EaseAdapterDelegatesManager}实现与{@link RecyclerView.Adapter}的关联。
 * （2）判断是否使用这个代理类的方法为{@link #isForViewType(Object, int)}，如果它返回true就代表这个代理类进行数据处理。
 * 具体逻辑，可见{@link EaseAdapterDelegatesManager#getItemViewType(Object, int)}。
 * （3）{@link EaseBaseDelegateAdapter#getItemViewType(int)}中得到不同delegate对应的viewType。
 * （4）{@link EaseBaseDelegateAdapter#getViewHolder(ViewGroup, int)}通过ViewType得到对应的{@link #onCreateViewHolder(ViewGroup, String)},
 * 这个{@link #onCreateViewHolder(ViewGroup, String)}就是需要具体的实现。
 * 特别说明一下：
 * 本文是参考：https://github.com/xuehuayous/DelegationAdapter实现而来。
 * 针对具体的项目中，如何实现对话类型的方便插入，而且对象类型一般是接收方和发送发两种样式，但是实现类似。
 * 针对这种情况，项目中使用tag进行区分，也就是说一个delegate对应多个tag，从而对应多个布局，所以这里就设计了tags集合用于管理
 * 同一个delegate对应多个tag的情况，并对{@link EaseAdapterDelegatesManager#getItemViewType(Object, int)}中相关逻辑进行修改。
 *
 * @param <T>
 * @param <VH>
 */
public abstract class EaseAdapterDelegate<T, VH extends RecyclerView.ViewHolder> implements Cloneable{
    public static final String DEFAULT_TAG = "";
    private String tag = DEFAULT_TAG;
    public List<String> tags = new ArrayList<>();

    public EaseAdapterDelegate() {
        setTag(this.tag);
    }

    public EaseAdapterDelegate(String tag) {
        setTag(tag);
    }

    public boolean isForViewType(T item, int position) {
        return true;
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, String tag);

    public void onBindViewHolder(VH holder, int position, T item){}

    public int getItemCount() {
        return 0;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * should update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at
     * the given position.
     */
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads,
                                 T item) {}

    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {}

    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        return false;
    }

    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {}

    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {}

    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {}

    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {}

    public int getItemViewType() { return 0;}

    public void setTag(String tag) {
        this.tag = tag;
        tags.add(tag);
    }

    public String getTag() {
        return tag;
    }

    public List<String> getTags() {
        return tags;
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
