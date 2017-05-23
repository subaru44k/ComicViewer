package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.ThreadContainer;
import com.appsubaruod.comicviewer.utils.ImageOperator;

import java.io.File;

/**
 * Created by s-yamada on 2017/05/01.
 */
public class HistoryItemViewModel extends BaseObservable {
    private static final String LOG_TAG = BaseObservable.class.getName();
    private String mTitle;
    private File mImageFile;
    private int mPageIndex;
    private int mBackgroundColor = Color.parseColor("#FFFFFF");
    private static ImageOperator mImageOperator = new ImageOperator();

    public HistoryItemViewModel(String title, File imageFile, int pageIndex) {
        mTitle = title;
        mImageFile = imageFile;
        mPageIndex = pageIndex;
        notifyPropertyChanged(BR.title);
        notifyPropertyChanged(BR.imageFile);

        calculateAndSetBackgroundColor(mImageFile);
    }

    private void calculateAndSetBackgroundColor(final File imageFile) {
        ThreadContainer.invokeSingleThreadTask(new Runnable() {
            @Override
            public void run() {
                mBackgroundColor = getPrimaryColor(mImageFile);
                HistoryItemViewModel.this.notifyPropertyChanged(BR.backgroundColor);
            }
        });
    }

    private int getPrimaryColor(File imageFile) {
        Bitmap imageBitMap = BitmapFactory.decodeFile(imageFile.toString());
        int color = Color.parseColor("#FFFFFF");
        if (imageBitMap != null) {
            int width = imageBitMap.getWidth();
            int height = imageBitMap.getHeight();
            color = imageBitMap.getPixel(width / 2, height / 2);
            // TODO background color is too strong. set alpha or something to make it mild.
        }
        return color;
    }

    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Bindable
    public File getImageFile() {
        return mImageFile;
    }

    @Bindable
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public int getPageIndex() {
        return mPageIndex;
    }

    @BindingAdapter("loadHistoryImageFile")
    public static void setImageBitmap(ImageView view, File imageFile) {
        if (imageFile != null) {
            Log.d(LOG_TAG, "setImageBitmap : " + imageFile.getPath());
        } else {
            Log.w(LOG_TAG, "setImageBitmap : null");
        }

        mImageOperator.setFileToImageView(view, imageFile);

    }
}
