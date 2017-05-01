package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.appsubaruod.comicviewer.BR;

/**
 * Created by s-yamada on 2017/05/01.
 */

public class HistoryItemViewModel extends BaseObservable {
    private String mTitle;

    public HistoryItemViewModel(String title) {
        mTitle = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getTitle() {
        return mTitle;
    }
}
