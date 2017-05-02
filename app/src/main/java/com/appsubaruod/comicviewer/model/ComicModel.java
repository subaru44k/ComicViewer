package com.appsubaruod.comicviewer.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.appsubaruod.comicviewer.managers.HistoryOrganizer;
import com.appsubaruod.comicviewer.model.file.FileOrganizer;
import com.appsubaruod.comicviewer.utils.messages.BookOpenedEvent;
import com.appsubaruod.comicviewer.utils.messages.HistoryChangedEvent;
import com.appsubaruod.comicviewer.utils.messages.HistoryViewEvent;
import com.appsubaruod.comicviewer.utils.messages.LoadCompleteEvent;
import com.appsubaruod.comicviewer.utils.messages.ReadComicEvent;
import com.appsubaruod.comicviewer.utils.messages.SelectPageEvent;
import com.appsubaruod.comicviewer.utils.messages.SetImageEvent;
import com.appsubaruod.comicviewer.viewmodel.HistoryItemViewModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.appsubaruod.comicviewer.utils.ImageOperator.isImageFile;

/**
 * Created by s-yamada on 2017/03/28.
 */
public class ComicModel {
    public static final int MAX_PAGE_WITHOUT_BLOCKING = 20;
    public static final String EXTENSION_NAME_ZIP = ".zip";
    public static final int INIT_PAGE_SIZE = 0;
    private static ComicModel mComicModelInstance;
    private int mPageIndex = INIT_PAGE_SIZE;
    private int mMaxPageIndex = INIT_PAGE_SIZE;
    private Map<Integer, File> mFileMap = new HashMap<>();

    private final String LOG_TAG = "ComicModel";
    private final Executor mWorkerThread = Executors.newSingleThreadExecutor();

    private Context mContext;
    private FileOrganizer mFileOrganizer;
    private HistoryOrganizer mHistoryOrganizer;

    private String mTitleName;

    private FileOrganizer.FileResolve mFileResolve = new FileOrganizer.FileResolve() {
        @Override
        public void onSingleFileResolved(String dirName, int fileCount, File resolvedFile, int sizeBytes) {
            storeFileList(fileCount, resolvedFile);
            setMaxPageIndex(fileCount);

            if (fileCount == 1) {
                mPageIndex = 1;
                mTitleName = dirName;
                // open ReadFragment
                requestReadComicView();
                // call postSticky, so as not to drop sending event during fragment transition
                EventBus.getDefault().postSticky(new SetImageEvent(mPageIndex, obtainFile(mPageIndex)));
                mHistoryOrganizer.addOrReflesh(new HistoryItemViewModel(mTitleName, resolvedFile));
                // notify book is opened
                EventBus.getDefault().postSticky(new BookOpenedEvent());
            }
        }

        @Override
        public void onAllFileResolved(String dirName, int maxFileCount) {
            // Send notification including maxpage info
            EventBus.getDefault().post(new LoadCompleteEvent(maxFileCount));
        }
    };

    private ComicModel(Context context) {
        mContext = context;
        mFileOrganizer = new FileOrganizer(mContext);
        mFileOrganizer.registerCallback(mFileResolve);
        mHistoryOrganizer = new HistoryOrganizer();
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

                requestContent(uri);
            }
        });
    }

    private void requestContent(Uri uri) {
        initialize();
        String contentPath = mFileOrganizer.getPath(uri);
        if (contentPath == null) {
            Log.d(LOG_TAG, "Unsupported uri. Maybe network storage: " + uri.toString());
            Log.d(LOG_TAG, "try to open");
            try {
                mFileOrganizer.requestNetworkContent(uri);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return;
            }
        }
        requestLocalContent(uri, contentPath);
    }

    private void requestLocalContent(Uri uri, String contentPath) {
        String contentLowerPath = contentPath.toLowerCase();
        Log.d(LOG_TAG, "lowerPath : " + contentLowerPath);
        if (contentLowerPath.contains(EXTENSION_NAME_ZIP)) {
            // maybe zip file
            uri = getUserFriendlyZipUri(uri);
            mFileOrganizer.requestLocalZipContent(uri);
        } else if (isImageFile(contentLowerPath)) {
            // image file
            Log.d(LOG_TAG, contentLowerPath);
            mFileOrganizer.requestLocalImageContent(uri);
        }
    }

    /**
     * Calculates the user friendly uri and returns it.
     * E.g. file://aaa.zip/hoge.png is transferred into file://aaa.zip.
     * FIXME uri containing .zip as file name (not extension) may cause problem.
     * E.g. file://hoge.zipfile/hoge.png
     * @param uri userSelected Uri which can be undesired.
     * @return Seemingly desired uri
     */
    private Uri getUserFriendlyZipUri(Uri uri) {
        if (!uri.getPath().endsWith(EXTENSION_NAME_ZIP)) {
            Log.d(LOG_TAG, uri.toString());

            // build new uri
            Uri.Builder builder = uri.buildUpon();
            String encodedPath = uri.getEncodedPath();
            int desiredEnd = encodedPath.lastIndexOf(EXTENSION_NAME_ZIP) + EXTENSION_NAME_ZIP.length();
            builder.encodedPath(encodedPath.substring(0, desiredEnd));
            return builder.build();
        }

        return uri;
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
                mHistoryOrganizer.addOrReflesh(new HistoryItemViewModel(mTitleName, file));
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
                mHistoryOrganizer.addOrReflesh(new HistoryItemViewModel(mTitleName, file));
                if (storePage) {
                    mPageIndex = pageIndex;
                }
            }
        });
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

    public void requestReadComicView() {
        EventBus.getDefault().post(new ReadComicEvent());
    }

    public boolean requestSelectPageView() {
        if (getMaxPageIndex() != INIT_PAGE_SIZE) {
            EventBus.getDefault().post(new SelectPageEvent(getPageIndex(), getMaxPageIndex()));
            return true;
        }
        return false;
    }

    public boolean requestHistoryView() {
        EventBus.getDefault().post(new HistoryViewEvent());
        return true;
    }

    public List<HistoryItemViewModel> getHistories() {
        return mHistoryOrganizer.getHistories();
    }
}
