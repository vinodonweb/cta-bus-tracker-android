package com.vinodsharma.ctabustracker.view_holders;

import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.ctabustracker.databinding.ActivityRouteEntryBinding;

public class RoutesViewHolder extends RecyclerView.ViewHolder {

     public ActivityRouteEntryBinding binding;

    public RoutesViewHolder(ActivityRouteEntryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

}
