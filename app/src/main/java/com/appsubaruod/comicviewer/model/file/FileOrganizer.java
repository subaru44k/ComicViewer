package com.appsubaruod.comicviewer.model.file;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
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
    private Context mContext;
    private FileOperator mFileOperator;
    private Set<FileResolve> mCallbackSet = new HashSet<>();
    private Map<String, ResolvedContent> mResolvedContentMap = new WeakHashMap<>();

    private FileOperator.OnFileCopy mOnFileCopy = new FileOperator.OnFileCopy() {
        @Override
        public void onCopiedSingleFile(int fileCount, File copiedFile, int unpackedBytes) {
            notifySingleFileResolved(fileCount, copiedFile, unpackedBytes);
        }

        @Override
        public void onCopyCompleted(int maxFileCount) {
            notifyAllFileResolved(maxFileCount);
        }
    };

    private void notifyAllFileResolved(int maxFileCount) {
        for (FileResolve callback : mCallbackSet) {
            callback.onAllFileResolved(maxFileCount);
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
        mFileOperator = new FileOperator(mContext, getFilesDir());
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
            mFileOperator.unpackZip(uri);
        } else {
            Log.w(LOG_TAG, "this type of file is not supported now!");
        }
    }

    /**
     * Requests to resolve zip file from uri
     * @param uri resolve target
     */
    public void requestZipContent(Uri uri) {
        ResolvedContent content = getContent(uri);
        if (content != null) {
            // find content in app storageg
            for (File file : content.getFiles()) {
                notifySingleFileResolved(content.getContentIndex(file), file, content.getContentSize(file));
            }
            notifyAllFileResolved(content.fileCount());
            return;
        }
        mFileOperator.unpackZip(uri);
    }

    private ResolvedContent getContent(Uri uri) {
        String filePathName = mFileOperator.getStoringDirString(uri);
        // check weak hash map
        ResolvedContent content = mResolvedContentMap.get(filePathName);
        try {
            if (content != null) {
                return content;
            } else {
                List<File> fileList = mFileOperator.getFileList(filePathName);
                int index = 1;
                content = new ResolvedContent();
                for (File file : fileList) {
                    content.store(file, index, file.length());
                    index++;
                }
                mResolvedContentMap.put(filePathName, content);
                return content;
            }
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "new file" + uri);
        }
        return null;
    }

    private String resolveFileName(Uri uri) {
        return (new File(getPath(uri))).getName();
    }

    private File getFilesDir() {
        return mContext.getFilesDir();
    }

    /**
     * Requests to resolve local image file from uri
     * @param uri resolve target
     */
    public void requestImageContent(Uri uri) {
        mFileOperator.copyImageFiles(uri);
    }

    /**
     * Obtains actual path of the uri.
     * If it failed e.g. acutual file is located on network, null is returned.
     * @param uri resolve target
     * @return File path if it is a local file, otherwise null
     */
    public String getPath(Uri uri) {
        return mFileOperator.getPath(uri);
    }

    public interface FileResolve {
        void onSingleFileResolved(int fileCount, File resolvedFile, int sizeBytes);
        void onAllFileResolved(int maxFileCount);
    }
}
