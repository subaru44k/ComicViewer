package com.appsubaruod.comicviewer.model;

import com.appsubaruod.comicviewer.utils.messages.HistoryChangedEvent;
import com.appsubaruod.comicviewer.viewmodel.HistoryItemViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by s-yamada on 2017/05/01.
 */
public class HistoryModel {
    private ComicModel mComicModel;

    public HistoryModel() {
        mComicModel = ComicModel.getInstanceIfCreated();
    }

    public List<HistoryItemViewModel> getHistories() {
        return mComicModel.getHistories();
    }

    public int getTheNumberOfHistories() {
        return getHistories().size();
    }

    public void requestHistories() {
        EventBus.getDefault().post(new HistoryChangedEvent());
    }
}
