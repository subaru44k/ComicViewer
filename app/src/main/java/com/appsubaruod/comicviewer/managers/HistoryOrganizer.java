package com.appsubaruod.comicviewer.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by s-yamada on 2017/04/15.
 */

public class HistoryOrganizer {
    private final List<String> mHistories = new ArrayList<>();

    public void add(String dirName) {
        mHistories.add(dirName);
    }

    public void remove(String dirName) {
        mHistories.remove(dirName);
    }

    public List<String> getHistories() {
        return mHistories;
    }

    public void clear() {
        mHistories.clear();
    }
}
