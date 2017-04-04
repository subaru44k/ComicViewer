package com.appsubaruod.comicviewer.utils.messages;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by s-yamada on 2017/04/01.
 */

public class SetImageEvent {
    private int mImageIndex;
    private File mImageFile;

    public SetImageEvent(int imageIndex, File imageFile) {
        mImageIndex = imageIndex;
        mImageFile = imageFile;
    }

    public int getImageIndex() {
        return mImageIndex;
    }

    public File getImageFile() {
        return mImageFile;
    }

}
