package com.vinodsharma.ctabustracker.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vinodsharma.ctabustracker.activities.MainActivity;
import com.vinodsharma.ctabustracker.databinding.ActivityRouteEntryBinding;
import com.vinodsharma.ctabustracker.models.Routes;
import com.vinodsharma.ctabustracker.view_holders.RoutesViewHolder;

import java.util.ArrayList;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesViewHolder> {

    private final ArrayList<Routes> routesList;
    private final ArrayList<Routes> originalRoutesList;
    private static final String TAG = "RoutesAdapter";
    private final MainActivity mainActivity;

    public RoutesAdapter(ArrayList<Routes> routesList, MainActivity ma) {
        this.originalRoutesList = new ArrayList<>(routesList);
        this.routesList = routesList;
        this.mainActivity = ma;
    }

    public void filterRoutes(String searchText) {
        routesList.clear();

        if (searchText.isEmpty()) {
            routesList.addAll(originalRoutesList);
        } else {
            String lowerSearchText = searchText.toLowerCase().trim();
            for (Routes route : originalRoutesList) {
                if (route.getrNum().toLowerCase().contains(lowerSearchText) ||
                        route.getrName().toLowerCase().contains(lowerSearchText)) {
                    routesList.add(route);
                }
            }
        }

        notifyDataSetChanged();
    }

    //This method is used to get the route at a particular position
    public Routes getRouteAtPosition(int position) {
        return routesList.get(position);
    }


    @NonNull
    @Override
    public RoutesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityRouteEntryBinding binding = ActivityRouteEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        // Set the click listener for popup menu
        binding.getRoot().setOnClickListener(mainActivity);

        return new RoutesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutesViewHolder holder, int position) {

        Routes routes = routesList.get(position);



        // Parse the route's color
        int backgroundColor = android.graphics.Color.parseColor(routes.getrColor());
        // Calculate luminance
        double luminance = calculateLuminance(backgroundColor);

        // Set text color based on luminance
        int textColor = (luminance < 0.25) ? Color.WHITE : Color.BLACK;

        // Setting text views
        holder.binding.routeName.setText(routes.getrName());
        holder.binding.routeName.setTextColor(textColor);

        holder.binding.routeNumber.setText(routes.getrNum());
        holder.binding.routeNumber.setTextColor(textColor);

        holder.binding.routeGroup.setBackgroundColor(android.graphics.Color.parseColor(routes.getrColor()));
    }

    private double calculateLuminance(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;


        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    @Override
    public int getItemCount() {
        return routesList.size();
    }
}
