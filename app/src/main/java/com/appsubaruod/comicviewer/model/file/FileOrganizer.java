package com.appsubaruod.comicviewer.model.file;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by s-yamada on 2017/04/15.
 */

public class FileOrganizer {
    public static final String IMAGE_DIR_NAME = "image";
    private Context mContext;
    private File mFileDir;
    private FileOperator mFileOperator;
    private Set<FileResolve> mCallbackSet = new HashSet<>();
    private Map<String, ResolvedContent> mResolvedContentMap = new WeakHashMap<>();

    private FileOperator.OnFileCopy mOnFileCopy = new FileOperator.OnFileCopy() {
        @Override
        public void onCopiedSingleFile(int fileCount, File copiedFile, int unpackedBytes) {
            notifySingleFileResolved(fileCount, copiedFile, unpackedBytes);
        }

        @Override
        public void onCopyCompleted(String dirName, int maxFileCount) {
            notifyAllFileResolved(dirName, maxFileCount);
        }
    };

    private void notifyAllFileResolved(String dirName, int maxFileCount) {
        for (FileResolve callback : mCallbackSet) {
            callback.onAllFileResolved(dirName, maxFileCount);
        }
    }

    private void notifySingleFileResolved(int fileCount, File copiedFile, int unpackedBytes) {
        for (FileResolve callback : mCallbackSet) {
            callback.onSingleFileResolved(fileCount, copiedFile, unpackedBytes);
        }
    }

    private static final String LOG_TAG = FileOrganizer.class.getName();

    public FileOrganizer(Context context) {
        mContext = context;
        mFileOperator = new FileOperator(mContext);
        mFileDir = getFilesDir();
        mFileOperator.registerCallback(mOnFileCopy);
    }

    public void registerCallback(FileResolve fileResolve) {
        mCallbackSet.add(fileResolve);
    }

    public void unregisterCallback(FileResolve fileResolve) {
        mCallbackSet.remove(fileResolve);
    }

    /**
     * Requests to resolve network file from uri
     * @param uri resolve target
     */
    public void requestNetworkContent(Uri uri) {
        ContentResolver cR = mContext.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extensionFromMimeType = mime.getExtensionFromMimeType(cR.getType(uri));
        Log.d(LOG_TAG, "mime-type : " + extensionFromMimeType);
        if ("zip".equals(extensionFromMimeType)) {
            requestNetworkZipContent(uri);
        } else {
            Log.w(LOG_TAG, "this type of file is not supported now!");
        }
    }

    private String getNetworkContentFileName(Uri uri) {
        String pathNameOfUri = uri.getPath();

        final int lastPeriodPos = pathNameOfUri.lastIndexOf('/');
        if (lastPeriodPos <= 0) {
            return "no_file_name";
        } else {
            // Remove the last period and everything after it
            return pathNameOfUri.substring(lastPeriodPos + 1, pathNameOfUri.length());
        }
    }

    /**
     * Requests to resolve local zip file from uri
     * @param uri resolve target
     */
    public void requestLocalZipContent(Uri uri) {
        requestZipContent(uri, getStoringDirName(uri));
    }

    /**
     * Requests to resolve network zip file from uri
     * @param uri resolve target
     */
    public void requestNetworkZipContent(Uri uri) {
        requestZipContent(uri, getNetworkContentFileName(uri));
    }

    private void requestZipContent(Uri uri, String dirName) {
        ResolvedContent content = getContent(dirName);
        if (content != null) {
            // found content in memory cache or app storage
            for (File file : content.getFiles()) {
                notifySingleFileResolved(content.getContentIndex(file), file, content.getContentSize(file));
            }
            notifyAllFileResolved(dirName, content.fileCount());
            return;
        }
        content = mFileOperator.unpackZip(getFilesDir(), dirName, uri);
        setContent(dirName, content);
    }

    private void setContent(String dirName, ResolvedContent content) {
        mResolvedContentMap.put(dirName, content);
    }

    @Nullable
    private ResolvedContent getContent(String storingDirName) {
        try{
            return getResolvedContent(storingDirName);
        } catch (FileNotFoundException e) {
            Log.i(LOG_TAG, "new file" + storingDirName);
            return null;
        }
    }

    private ResolvedContent getResolvedContent(String storedDirName) throws FileNotFoundException {
        // check weak hash map
        ResolvedContent content = mResolvedContentMap.get(storedDirName);
        if (content != null) {
            Log.i(LOG_TAG, "found in memory cache : " + storedDirName);
            return content;
        } else {
            File targetDir = new File(getFilesDir() + File.separator + storedDirName);
            List<File> fileList = mFileOperator.getFileList(targetDir);
            int index = 1;
            content = new ResolvedContent();
            for (File file : fileList) {
                content.store(file, index, file.length());
                index++;
            }
            setContent(storedDirName, content);
            Log.i(LOG_TAG, "found in directory cache : " + storedDirName);
            return content;
        }
    }

    public String getStoringDirName(Uri uri) {
        String fileName = getFileName(uri);
        String targetDirName;

        final int lastPeriodPos = fileName.lastIndexOf('.');
        if (lastPeriodPos <= 0) {
            targetDirName = fileName;
        } else {
            // Remove the last period and everything after it
            targetDirName = fileName.substring(0, lastPeriodPos);
        }
        return targetDirName;
    }

    @NonNull
    private String getFileName(Uri uri) {
        return resolveFile(uri).getName();
    }

    @NonNull
    private File resolveFile(Uri uri) {
        return new File(getPath(uri));
    }

    private File getFilesDir() {
        return mContext.getFilesDir();
    }

    /**
     * Requests to resolve local image file from uri
     * @param uri resolve target
     */
    public void requestLocalImageContent(Uri uri) {
        File targetFile = resolveFile(uri);

        // does not cache image files
        mFileOperator.copyImageFiles(getFilesDir(), IMAGE_DIR_NAME, targetFile.getParentFile());
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
                                String[] selectionArgs) throws IllegalArgumentException {
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
            String errorString = e.getMessage();
            Log.w(LOG_TAG, errorString);
            Log.i(LOG_TAG, "maybe network content");
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

    public interface FileResolve {
        void onSingleFileResolved(int fileCount, File resolvedFile, int sizeBytes);
        void onAllFileResolved(String dirName, int maxFileCount);
    }
}
