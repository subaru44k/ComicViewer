package com.appsubaruod.comicviewer.model;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by s-yamada on 2017/04/10.
 */

public class FileOperator {
    private Context mContext;
    private File mFileDir;

    private static final int BUFFER = 512;
    private static final int TOOBIG = 0x6400000; // maximum file size : 100MB
    private static final int TOOMANY = 10000;     // maximum file entries

    public FileOperator(Context context) {
        mContext = context;
        mFileDir = mContext.getFilesDir();
    }

    private Set<OnFileCopy> mCallbackSet = new HashSet<>();

    public void registerCallback(OnFileCopy mCallback) {
        mCallbackSet.add(mCallback);
    }

    public void unRegisterCallback(OnFileCopy mCallback) {
        mCallbackSet.remove(mCallback);
    }

    public void unpackZip(Uri uri) throws IllegalFormatException {

        InputStream is;
        ZipInputStream zis = null;
        int entries = 0;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry entry;
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
                notifyCopiedSingleFile(entries, outFile, total);

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
            notifyCopyCompleted(entries);
        }

    }

    public void copyImageFiles(Uri uri) throws IllegalFormatException {

    }

    private void notifyCopiedSingleFile(int fileNumber, File outFile, int unpackedBytes) {
        for (OnFileCopy item : mCallbackSet) {
            item.onCopiedSingleFile(fileNumber, outFile, unpackedBytes);
        }
    }

    private void notifyCopyCompleted(int maxPage) {
        for (OnFileCopy item : mCallbackSet) {
            item.onCopyCompleted(maxPage);
        }
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

    protected interface OnFileCopy {
        void onCopiedSingleFile(int fileCount, File copiedFile, int unpackedBytes);
        void onCopyCompleted(int maxFileCount);
    }

}
