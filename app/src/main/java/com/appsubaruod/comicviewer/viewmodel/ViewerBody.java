package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.utils.messages.SetImageEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by s-yamada on 2017/03/10.
 */
public class ViewerBody extends BaseObservable {
    public Bitmap mMainImage;

    public ViewerBody(Bitmap image) {
        mMainImage = image;
    }

    @Bindable
    public Bitmap getMainImage() {
        return mMainImage;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setMainImage(SetImageEvent event) {
        mMainImage = event.getImage();
        notifyPropertyChanged(BR.mainImage);
    }

    @BindingAdapter("imageBitmap")
    public static void setImageBitmap(ImageView view, Bitmap image) {
        view.setImageBitmap(image);
    }
}
