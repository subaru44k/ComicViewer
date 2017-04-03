package com.appsubaruod.comicviewer.fragments;

import android.support.v4.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsubaruod.comicviewer.R;
import com.appsubaruod.comicviewer.databinding.FragmentSelectPageBinding;
import com.appsubaruod.comicviewer.viewmodel.SelectPageViewModel;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by s-yamada on 2017/04/03.
 */

public class SelectPageFragment extends Fragment {
    private SelectPageViewModel mSelectPageModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "currentPage";
    private static final String ARG_PARAM2 = "maxPage";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // This constructor is called by Android FW.
    // Do not add arguments for fragment installation. Use static method insted.
    // http://mixi-inc.github.io/AndroidTraining/fundamentals/2.02.activity-and-fragment.html
    public SelectPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * Use this method instead of expanded constructor.
     * http://mixi-inc.github.io/AndroidTraining/fundamentals/2.02.activity-and-fragment.html
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ComicViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectPageFragment newInstance(String currentPage, String maxPage) {
        SelectPageFragment fragment = new SelectPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, currentPage);
        args.putString(ARG_PARAM2, maxPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(mSelectPageModel);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(mSelectPageModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentSelectPageBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_page, container, false);

        mSelectPageModel = new SelectPageViewModel();
        binding.setSelectPage(mSelectPageModel);

        return binding.getRoot();
    }

}
