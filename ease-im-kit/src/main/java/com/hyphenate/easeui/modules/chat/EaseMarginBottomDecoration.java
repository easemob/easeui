package com.hyphenate.easeui.modules.chat;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeui.utils.EaseCommonUtils;

public class EaseMarginBottomDecoration extends RecyclerView.ItemDecoration {
    private int bottomMargin = 0;

    public EaseMarginBottomDecoration() {

    }

    public EaseMarginBottomDecoration(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // the last item
        if(parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            if(bottomMargin <= 0) {
                bottomMargin = (int) EaseCommonUtils.dip2px(parent.getContext(), 15);
            }
            outRect.bottom = bottomMargin;
        }
    }
}
