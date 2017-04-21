package com.appsubaruod.comicviewer.model.file;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.appsubaruod.comicviewer.utils.ImageOperator.isImageFile;

/**
 * Created by s-yamada on 2017/04/10.
 */

public class FileOperator {
    private Context mContext;

    private static final String LOG_TAG = FileOperator.class.getName();

    private static final int BUFFER = 512;
    private static final int TOOBIG = 0x6400000; // maximum file size : 100MB
    private static final int TOOMANY = 10000;     // maximum file entries

    public FileOperator(Context context) {
        mContext = context;
    }

    private Set<OnFileCopy> mCallbackSet = new HashSet<>();

    public void registerCallback(OnFileCopy mCallback) {
        mCallbackSet.add(mCallback);
    }

    public void unregisterCallback(OnFileCopy mCallback) {
        mCallbackSet.remove(mCallback);
    }

    public ResolvedContent unpackZip(File outDirFile, Uri uri) {
        InputStream is;
        ZipInputStream zis = null;
        ResolvedContent content = new ResolvedContent();
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
                String outPath = outDirFile + File.separator + name;
                File outFile = new File(outPath);
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
                content.store(outFile, entries, total);

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            notifyCopyCompleted(entries);
            return content;
        }
    }

    public void copyImageFiles(File outDir, File imageDir) throws IllegalFormatException {
        try {
            int entries = 0;
            for (File eachFile : imageDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (isImageFile(pathname.toString())) {
                        return true;
                    }
                    return false;
                }
            })) {
                Log.d(LOG_TAG, eachFile.getAbsolutePath());
                File outFile = new File(outDir + eachFile.getName());
                copyImageFile(eachFile, outFile, entries++);
            }
            notifyCopyCompleted(entries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyImageFile(File inFile, File outFile, int entry) throws IOException {
        FileChannel inChannel = new FileInputStream(inFile).getChannel();
        FileChannel outChannel = new FileOutputStream(outFile).getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);

        notifyCopiedSingleFile(entry, outFile, (int) inChannel.size());
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

    public List<File> listFilesAndFilesSubDirectories(String directoryName){
        List<File> fileList = new ArrayList();
        File directory = new File(directoryName);
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
            if (file.isFile()){
                fileList.add(file);
            } else if (file.isDirectory()){
                fileList.addAll(listFilesAndFilesSubDirectories(file.getAbsolutePath()));
            }
        }

        return fileList;
    }

    public List<File> getFileList(File targetDir) throws FileNotFoundException {
        if (!targetDir.exists()) {
            throw new FileNotFoundException("cannot find directory : " + targetDir.toString());
        }

        return listFilesAndFilesSubDirectories(targetDir.getAbsolutePath());
    }

    interface OnFileCopy {
        void onCopiedSingleFile(int fileCount, File copiedFile, int unpackedBytes);
        void onCopyCompleted(int maxFileCount);
    }

}
