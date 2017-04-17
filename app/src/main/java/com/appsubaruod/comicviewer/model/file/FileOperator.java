package com.appsubaruod.comicviewer.model.file;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import java.util.Arrays;
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
    private File mFileDir;

    private static final String LOG_TAG = FileOperator.class.getName();

    private static final int BUFFER = 512;
    private static final int TOOBIG = 0x6400000; // maximum file size : 100MB
    private static final int TOOMANY = 10000;     // maximum file entries

    public FileOperator(Context context, File fileDir) {
        mContext = context;
        mFileDir = fileDir;
    }

    private Set<OnFileCopy> mCallbackSet = new HashSet<>();

    public void registerCallback(OnFileCopy mCallback) {
        mCallbackSet.add(mCallback);
    }

    public void unregisterCallback(OnFileCopy mCallback) {
        mCallbackSet.remove(mCallback);
    }

    public void unpackZip(Uri uri) throws IllegalFormatException {
        String dirnameToCreate = getStoringDirString(uri);
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
                String outPath = getStoredDir(dirnameToCreate) + File.separator + name;
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

    public String getStoringDirString(Uri uri) {
        return uri.getPath();
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     * @author paulburke
     */
    public String getPath(final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(mContext, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void copyImageFiles(Uri uri) throws IllegalFormatException {
        File file = new File(getPath(uri));
        try {
            int entries = 0;
            for (File eachFile : file.getParentFile().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (isImageFile(pathname.toString())) {
                        return true;
                    }
                    return false;
                }
            })) {
                Log.d(LOG_TAG, eachFile.getAbsolutePath());
                File outFile = new File(mFileDir + eachFile.getName());
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

    public List<File> getFileList(String fileName) throws FileNotFoundException {
        File targetDir = getStoredDir(fileName);

        if (!targetDir.exists()) {
            throw new FileNotFoundException("cannot find directory : " + targetDir.toString());
        }

        return listFilesAndFilesSubDirectories(targetDir.getAbsolutePath());
    }

    @NonNull
    private File getStoredDir(String fileName) {
        // obtain target dir
        String filePath = mFileDir.getAbsolutePath() + File.separatorChar + fileName;
        File f = new File(filePath);
        String name = f.getName();
        File targetDir;
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0) {
            // No period after first character - return name as it was passed in
            targetDir = new File(filePath);
        } else {
            // Remove the last period and everything after it
            File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
            targetDir = new File(renamed.getPath());
        }
        return targetDir;
    }

    interface OnFileCopy {
        void onCopiedSingleFile(int fileCount, File copiedFile, int unpackedBytes);
        void onCopyCompleted(int maxFileCount);
    }

}
