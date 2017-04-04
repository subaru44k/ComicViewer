 package com.appsubaruod.comicviewer.activity;

 import android.content.Intent;
 import android.os.Bundle;
 import android.support.design.widget.NavigationView;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBarDrawerToggle;
 import android.support.v7.app.AppCompatActivity;
 import android.support.v7.widget.Toolbar;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;

 import com.appsubaruod.comicviewer.R;
 import com.appsubaruod.comicviewer.fragments.ComicViewFragment;
 import com.appsubaruod.comicviewer.fragments.SelectPageFragment;
 import com.appsubaruod.comicviewer.managers.NavigationItemInteraction;
 import com.appsubaruod.comicviewer.utils.messages.BackKeyEvent;
 import com.appsubaruod.comicviewer.utils.messages.MenuClickEvent;
 import com.appsubaruod.comicviewer.utils.messages.NavigationItemCloseEvent;
 import com.appsubaruod.comicviewer.utils.messages.ReadComicEvent;
 import com.appsubaruod.comicviewer.utils.messages.RequestActivityIntentEvent;
 import com.appsubaruod.comicviewer.utils.messages.SelectPageEvent;

 import org.greenrobot.eventbus.EventBus;
 import org.greenrobot.eventbus.Subscribe;
 import org.greenrobot.eventbus.ThreadMode;

 import static com.appsubaruod.comicviewer.utils.Constant.CHOSE_FILE_CODE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationItemInteraction mNavigationItemInteraction;
    private static final String LOG_TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNavigationItemInteraction = new NavigationItemInteraction(getApplicationContext());

        FragmentManager manager = getSupportFragmentManager();
        // FragmentTransaction を開始
        FragmentTransaction transaction = manager.beginTransaction();

        // FragmentContainer のレイアウトに、MyFragment を割当てる
        transaction.add(R.id.FragmentContainer, ComicViewFragment.newInstance());

        // 変更を確定して FragmentTransaction を終える
        transaction.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(mNavigationItemInteraction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(mNavigationItemInteraction);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Send event
        EventBus.getDefault().post(new MenuClickEvent(item));

        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void closeNavigationItem(NavigationItemCloseEvent event) {
        Log.d(LOG_TAG, "closeNavigationItem");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dispatchIntentForResult(RequestActivityIntentEvent event) {
        startActivityForResult(event.getIntent(), event.getCode());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReadComicFragment(ReadComicEvent event) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        // change fragment and add ComicViewFragment
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        transaction.replace(R.id.FragmentContainer, ComicViewFragment.newInstance());

        transaction.commit();

        Log.d(LOG_TAG, "backstack count : " + manager.getBackStackEntryCount());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showSelectPageFragment(SelectPageEvent event) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        // change fragment and add to back stack
        transaction.replace(R.id.FragmentContainer, SelectPageFragment.newInstance());
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleBackKeyEvent(BackKeyEvent event) {
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            // cancelled or something
            return;
        }

        switch (requestCode) {
            case CHOSE_FILE_CODE:
                mNavigationItemInteraction.extractFile(data.getData());
                break;
            default:
                break;
        }
    }
}
