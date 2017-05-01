package com.appsubaruod.comicviewer.model;

import java.util.List;

/**
 * Created by s-yamada on 2017/05/01.
 */
public class HistoryModel {
    private ComicModel mComicModel;
    private List<String> mHistories;

    public HistoryModel() {
        mComicModel = ComicModel.getInstanceIfCreated();
    }

    public List<String> getHistories() {
        return mComicModel.getHistories();
    }
}
