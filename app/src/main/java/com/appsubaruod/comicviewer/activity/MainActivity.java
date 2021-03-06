 package com.appsubaruod.comicviewer.activity;

 import android.Manifest;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.databinding.DataBindingUtil;
 import android.os.Bundle;
 import android.support.annotation.NonNull;
 import android.support.design.widget.FloatingActionButton;
 import android.support.design.widget.NavigationView;
 import android.support.v4.app.ActivityCompat;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.content.ContextCompat;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.AppCompatActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;

 import com.appsubaruod.comicviewer.R;
 import com.appsubaruod.comicviewer.databinding.ActivityMainBinding;
 import com.appsubaruod.comicviewer.fragments.ComicViewFragment;
 import com.appsubaruod.comicviewer.fragments.SelectPageFragment;
 import com.appsubaruod.comicviewer.managers.NavigationItemInteraction;
 import com.appsubaruod.comicviewer.utils.messages.BackKeyEvent;
 import com.appsubaruod.comicviewer.utils.messages.MenuClickEvent;
 import com.appsubaruod.comicviewer.utils.messages.NavigationItemCloseEvent;
 import com.appsubaruod.comicviewer.utils.messages.ReadComicEvent;
 import com.appsubaruod.comicviewer.utils.messages.RequestActivityIntentEvent;
 import com.appsubaruod.comicviewer.utils.messages.RequestPermissionEvent;
 import com.appsubaruod.comicviewer.utils.messages.SelectPageEvent;
 import com.appsubaruod.comicviewer.viewmodel.ActivityMainViewModel;

 import org.greenrobot.eventbus.EventBus;
 import org.greenrobot.eventbus.Subscribe;
 import org.greenrobot.eventbus.ThreadMode;

 import static com.appsubaruod.comicviewer.utils.Constant.CHOSE_FILE_CODE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainViewModel mActivityMainViewModel;
    private NavigationItemInteraction mNavigationItemInteraction;
    private static final String LOG_TAG = MainActivity.class.getName();

    private static final int PERMISSION_REQUEST_READ_STORAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mActivityMainViewModel = new ActivityMainViewModel();
        binding.setActivityMainModel(mActivityMainViewModel);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // check if the app have permission
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(MainActivity.super.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    EventBus.getDefault().post(new RequestPermissionEvent());
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNavigationItemInteraction = new NavigationItemInteraction(getApplicationContext());

        // stop adding fragment when rotation
        if (savedInstanceState == null) {
            FragmentManager manager = getSupportFragmentManager();
            // FragmentTransaction を開始
            FragmentTransaction transaction = manager.beginTransaction();

            // FragmentContainer のレイアウトに、MyFragment を割当てる
            transaction.add(R.id.FragmentContainer, ComicViewFragment.newInstance());

            // 変更を確定して FragmentTransaction を終える
            transaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(mActivityMainViewModel);
        EventBus.getDefault().register(mNavigationItemInteraction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(mActivityMainViewModel);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleRequestPermissionEvent(RequestPermissionEvent event) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_READ_STORAGE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    EventBus.getDefault().post(new NavigationItemCloseEvent());
                }
                return;
            }
        }
    }
}
