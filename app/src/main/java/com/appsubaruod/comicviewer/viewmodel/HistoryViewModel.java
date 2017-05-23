package com.appsubaruod.comicviewer.viewmodel;

import android.support.v7.widget.RecyclerView;

import com.appsubaruod.comicviewer.model.HistoryModel;
import com.appsubaruod.comicviewer.utils.messages.HistoryChangedEvent;
import com.appsubaruod.comicviewer.views.HistoryItemAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by s-yamada on 2017/05/02.
 */

public class HistoryViewModel {
    private List<HistoryItemViewModel> mHistories;
    private HistoryModel mHistoryModel;

    public HistoryViewModel(RecyclerView recyclerView) {
        EventBus.getDefault().register(this);

        mHistories = new ArrayList<>();
        HistoryItemAdapter adapter = new HistoryItemAdapter(mHistories);

        mHistoryModel = new HistoryModel();
        mHistoryModel.requestHistories();

        recyclerView.setAdapter(adapter);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onHistoryChanged(HistoryChangedEvent event) {
        mHistories.clear();
        mHistories.addAll(mHistoryModel.getHistories());
    }
}
