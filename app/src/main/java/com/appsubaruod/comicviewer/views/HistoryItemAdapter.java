package com.appsubaruod.comicviewer.views;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.appsubaruod.comicviewer.R;
import com.appsubaruod.comicviewer.databinding.HistoryItemBinding;
import com.appsubaruod.comicviewer.viewmodel.HistoryItemViewModel;

import java.util.List;

/**
 * Created by s-yamada on 2017/05/01.
 */

public class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.HistoryItemViewHolder> {
    private List<HistoryItemViewModel> mHistories;

    public HistoryItemAdapter(List<HistoryItemViewModel> histories) {
        mHistories = histories;
    }

    @Override
    public HistoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HistoryItemBinding bindings = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.history_item, parent, false);
        return new HistoryItemViewHolder(bindings);
    }

    @Override
    public void onBindViewHolder(HistoryItemViewHolder holder, int position) {
        holder.getHistoryItemBinding().setHistoryitem(mHistories.get(position));
    }

    @Override
    public int getItemCount() {
        return mHistories.size();
    }

    static class HistoryItemViewHolder extends RecyclerView.ViewHolder {
        private final HistoryItemBinding mBinding;

        public HistoryItemViewHolder(HistoryItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public HistoryItemBinding getHistoryItemBinding() {
            return mBinding;
        }
    }
}
