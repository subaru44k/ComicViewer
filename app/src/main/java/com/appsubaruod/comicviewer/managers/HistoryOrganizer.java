package com.appsubaruod.comicviewer.managers;

import android.util.Log;

import com.appsubaruod.comicviewer.utils.messages.HistoryChangedEvent;
import com.appsubaruod.comicviewer.viewmodel.HistoryItemViewModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by s-yamada on 2017/04/15.
 */

public class HistoryOrganizer {
    private static final String LOG_TAG = HistoryOrganizer.class.getName();

    private int mIndex = 0;
    private Map<String, Integer> mDirnameTable = new HashMap<>();
    private final List<HistoryItemViewModel> mHistories = new ArrayList<>();

    public void addOrReflesh(HistoryItemViewModel history) {
        Integer index = mDirnameTable.get(history.getTitle());
        if (index == null) {
            Log.d(LOG_TAG, "history added: " + history.getTitle());
            mHistories.add(mIndex, history);
            mDirnameTable.put(history.getTitle(), mIndex);
            mIndex++;
        } else {
            Log.d(LOG_TAG, "history refreshed: " + history.getTitle());
            HistoryItemViewModel model = mHistories.get(index);
            Log.d(LOG_TAG, model.getTitle() + " will be refreshed");
            mHistories.remove(model);
            mHistories.add(index, history);
        }
        notifyHistoryChangedEvent();
    }

    private void notifyHistoryChangedEvent() {
        EventBus.getDefault().post(new HistoryChangedEvent());
    }

    public void remove(HistoryItemViewModel history) {
        mHistories.remove(history);
        notifyHistoryChangedEvent();
    }

    public List<HistoryItemViewModel> getHistories() {
        return mHistories;
    }

    public void clear() {
        mHistories.clear();
        notifyHistoryChangedEvent();
    }
}
