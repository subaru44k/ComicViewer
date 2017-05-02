package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.ImageView;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.utils.ImageOperator;

import java.io.File;

/**
 * Created by s-yamada on 2017/05/01.
 */
public class HistoryItemViewModel extends BaseObservable {
    private static final String LOG_TAG = BaseObservable.class.getName();
    private String mTitle;
    private File mImageFile;
    private static ImageOperator mImageOperator = new ImageOperator();

    public HistoryItemViewModel(String title, File imageFile) {
        mTitle = title;
        mImageFile = imageFile;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Bindable
    public File getImageFile() {
        return mImageFile;
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
