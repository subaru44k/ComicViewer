package com.appsubaruod.comicviewer.utils.messages;

/**
 * Created by s-yamada on 2017/04/04.
 */

public class LoadCompleteEvent {
    private int mMaxPage;

    public LoadCompleteEvent(int maxPage) {
        mMaxPage = maxPage;
    }

    public int getMaxPage() {
        return mMaxPage;
    }
}
