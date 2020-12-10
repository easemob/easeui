package com.hyphenate.easeui.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter.ViewHolder;
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate;

public class EaseAdapterDelegatesManager {
    private boolean hasConsistItemType;
    private SparseArrayCompat<String> dataTypeWithTags = new SparseArrayCompat<>();
    private SparseArrayCompat<EaseAdapterDelegate<Object, ViewHolder>> delegates = new SparseArrayCompat<>();
    public EaseAdapterDelegate<Object, ViewHolder> fallbackDelegate;

    public EaseAdapterDelegatesManager(boolean hasConsistItemType) {
        this.hasConsistItemType = hasConsistItemType;
    }

    public EaseAdapterDelegatesManager addDelegate(EaseAdapterDelegate<?, ?> delegate, String tag) {
        Type superclass = getParameterizedType(delegate.getClass());
        if(superclass instanceof ParameterizedType) {
            Class<?> clazz = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
            String typeWithTag = typeWithTag(clazz, tag);
            int viewType = hasConsistItemType ? delegate.getItemViewType() : delegates.size();
            //save the delegate to the collection
            delegates.put(viewType, (EaseAdapterDelegate<Object, ViewHolder>) delegate);
            // save the index of the delegate to the collection
            dataTypeWithTags.put(viewType, typeWithTag);
        }else {
            // Has no generics.
            throw new IllegalArgumentException(
                    String.format("Please set the correct generic parameters on %s.", delegate.getClass().getName()));
        }
        return this;
    }

    private Type getParameterizedType(Class<?> clazz) {
        if(clazz == null) {
            return null;
        }
        Type superclass = clazz.getGenericSuperclass();
        if(superclass instanceof ParameterizedType) {
            return superclass;
        }else {
            if(clazz.getName().equals("java.lang.Object")) {
                return null;
            }
            return getParameterizedType(clazz.getSuperclass());
        }
    }

    @Nullable
    public EaseAdapterDelegate<Object, ViewHolder> getDelegate(int viewType) {
        EaseAdapterDelegate<Object, ViewHolder> delegate = delegates.get(viewType);
        if(delegate == null) {
            return fallbackDelegate;
        }
        return delegate;
    }

    public List<EaseAdapterDelegate<Object, ViewHolder>> getAllDelegates() {
        List<EaseAdapterDelegate<Object, ViewHolder>> list = new ArrayList<>();
        if(!delegates.isEmpty()) {
            for(int i = 0; i < delegates.size(); i++) {
                list.add(delegates.valueAt(i));
            }
        }
        if(fallbackDelegate != null) {
            list.add(fallbackDelegate);
        }
        return list;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EaseAdapterDelegate<Object, ViewHolder> delegate = getDelegate(viewType);
        if(delegate == null) {
            throw new NullPointerException("No EaseAdapterDelegate added for ViewType "+viewType);
        }
        String tag = getTagByViewType(viewType);
        return delegate.onCreateViewHolder(parent, tag);
    }

