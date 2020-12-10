package com.hyphenate.easeui.adapter;

import androidx.recyclerview.widget.RecyclerView;

public abstract class EaseBaseAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * Get the data item associated with the specified position in the data set.
     * @param position
     * @return
     */
    public abstract Object getItem(int position);
}
