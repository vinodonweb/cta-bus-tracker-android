package com.vinodsharma.ctabustracker.view_holders;

import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.ctabustracker.databinding.ActivityPredictionsEntryBinding;

public class PredictionsViewHolder extends RecyclerView.ViewHolder {

    public ActivityPredictionsEntryBinding binding;

    public PredictionsViewHolder(ActivityPredictionsEntryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
