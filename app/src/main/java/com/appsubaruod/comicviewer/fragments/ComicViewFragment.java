package com.appsubaruod.comicviewer.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsubaruod.comicviewer.R;
import com.appsubaruod.comicviewer.databinding.FragmentViewComicBinding;
import com.appsubaruod.comicviewer.viewmodel.ComicViewModel;

import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ComicViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComicViewFragment extends Fragment {
    private static final String LOG_TAG = ComicViewFragment.class.getName();
    private ComicViewModel mComicViewModel;

    // This constructor is called by Android FW.
    // Do not add arguments for fragment installation. Use static method insted.
    // http://mixi-inc.github.io/AndroidTraining/fundamentals/2.02.activity-and-fragment.html
    public ComicViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * Use this method instead of expanded constructor.
     * http://mixi-inc.github.io/AndroidTraining/fundamentals/2.02.activity-and-fragment.html
     *
     * @return A new instance of fragment ComicViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ComicViewFragment newInstance() {
        ComicViewFragment fragment = new ComicViewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        EventBus.getDefault().register(mComicViewModel);
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
        EventBus.getDefault().unregister(mComicViewModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentViewComicBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_view_comic, container, false);

        // create binder object and set
        mComicViewModel = new ComicViewModel();
        binder.setBody(mComicViewModel);

        return binder.getRoot();
    }
}
