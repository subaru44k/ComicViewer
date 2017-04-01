package com.appsubaruod.comicviewer.utils.messages;

import android.graphics.Bitmap;

/**
 * Created by s-yamada on 2017/04/01.
 */

public class SetImageEvent {
    Bitmap mImage;

    public SetImageEvent(Bitmap image) {
        mImage = image;
    }

    public Bitmap getImage() {
        return mImage;
    }
}
