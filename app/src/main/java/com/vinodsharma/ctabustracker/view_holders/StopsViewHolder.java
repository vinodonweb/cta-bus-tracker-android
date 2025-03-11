package com.vinodsharma.ctabustracker.view_holders;

import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.ctabustracker.databinding.ActivityStopEntryBinding;

public class StopsViewHolder extends RecyclerView.ViewHolder {

    public ActivityStopEntryBinding binding;

    public StopsViewHolder(ActivityStopEntryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
