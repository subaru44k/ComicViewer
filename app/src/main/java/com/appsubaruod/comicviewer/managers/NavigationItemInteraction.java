package com.appsubaruod.comicviewer.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;

import com.appsubaruod.comicviewer.R;
import com.appsubaruod.comicviewer.model.ComicModel;
import com.appsubaruod.comicviewer.utils.messages.MenuClickEvent;
import com.appsubaruod.comicviewer.utils.messages.NavigationItemCloseEvent;
import com.appsubaruod.comicviewer.utils.messages.RequestActivityIntentEvent;
import com.appsubaruod.comicviewer.utils.messages.SelectPageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.appsubaruod.comicviewer.utils.Constant.CHOSE_FILE_CODE;

/**
 * Created by s-yamada on 2017/02/24.
 */
public class NavigationItemInteraction {

    private ComicModel mComicModel;
    private Context mContext;


    public NavigationItemInteraction(Context context) {
        mContext = context;
        mComicModel = ComicModel.getInstance(mContext);
    }

    private static String LOG_TAG = NavigationItemInteraction.class.getName();

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onItemSelected(MenuClickEvent event) {
        // Handle navigation view item clicks here.
        int id = event.getItemId();

        switch (id) {
            case R.id.nav_openbook:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                EventBus.getDefault().post(new RequestActivityIntentEvent(intent, CHOSE_FILE_CODE));
                break;
            case R.id.nav_gallery:
                EventBus.getDefault().post(new SelectPageEvent(mComicModel.getPageIndex(), mComicModel.getMaxPageIndex()));
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

    public void extractFile(Uri uri) {
        mComicModel.readComic(uri);
    }

    public void notifyItemSelected(MenuItem item) {
        Log.d("hoge", "item selected:" + item.toString());
    }
}
