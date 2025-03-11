package com.vinodsharma.ctabustracker.adapters;

import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.vinodsharma.ctabustracker.activities.PredictionsActivity;
import com.vinodsharma.ctabustracker.activities.StopsActivity;
import com.vinodsharma.ctabustracker.databinding.ActivityStopEntryBinding;
import com.vinodsharma.ctabustracker.models.Stops;
import com.vinodsharma.ctabustracker.view_holders.RoutesViewHolder;
import com.vinodsharma.ctabustracker.view_holders.StopsViewHolder;

import java.util.ArrayList;

public class StopsAdapter extends RecyclerView.Adapter<StopsViewHolder> {
    private final  ArrayList<Stops> stopsList;
   private final StopsActivity stopsActivity;
    private static final String TAG = "StopsAdapter";
    private final String routeNumber;
    private final String routeName;
    private final String direction;


    public StopsAdapter(ArrayList<Stops> stopList, StopsActivity sa, String routeNumber, String routeName, String direction) {
       this.stopsList = stopList;
       this.stopsActivity = sa;
       this.routeNumber = routeNumber;
       this.routeName = routeName;
         this.direction = direction;
   }


   @NonNull
   @Override
    public StopsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int ViewType) {
       ActivityStopEntryBinding binding = ActivityStopEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

       return new StopsViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull StopsViewHolder holder, int position){
       Stops stops = stopsList.get(position);

        //set the viewFields here
        holder.binding.stopName.setText(stops.getStpnm());

        try {
            double stopLat = Double.parseDouble(stops.getLat());
            double stopLon = Double.parseDouble(stops.getLon());

            float[] results = new float[1];
            Location.distanceBetween(
                    stopsActivity.userLocation.latitude,
                    stopsActivity.userLocation.longitude,
                    stopLat,
                    stopLon,
                    results
            );

            int distance = Math.round(results[0]);
            String direction = getDirectionText(stopsActivity.userLocation, stopLat, stopLon);
            holder.binding.busDistance.setText(distance + " m " + direction + " of your location");
        } catch (NumberFormatException e) {
            holder.binding.busDistance.setText("Distance unavailable");
            Log.e(TAG, "Invalid lat/lon for stop: " + stops.getStpnm(), e);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Stops selectedStop = stopsList.get(currentPosition);
                // Redirect to PredictionsActivity with route number and stop ID
                Intent intent = new Intent(stopsActivity, PredictionsActivity.class);
                intent.putExtra("ROUTE_NUM", routeNumber);
                intent.putExtra("ROUTE_NAME", routeName);
                intent.putExtra("STOP_ID", selectedStop.getStpid());
                intent.putExtra("STOP_NAME", selectedStop.getStpnm());
                intent.putExtra("DIRECTION", direction);
                intent.putExtra("STOP_LAN", selectedStop.getLat());
                intent.putExtra("STOP_LON", selectedStop.getLon());
                intent.putExtra("USER_LOCATION", stopsActivity.userLocation);
                stopsActivity.startActivity(intent);
            }
        });

    }


    private String getDirectionText(LatLng userLocation, double stopLat, double stopLon) {
        double latDiff = stopLat - userLocation.latitude;
        double lonDiff = stopLon - userLocation.longitude;

        if (Math.abs(latDiff) > Math.abs(lonDiff)) {
            return latDiff > 0 ? "north" : "south";
        } else {
            return lonDiff > 0 ? "east" : "west";
        }
    }

    @Override
    public int getItemCount() {
        return stopsList.size();
    }

}
