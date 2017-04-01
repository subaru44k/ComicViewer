package com.appsubaruod.comicviewer.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.appsubaruod.comicviewer.utils.messages.SetImageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by s-yamada on 2017/03/28.
 */

public class ComicModel {
    private final String LOG_TAG = "ComicModel";
    private Context mContext;
    private final Executor mWorkerThread = Executors.newSingleThreadExecutor();

    private static final int BUFFER = 512;
    private static final int TOOBIG = 0x6400000; // maximum file size : 100MB
    private static final int TOOMANY = 10000;     // maximum file entries

    public ComicModel(Context context) {
        mContext = context;
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
                unpackZip(mContext.getFilesDir(), uri);
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

    private void unpackZip(File fileDir, Uri zipUri) {
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
                File outFile = new File(fileDir + name);
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
                if (entries == 1) {
                    FileInputStream fis = new FileInputStream(outFile);
                    Bitmap bm = BitmapFactory.decodeStream(fis);
                    EventBus.getDefault().post(new SetImageEvent(bm));
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
}
