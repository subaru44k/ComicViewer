package com.appsubaruod.comicviewer.utils;

import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by s-yamada on 2017/04/07.
 */

public class ImageOperator {
    private static final String LOG_TAG = ImageOperator.class.getName();

    /**
     * Sets image file to the imageView with appropriate size
     * @param view imageView that image is set
     * @param imageFile image file to be set
     */
    public void setFileToImageView(final ImageView view, final File imageFile) {
        final int width = view.getWidth();
        final int height = view.getHeight();

        if (width == 0 && height == 0) {
            // layout size has not measured
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.d(LOG_TAG, "onGlobalLayout");
                    Picasso.with(view.getContext()).load(imageFile).resize(view.getWidth(), view.getHeight()).centerInside().into(view);
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else {
            // TODO if width is 0 but height is not 0 case, this would collapse layout.
            // Is width and height updated simultaneously?
            Picasso.with(view.getContext()).load(imageFile).resize(width, height).centerInside().into(view);
        }
    }
}
