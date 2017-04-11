package com.appsubaruod.comicviewer.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.appsubaruod.comicviewer.utils.messages.BookOpenedEvent;
import com.appsubaruod.comicviewer.utils.messages.LoadCompleteEvent;
import com.appsubaruod.comicviewer.utils.messages.SetImageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by s-yamada on 2017/03/28.
 */
public class ComicModel {
    public static final int MAX_PAGE_WITHOUT_BLOCKING = 20;
    private static ComicModel mComicModelInstance;
    private int mPageIndex = 0;
    private int mMaxPageIndex = 0;
    private Map<Integer, File> mFileMap = new HashMap<>();

    private final String LOG_TAG = "ComicModel";
    private final Executor mWorkerThread = Executors.newSingleThreadExecutor();

    private FileOperator mFileOperator;

    private FileOperator.OnFileCopy mOnFileCopy = new FileOperator.OnFileCopy() {
        @Override
        public void onCopiedSingleFile(int fileCount, File outFile, int unpackedBytes) {
            storeFileList(fileCount, outFile);
            setMaxPageIndex(fileCount);

            if (fileCount == 1) {
                mPageIndex = 1;
                // call postSticky, so as not to drop sending event during fragment transition
                EventBus.getDefault().postSticky(new SetImageEvent(mPageIndex, obtainFile(mPageIndex)));
                // notify book is opened
                EventBus.getDefault().postSticky(new BookOpenedEvent());
            }
        }

        @Override
        public void onCopyCompleted(int maxFileCount) {
            // Send notification including maxpage info
            EventBus.getDefault().post(new LoadCompleteEvent(maxFileCount));
        }
    };

    private ComicModel(Context context) {
        mFileOperator = new FileOperator(context);
        mFileOperator.registerCallback(mOnFileCopy);
    }

    public static ComicModel getInstance(Context mContext) {
        if (mComicModelInstance == null) {
            mComicModelInstance = new ComicModel(mContext);
        }
        return mComicModelInstance;
    }

    public static ComicModel getInstanceIfCreated() {
        return mComicModelInstance;
    }

    /**
     * Reads comic and extract appropriately.
     * @param uri
     */
    public void readComic(final Uri uri) {
        mWorkerThread.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, uri.toString());

                copyToAppStorage(uri);
            }
        });
    }

    private void copyToAppStorage(Uri uri) {
        initialize();
        if (uri.getPath().contains(".zip")) {
            // maybe zip file
            mFileOperator.unpackZip(uri);
        } else if (uri.getPath().endsWith(".png")
                || uri.getPath().endsWith(".jpg")
                || uri.getPath().endsWith(".jpeg")
                || uri.getPath().endsWith(".gif")) {
            // image file
            mFileOperator.copyImageFiles(uri);
        }
    }

    public void readNextPage() {
        readSpecifiedPage(mPageIndex + 1);
    }

    public void readPreviousPage() {
        readSpecifiedPage(mPageIndex - 1);
    }

    /**
     * Requests to send file and set page index in the model
     * @param pageIndex Request page index
     */
    public void readSpecifiedPage(int pageIndex) {
        requestSpecifiedPage(pageIndex, true);
    }

    /**
     * Reqests to send file without setting page index
     * @param pageIndex Request page index
     */
    public void requestSpecifiedPage(int pageIndex) {
        requestSpecifiedPage(pageIndex, false);
    }

    private void requestSpecifiedPage(final int pageIndex, final boolean storePage) {
        if (pageIndex <= 0 || pageIndex > mMaxPageIndex) {
            Log.d(LOG_TAG, "Specified page out of index. Ignore : " + pageIndex);
            return;
        }
        // If page index is small, try to load without blocking
        // If fails to load, execute again after extraction is finished
        final File file = obtainFile(pageIndex);
        if (pageIndex < MAX_PAGE_WITHOUT_BLOCKING) {
            if (file != null) {
                // call postSticky, so as not to drop sending event during fragment translation
                EventBus.getDefault().postSticky(new SetImageEvent(pageIndex, file));
                if (storePage) {
                    mPageIndex = pageIndex;
                }
                return;
            }
        }
        mWorkerThread.execute(new Runnable() {
            @Override
            public void run() {
                // call postSticky, so as not to drop sending event during fragment translation
                EventBus.getDefault().postSticky(new SetImageEvent(pageIndex, file));
                if (storePage) {
                    mPageIndex = pageIndex;
                }
            }
        });
    }



    private void unpackZip(Uri zipUri) {
    }

    private void initialize() {
        mFileMap = new HashMap<>();
        mPageIndex = 0;
        mMaxPageIndex = 0;
    }

    /**
     * Get the index that currently showing
     * @return
     */
    public int getPageIndex() {
        return mPageIndex;
    }

    public int getMaxPageIndex() {
        return mMaxPageIndex;
    }

    private void setMaxPageIndex(int index) {
        mMaxPageIndex = index;
    }

    private void storeFileList(int entries, File outFile) {
        mFileMap.put(entries, outFile);
    }

    private File obtainFile(int page) {
        return mFileMap.get(page);
    }
}
