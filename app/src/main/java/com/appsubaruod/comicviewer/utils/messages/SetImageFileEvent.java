package com.appsubaruod.comicviewer.utils.messages;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by s-yamada on 2017/04/01.
 */

public class SetImageFileEvent {
    private File mImageFile;

    public SetImageFileEvent(File imageFile) {
        mImageFile = imageFile;
    }

    public File getImageFile() {
        return mImageFile;
    }

}
