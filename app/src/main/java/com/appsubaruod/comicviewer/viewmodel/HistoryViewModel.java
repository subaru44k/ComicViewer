package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.model.HistoryModel;

import java.util.List;

/**
 * Created by s-yamada on 2017/05/01.
 */

public class HistoryViewModel extends BaseObservable {
    private HistoryModel mHistoryModel;
    private List<String> mHistories;

    public HistoryViewModel() {
        mHistoryModel = new HistoryModel();
        mHistories = mHistoryModel.getHistories();
    }

    @Bindable
    public List<String> getHistories() {
        return mHistories;
    }

    @Bindable
    public String getHistory() {
        return mHistories.get(0);
    }

    public void setHistories(List<String> histories) {
        mHistories = histories;
        notifyPropertyChanged(BR.histories);
    }

}
