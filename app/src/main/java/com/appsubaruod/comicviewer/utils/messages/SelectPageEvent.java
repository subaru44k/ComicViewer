package com.appsubaruod.comicviewer.utils.messages;

/**
 * Created by s-yamada on 2017/04/02.
 */
public class SelectPageEvent {
    private int mCurrentPage;
    private int mMaxPage;

    public SelectPageEvent(int currentPage, int maxPage) {
        mCurrentPage = currentPage;
        mMaxPage = maxPage;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getMaxPage() {
        return mMaxPage;
    }
}
