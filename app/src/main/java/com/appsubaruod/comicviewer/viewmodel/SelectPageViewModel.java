package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.model.ComicModel;
import com.appsubaruod.comicviewer.utils.messages.BackKeyEvent;
import com.appsubaruod.comicviewer.utils.messages.LoadCompleteEvent;
import com.appsubaruod.comicviewer.utils.messages.SetImageEvent;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

/**
 * Created by s-yamada on 2017/04/03.
 */

public class SelectPageViewModel extends BaseObservable {
    private static final String LOG_TAG = SelectPageViewModel.class.getName();
    private ComicModel mComicModel;
    private int mCurrentPageIndex;
    private int mMaxPageIndex;
    private File mCurrentPageFile;

    public SelectPageViewModel() {
        mComicModel = ComicModel.getInstanceIfCreated();
        setMaxPageIndex(mComicModel.getMaxPageIndex());
        mComicModel.requestSpecifiedPage(mComicModel.getPageIndex());
    }

    @Bindable
    public int getCurrentPageIndex() {
        return mCurrentPageIndex;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        mCurrentPageIndex = currentPageIndex;
        notifyPropertyChanged(BR.currentPageIndex);
    }

    @Bindable
    public int getMaxPageIndex() {
        return mMaxPageIndex;
    }

    public void setMaxPageIndex(int maxPageIndex) {
        mMaxPageIndex = maxPageIndex;
        notifyPropertyChanged(BR.maxPageIndex);
    }

    @Bindable
    public File getCurrentPageFile() {
        return mCurrentPageFile;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void setCurrentPageFile(SetImageEvent event) {
        mCurrentPageIndex = event.getImageIndex();
        mCurrentPageFile = event.getImageFile();
        notifyPropertyChanged(BR.currentPageIndex);
        notifyPropertyChanged(BR.currentPageFile);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onLoadComplete(LoadCompleteEvent event) {
        setMaxPageIndex(event.getMaxPage());
    }

    public void select(View view) {
        mComicModel.readSpecifiedPage(mCurrentPageIndex);
        EventBus.getDefault().post(new BackKeyEvent());
    }

    public void cancel(View view) {
        EventBus.getDefault().post(new BackKeyEvent());
    }

    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
        Log.d(LOG_TAG, "onProgressChanged");
        if (fromUser) {
            mComicModel.requestSpecifiedPage(progressValue);
        } else {
            // onProgressChanged may called from system when SeekBar is loaded or max value is changed.
            // In this case ignore.
            Log.d(LOG_TAG, "SeekBar.onProgressChanged is called from system, ignore.");
        }
    }

    @BindingAdapter("loadSelectedImageFile")
    public static void setImageBitmap(ImageView view, File imageFile) {
        if (imageFile != null) {
            Log.d(LOG_TAG, "setImageBitmap : " + imageFile.getPath());
        } else {
            Log.w(LOG_TAG, "setImageBitmap : null");
        }
        Picasso.with(view.getContext()).load(imageFile).fit().into(view);
    }
}
