package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.model.ComicModel;
import com.appsubaruod.comicviewer.utils.messages.BackKeyEvent;
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
    private ComicModel mComicModel;
    private int mCurrentPageIndex;
    private int mMaxPageIndex;
    private File mCurrentPageFile;

    public SelectPageViewModel() {
        mComicModel = ComicModel.getInstance(null);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setCurrentPageFile(SetImageEvent event) {
        mCurrentPageIndex = event.getImageIndex();
        mCurrentPageFile = event.getImageFile();
        notifyPropertyChanged(BR.currentPageIndex);
        notifyPropertyChanged(BR.currentPageFile);
    }

    public void select(View view) {
        mComicModel.readSpecifiedPage(mCurrentPageIndex);
        EventBus.getDefault().post(new BackKeyEvent());
    }

    public void cancel(View view) {
        EventBus.getDefault().post(new BackKeyEvent());
    }

    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
        mComicModel.requestSpecifiedPage(progresValue);
    }

    @BindingAdapter("loadImageFile")
    public static void setImageBitmap(ImageView view, File imageFile) {
        Picasso.with(view.getContext()).load(imageFile).fit().into(view);
    }
}
