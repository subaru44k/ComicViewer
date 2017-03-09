package com.appsubaruod.comicviewer.managers;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import com.appsubaruod.comicviewer.R;
import com.appsubaruod.comicviewer.utils.messages.MenuClickEvent;
import com.appsubaruod.comicviewer.utils.messages.NavigationItemCloseEvent;
import com.appsubaruod.comicviewer.utils.messages.RequestActivityIntentEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.appsubaruod.comicviewer.utils.Constant.CHOSE_FILE_CODE;

/**
 * Created by s-yamada on 2017/02/24.
 */
public class NavigationItemInteraction {

    public NavigationItemInteraction() {
    }

    private static String LOG_TAG = NavigationItemInteraction.class.getName();

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onItemSelected(MenuClickEvent event) {
        // Handle navigation view item clicks here.
        int id = event.getItemId();

        switch (id) {
            case R.id.nav_openbook:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                EventBus.getDefault().post(new RequestActivityIntentEvent(intent, CHOSE_FILE_CODE));
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_slideshow:
                break;
            case R.id.nav_manage:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
        }

        EventBus.getDefault().post(new NavigationItemCloseEvent());
    }

    public void notifyItemSelected(MenuItem item) {
        Log.d("hoge", "item selected:" + item.toString());
    }
}
