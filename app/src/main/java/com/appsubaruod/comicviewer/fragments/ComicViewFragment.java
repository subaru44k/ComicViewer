package com.appsubaruod.comicviewer.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appsubaruod.comicviewer.R;
import com.appsubaruod.comicviewer.databinding.FragmentViewComicBinding;
import com.appsubaruod.comicviewer.viewmodel.ComicViewModel;

import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ComicViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ComicViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComicViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ComicViewModel mComicViewModel;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ComicViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ComicViewFragment newInstance(String param1, String param2) {
        ComicViewFragment fragment = new ComicViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
        EventBus.getDefault().register(mComicViewModel);
    }

    @Override
    public void onPause() {
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
