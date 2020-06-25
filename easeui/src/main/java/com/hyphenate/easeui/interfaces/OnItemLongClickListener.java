package com.hyphenate.easeui.interfaces;

import android.view.View;

/**
 * 条目长按点击事件
 */
public interface OnItemLongClickListener {
    /**
     * 条目点击
     * @param view
     * @param position
     */
    boolean onItemLongClick(View view, int position);
}