    /**
     * 注意：此处获取viewType时，不要直接使用{@link RecyclerView.ViewHolder#getItemViewType()}
     * 使用ConcatAdapter时，返回的viewType不准确。下同
     * @param holder
     * @param position
     * @param item
     */
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, Object item) {
        int viewType = holder.getAdapter().getItemViewType(position);
        EaseAdapterDelegate<Object, ViewHolder> delegate = getDelegate(viewType);
        if(delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + viewType);
        }
        delegate.onBindViewHolder(holder, position, targetItem(item));
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads, Object item) {
        int viewType = holder.getAdapter().getItemViewType(position);
        EaseAdapterDelegate<Object, ViewHolder> delegate = getDelegate(viewType);
        if(delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + viewType);
        }
        delegate.onBindViewHolder(holder, position, payloads, targetItem(item));
    }

    public int getItemViewType(Object item, int position) {
        Class<?> clazz = targetItem(item).getClass();
        String tag = targetTag(item);
        String typeWithTag = typeWithTag(clazz, tag);
        List<Integer> indexList = indexesOfValue(dataTypeWithTags, typeWithTag);
        for (int index : indexList) {
            EaseAdapterDelegate<Object, ViewHolder> delegate = delegates.get(index);
            if(delegate != null && delegate.getTags().contains(tag) && delegate.isForViewType(item, position)) {
                return hasConsistItemType ? delegate.getItemViewType() : index;
            }
        }
        
        if(fallbackDelegate != null && fallbackDelegate.isForViewType(item, position)) {
            int index = 0;
            if(fallbackDelegate.getTags().contains(tag)) {
                index = fallbackDelegate.getTags().indexOf(tag);
            }
            return hasConsistItemType ? fallbackDelegate.getItemViewType() + index : delegates.size() + index;
        }

        throw new NullPointerException("No EaseAdapterDelegate added that matches position = " + position + " item = " + targetItem(item) + " in data source.");
    }

    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        EaseAdapterDelegate<Object, ViewHolder> delegate = getDelegate(holder.getItemViewType());
        if(delegate != null) {
            delegate.onViewRecycled(holder);
        }
    }

    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        EaseAdapterDelegate<Object, ViewHolder> delegate = getDelegate(holder.getItemViewType());
        return delegate != null && delegate.onFailedToRecycleView(holder);
    }

    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        EaseAdapterDelegate<Object, ViewHolder> delegate = getDelegate(holder.getItemViewType());
        if(delegate != null) {
            delegate.onViewAttachedToWindow(holder);
        }
    }

    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        EaseAdapterDelegate<Object, ViewHolder> delegate = getDelegate(holder.getItemViewType());
        if(delegate != null) {
            delegate.onViewDetachedFromWindow(holder);
        }
    }

    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        for(int i = 0; i < delegates.size(); i++) {
            EaseAdapterDelegate<Object, ViewHolder> delegate = delegates.get(i);
            if(delegate != null) {
                delegate.onAttachedToRecyclerView(recyclerView);
            }
        }
    }

    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        for(int i = 0; i < delegates.size(); i++) {
            EaseAdapterDelegate<Object, ViewHolder> delegate = delegates.get(i);
            if(delegate != null) {
                delegate.onDetachedFromRecyclerView(recyclerView);
            }
        }
    }

    public int getDelegateViewType(EaseAdapterDelegate delegate) {
        int index = delegates.indexOfValue(delegate);
        return index > 0 ? delegates.keyAt(index) : -1;
    }

    private String getTagByViewType(int viewType) {
        if(dataTypeWithTags.containsKey(viewType)) {
            String typeWithTag = dataTypeWithTags.get(viewType);
            if(TextUtils.isEmpty(typeWithTag)) {
                return typeWithTag;
            }
            if(!typeWithTag.contains(":")) {
                return typeWithTag;
            }
            return typeWithTag.split(":")[1];
        }else {
            int index = hasConsistItemType ? viewType - fallbackDelegate.getItemViewType() : viewType - delegates.size();
            if(fallbackDelegate.getTags().size() <= index) {
                return null;
            }
            return fallbackDelegate.getTags().get(index);
        }
    }

    private String typeWithTag(Class<?> clazz, String tag) {
        return TextUtils.isEmpty(tag) ? clazz.getName() : clazz.getName() + ":" + tag;
    }

    private Object targetItem(Object item) {
        return item;
    }

    private String targetTag(Object item) {
        if(item instanceof EMMessage) {
            if(!delegates.isEmpty()) {
                boolean isChat = true;
                for(int i = 0; i < delegates.size(); i++) {
                    int key = delegates.indexOfKey(i);
                    EaseAdapterDelegate<Object, ViewHolder> delegate = delegates.get(key);
                    if(!(delegate instanceof EaseMessageAdapterDelegate)) {
                        isChat = false;
                        break;
                    }
                }
                return isChat ? ((EMMessage) item).direct().toString() : EaseAdapterDelegate.DEFAULT_TAG;
            }
            return EaseAdapterDelegate.DEFAULT_TAG;
        }
        return EaseAdapterDelegate.DEFAULT_TAG;
    }

    private List<Integer> indexesOfValue(SparseArrayCompat<String> array, String value) {
        List<Integer> indexes = new ArrayList<>();
        for(int i = 0; i < array.size(); i++) {
            if(TextUtils.equals(array.valueAt(i), value)) {
                indexes.add(array.keyAt(i));
            }
        }
        return indexes;
    }

}
