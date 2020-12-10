package com.hyphenate.easeui.widget.chatextend;

/**
 * 参考博文：https://blog.csdn.net/Y_sunny_U/article/details/89500464
 */
public interface PageDecorationLastJudge {
    /**
     * Is the last row in one page
     *
     * @param position
     * @return
     */
    boolean isLastRow(int position);
 
    /**
     * Is the last Colum in one row;
     *
     * @param position
     * @return
     */
    boolean isLastColumn(int position);
 
    boolean isPageLast(int position);
}