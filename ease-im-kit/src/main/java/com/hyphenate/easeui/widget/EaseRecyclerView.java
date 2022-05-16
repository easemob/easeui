package com.hyphenate.easeui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 可以添加header和footer
 * 参考：https://github.com/idisfkj/EnhanceRecyclerView
 */
public class EaseRecyclerView extends RecyclerView {
    private static final int BASE_HEADER_VIEW_TYPE = -1 << 10;
    private static final int BASE_FOOTER_VIEW_TYPE = -1 << 11;
    private RecyclerViewContextMenuInfo mContextMenuInfo;
    private List<FixedViewInfo> mHeaderViewInfos = new ArrayList<>();
    private List<FixedViewInfo> mFooterViewInfos = new ArrayList<>();
    private Adapter mAdapter;
    private boolean isStaggered;
    private boolean isShouldSpan;

    public EaseRecyclerView(@NonNull Context context) {
        super(context);
    }

    public EaseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EaseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // TODO: 2020/1/11 添加移除头布局的方法
    /**
     * 如果view的初始化中的parent用的是recyclerView, 该方法的调用应该放在setLayoutManager之后,
     * 否则需要自己对view添加LayoutParams
     * @param view
     */
    public void addHeaderView(View view) {
        FixedViewInfo info = new FixedViewInfo();
        info.view = view;
        info.viewType = BASE_HEADER_VIEW_TYPE + mHeaderViewInfos.size();
        mHeaderViewInfos.add(info);

        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 移除所有的头布局
     */
    public void removeHeaderViews() {
        if(mHeaderViewInfos != null) {
            mHeaderViewInfos.clear();
        }
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void addFooterView(View view) {
        FixedViewInfo info = new FixedViewInfo();
        info.view = view;
        info.viewType = BASE_FOOTER_VIEW_TYPE + mFooterViewInfos.size();
        mFooterViewInfos.add(info);

        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        if(!(adapter instanceof WrapperRecyclerViewAdapter)) {
            if(adapter != null) {
                adapter.registerAdapterDataObserver(mObserver);
            }
            mAdapter = new WrapperRecyclerViewAdapter(adapter);
            if(adapter != null) {
                mAdapter.setHasStableIds(adapter.hasStableIds());
            }
        }
        super.setAdapter(mAdapter);
        if(isShouldSpan) {
            ((WrapperRecyclerViewAdapter)mAdapter).adjustSpanSize(this);
        }
    }

    @Override
    public void setLayoutManager(@Nullable LayoutManager layout) {
        if(layout instanceof GridLayoutManager || layout instanceof  StaggeredGridLayoutManager) {
            isShouldSpan = true;
        }
        super.setLayoutManager(layout);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        int longPressPosition = getChildBindingAdapterPosition(originalView);
        if(longPressPosition >= 0) {
            long longPressId = getAdapter().getItemId(longPressPosition);
            mContextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public int getChildBindingAdapterPosition(@NonNull View child) {
        final RecyclerView.ViewHolder holder = getChildViewHolderInt(child);
        return holder != null ? holder.getBindingAdapterPosition() : NO_POSITION;
    }

    RecyclerView.ViewHolder getChildViewHolderInt(View child) {
        if (child == null) {
            return null;
        }
        return getChildViewHolder(child);
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(@NonNull View child) {
        return super.getChildViewHolder(child);
    }

    public int getFootersCount() {
        return mFooterViewInfos.size();
    }

    public int getHeadersCount() {
        return mHeaderViewInfos.size();
    }

    public class FixedViewInfo {
        public View view;
        public int viewType;
    }

    public class WrapperRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Adapter mAdapter;

        public WrapperRecyclerViewAdapter(Adapter adapter) {
            this.mAdapter = adapter;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType >= BASE_HEADER_VIEW_TYPE && viewType < BASE_HEADER_VIEW_TYPE + getHeadersCount()) {
                View view = mHeaderViewInfos.get(viewType - BASE_HEADER_VIEW_TYPE).view;
                return viewHolder(view);
            }else if(viewType >= BASE_FOOTER_VIEW_TYPE && viewType < BASE_FOOTER_VIEW_TYPE + getFootersCount()) {
                View view = mFooterViewInfos.get(viewType - BASE_FOOTER_VIEW_TYPE).view;
                return viewHolder(view);
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(isHeaderOrFooter(holder)) {
                return;
            }
            position -= getHeadersCount();
            mAdapter.onBindViewHolder(holder, position);
        }

        @Override
        public int findRelativeAdapterPositionIn(@NonNull Adapter<? extends RecyclerView.ViewHolder> adapter,
                                                 @NonNull RecyclerView.ViewHolder viewHolder, int localPosition) {
            if(adapter == this) {
                return localPosition;
            }else {
                if(mAdapter instanceof ConcatAdapter) {
                    List<? extends Adapter<? extends RecyclerView.ViewHolder>> adapters = ((ConcatAdapter) mAdapter).getAdapters();
                    int prePosition = 0;
                    for(int i = 0; i < adapters.size(); i++) {
                        Adapter<? extends RecyclerView.ViewHolder> curAdapter = adapters.get(i);
                        if(curAdapter == adapter) {
                            return localPosition - prePosition;
                        }else {
                            prePosition += curAdapter.getItemCount();
                        }
                    }
                    return NO_POSITION;
                }
            }
            return super.findRelativeAdapterPositionIn(adapter, viewHolder, localPosition);
        }

        @Override
        public long getItemId(int position) {
            if(isHeaderOrFooter(position)) {
                return -1;
            }
            position -= getHeadersCount();
            return mAdapter.getItemId(position);
        }

        @Override
        public int getItemCount() {
            return getHeadersCount() + getFootersCount() + getContentCount();
        }

        @Override
        public int getItemViewType(int position) {
            if(isHeader(position)) {
                return mHeaderViewInfos.get(position).viewType;
            }
            if(isFooter(position)) {
                return mFooterViewInfos.get(position - getHeadersCount() - getContentCount()).viewType;
            }
            return mAdapter.getItemViewType(position - getHeadersCount());
        }

        public int getContentCount() {
            return mAdapter == null ? 0 : mAdapter.getItemCount();
        }

        public boolean isHeader(int position) {
            return position < getHeadersCount();
        }

        public boolean isFooter(int position) {
            return position >= getHeadersCount() + getContentCount();
        }

        public boolean isHeaderOrFooter(RecyclerView.ViewHolder holder) {
            return holder instanceof ViewHolder || isHeaderOrFooter(holder.getAdapterPosition());
        }

        public boolean isHeaderOrFooter(int position) {
            return isHeader(position) || isFooter(position);
        }

        public void adjustSpanSize(RecyclerView recyclerView) {
            if(recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int headersCount = getHeadersCount();
                        int adjPosition = position - headersCount;
                        if(position < headersCount || adjPosition >= mAdapter.getItemCount()) {
                            return manager.getSpanCount();
                        }
                        return 1;
                    }
                });
            }

            if(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                isStaggered = true;
            }
        }

        public Adapter getAdapter() {
            return mAdapter;
        }
    }

    private RecyclerView.ViewHolder viewHolder(View itemView) {
        if(isStaggered) {
            StaggeredGridLayoutManager.LayoutParams params =
                    new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setFullSpan(true);
            itemView.setLayoutParams(params);
        }
        return new ViewHolder(itemView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private AdapterDataObserver mObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            positionStart += getHeadersCount();
            mAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            positionStart += getHeadersCount();
            mAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            positionStart += getHeadersCount();
            mAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            positionStart += getHeadersCount();
            mAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            fromPosition += getHeadersCount();
            toPosition += getHeadersCount();
            mAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

//=====================解决添加条目快捷菜单的问题======================================
    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {
        public int position;
        public long id;

        public RecyclerViewContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }
    }
}
