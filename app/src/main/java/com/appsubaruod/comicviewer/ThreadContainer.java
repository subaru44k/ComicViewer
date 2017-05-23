package com.appsubaruod.comicviewer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by s-yamada on 2017/05/23.
 */

public class ThreadContainer {
    private static ExecutorService mSingleThreadService = null;

    private ThreadContainer() {
    }

    public static void invokeSingleThreadTask(Runnable task) {
        if (mSingleThreadService == null) {
            mSingleThreadService = Executors.newSingleThreadExecutor();
        }
        mSingleThreadService.submit(task);
    }

}
