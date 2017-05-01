package com.appsubaruod.comicviewer.fragments;


import android.support.v4.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsubaruod.comicviewer.R;
import com.appsubaruod.comicviewer.databinding.FragmentHistoryBinding;
import com.appsubaruod.comicviewer.viewmodel.HistoryViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    private static final String LOG_TAG = HistoryFragment.class.getName();
    HistoryViewModel mHistoryViewModel;

    // This constructor is called by Android FW.
    // Do not add arguments for fragment installation. Use static method insted.
    // http://mixi-inc.github.io/AndroidTraining/fundamentals/2.02.activity-and-fragment.html
    public HistoryFragment() {
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
    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
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
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentHistoryBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false);

        mHistoryViewModel = new HistoryViewModel();
        binding.setHistory(mHistoryViewModel);

        return binding.getRoot();
    }

}