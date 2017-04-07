package com.appsubaruod.comicviewer.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.appsubaruod.comicviewer.BR;
import com.appsubaruod.comicviewer.utils.messages.BookOpenedEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by s-yamada on 2017/04/07.
 */

public class ActivityMainViewModel extends BaseObservable {
    private boolean mBookOpened = false;

    @Bindable
    public boolean isBookOpened() {
        return mBookOpened;
    }

    public void setBookOpened(boolean bookOpened) {
        mBookOpened = bookOpened;
        notifyPropertyChanged(BR.bookOpened);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void receiveBookOpened(BookOpenedEvent event) {
        if (mBookOpened == false) {
            setBookOpened(true);
        }
    }
}
