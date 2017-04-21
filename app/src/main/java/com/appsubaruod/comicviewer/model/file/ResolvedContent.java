package com.appsubaruod.comicviewer.model.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by s-yamada on 2017/04/16.
 */

public class ResolvedContent {
    private List<File> mFileList = new ArrayList<>();
    private Map<File, Integer> mIndexMap = new HashMap<>();
    private Map<File, Long> mSizeMap = new HashMap<>();

    public void store(File file, int index, long size) {
        mFileList.add(file);
        mIndexMap.put(file, index);
        mSizeMap.put(file, size);
    }

    public List<File> getFiles() {
        return mFileList;
    }

    public int getContentIndex(File file) {
        return mIndexMap.get(file);
    }

    public int getContentSize(File file) {
        // FIXME Is this appropriate?
        return (int) ((long) mSizeMap.get(file));
    }

    public int fileCount() {
        return mFileList.size();
    }
}
