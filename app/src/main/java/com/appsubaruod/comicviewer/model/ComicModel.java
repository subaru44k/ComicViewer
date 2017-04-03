package com.appsubaruod.comicviewer.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.appsubaruod.comicviewer.utils.messages.SetImageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private final Context mContext;
    private final File mFileDir;
    private final Executor mWorkerThread = Executors.newSingleThreadExecutor();

    private static final int BUFFER = 512;
    private static final int TOOBIG = 0x6400000; // maximum file size : 100MB
    private static final int TOOMANY = 10000;     // maximum file entries

    private ComicModel(Context context) {
        mContext = context;
        mFileDir = mContext.getFilesDir();
    }

    public static ComicModel getInstance(Context mContext) {
        if (mComicModelInstance == null) {
            mComicModelInstance = new ComicModel(mContext);
        }
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
                unpackZip(uri);
            }
        });
    }

    public void readNextPage() {
        readSpecifiedPage(mPageIndex + 1);
    }

    public void readSpecifiedPage(final int pageIndex) {
        // If page index is small, try to load without blocking
        // If fails to load, execute again after extraction is finished
        final File file = obtainFile(pageIndex);
        if (pageIndex < MAX_PAGE_WITHOUT_BLOCKING) {
            if (file != null) {
                EventBus.getDefault().post(new SetImageEvent(pageIndex, file));
                mPageIndex = pageIndex;
                return;
            }
        }
        mWorkerThread.execute(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new SetImageEvent(pageIndex, file));
                mPageIndex = pageIndex;
            }
        });
    }

    private String validateFilename(String filename, String intendedDir) throws IOException {
        File f = new File(filename);
        String canonicalPath = f.getCanonicalPath();

        File iD = new File(intendedDir);
        String canonicalID = iD.getCanonicalPath();

        if (canonicalPath.startsWith(canonicalID)) {
            return canonicalPath;
        } else {
            throw new IllegalStateException("File is outside extraction target directory.");
        }
    }

    private void unpackZip(Uri zipUri) {

        initialize();

        InputStream is;
        ZipInputStream zis = null;
        try {
            is = mContext.getContentResolver().openInputStream(zipUri);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry entry;
            int entries = 0;
            int total = 0;

            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);
                int count;
                byte data[] = new byte[BUFFER];
                // check if file name is valid and size is adequate
                String name = validateFilename(entry.getName(), ".");
                File outFile = new File(mFileDir + name);
                // create parent dirs
                outFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                while (total <= TOOBIG && (count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                    total += count;
                }
                dest.flush();
                dest.close();
                zis.closeEntry();

                entries++;
                storeFileList(entries, outFile);
                setMaxPageIndex(entries);

                if (entries == 1) {
                    mPageIndex = 1;
                    EventBus.getDefault().post(new SetImageEvent(mPageIndex, obtainFile(mPageIndex)));
                }
                if (entries > TOOMANY) {
                    throw new IllegalStateException("Too many files to unzip.");
                }
                if (total > TOOBIG) {
                    throw new IllegalStateException("File being unzipped is too big.");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
