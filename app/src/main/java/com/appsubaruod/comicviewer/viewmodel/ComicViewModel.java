package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.model.ComicModel;
import com.appsubaruod.comicviewer.utils.messages.SetImageEvent;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

/**
 * Created by s-yamada on 2017/03/10.
 */
public class ComicViewModel extends BaseObservable {
    private static final String LOG_TAG = ComicViewModel.class.getName();
    private File mMainImageFile;
    private ComicModel mComicModel;

    public ComicViewModel() {
        mComicModel = ComicModel.getInstanceIfCreated();
        mComicModel.requestSpecifiedPage(mComicModel.getPageIndex());
    }

    @Bindable
    public File getMainImageFile() {
        return mMainImageFile;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void setMainImageFile(SetImageEvent event) {
        mMainImageFile = event.getImageFile();
        notifyPropertyChanged(BR.mainImageFile);
    }

    public void clickLeftSide(View view) {
        Log.d(LOG_TAG, "Click left side");
        mComicModel.readNextPage();
    }

    public void clickRightSide(View view) {
        Log.d(LOG_TAG, "Click right side");
        mComicModel.readPreviousPage();
    }

    @BindingAdapter("loadImageFile")
    public static void setImageBitmap(ImageView view, File imageFile) {
        if (imageFile != null) {
            Log.d(LOG_TAG, "setImageBitmap : " + imageFile.getPath());
        } else {
            Log.w(LOG_TAG, "setImageBitmap : null");
        }
        Picasso.with(view.getContext()).load(imageFile).fit().into(view);
    }
}
