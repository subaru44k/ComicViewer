package com.appsubaruod.comicviewer.utils.messages;

import android.view.MenuItem;

/**
 * Created by s-yamada on 2017/03/03.
 */
public class MenuClickEvent {
    public MenuItem mMenuItem;

    public MenuClickEvent(MenuItem item) {
        this.mMenuItem = item;
    }

    public int getItemId() {
        return mMenuItem.getItemId();
    }
}
