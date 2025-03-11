package com.vinodsharma.ctabustracker.activities;

import static java.lang.String.format;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.model.LatLng;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.vinodsharma.ctabustracker.R;
import com.vinodsharma.ctabustracker.adapters.PredictionsAdapter;
import com.vinodsharma.ctabustracker.ads.BannerViewListener;
import com.vinodsharma.ctabustracker.ads.UnityInitializationListener;
import com.vinodsharma.ctabustracker.databinding.ActivityPredictionsBinding;
import com.vinodsharma.ctabustracker.models.Predictions;
import com.vinodsharma.ctabustracker.volley.PredictionsVolley;
import com.vinodsharma.ctabustracker.volley.VehiclesVolley;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PredictionsActivity extends AppCompatActivity {

    ActivityPredictionsBinding binding;
    private PredictionsAdapter predictionsAdapter;
    private String stopName;
    private double stopLat;
    private double stopLon;
    private LatLng userLocation;

    //uniity add setup
    private static final String unityGameID = "5783381";
    private static final boolean testMode = false;
    private static final String bannerPlacement = "Banner_android_2";
    private BannerView.IListener bannerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityPredictionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //banner ad initialization
        bannerListener = new BannerViewListener(this);
        // Initialize the SDK:
        UnityAds.initialize(this, unityGameID, testMode,
                new UnityInitializationListener(this));


        //retreive the pass data
        String routeNum = getIntent().getStringExtra("ROUTE_NUM");
        String routeName = getIntent().getStringExtra("ROUTE_NAME");
        String stopId = getIntent().getStringExtra("STOP_ID");
        stopName = getIntent().getStringExtra("STOP_NAME");
        String direction = getIntent().getStringExtra("DIRECTION");

        String stopLatStr = getIntent().getStringExtra("STOP_LAT");
        String stopLonStr = getIntent().getStringExtra("STOP_LON");

        try {
            if (stopLatStr != null && stopLonStr != null) {
                stopLat = Double.parseDouble(stopLatStr.replace(",", "."));
                stopLon = Double.parseDouble(stopLonStr.replace(",", "."));
            }
        } catch (NumberFormatException e) {
            Log.e("PredictionsActivity", "Invalid stop coordinates", e);
            Toast.makeText(this, "Invalid stop coordinates", Toast.LENGTH_SHORT).show();
        }

        Log.d("CoordDebug", "Stop Coordinates - Lat: " + stopLat + ", Lon: " + stopLon);

        // Retrieve user location
        userLocation = getIntent().getParcelableExtra("USER_LOCATION");


        // Initialize RecyclerView
        predictionsAdapter = new PredictionsAdapter(new ArrayList<>(), this);
        binding.predicRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.predicRecyclerView.setAdapter(predictionsAdapter);

        //update the ui
        String predicString = "Route " + routeNum + " - " + routeName;
        binding.predicTitle.setText(predicString);

        String predicTimeStopStr  = stopName + "(" + direction + ")";
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());

        binding.predicDirTimStop.setText(String.format("%s\n%s", predicTimeStopStr, currentTime));


        // Fetch predictions
        PredictionsVolley.downloadPredictions(this,routeNum, stopId);

        //swipe to refresh
        binding.swiper.setOnRefreshListener(() -> {
            PredictionsVolley.downloadPredictions(this,routeNum, stopId);
            binding.swiper.setRefreshing(false);
        });

    }

    //unity ad setup
    public void showBanner() {

        BannerView bottomBanner = new BannerView(
                this, bannerPlacement, UnityBannerSize.getDynamicSize(this));
        bottomBanner.setListener(bannerListener);

        binding.layout.addView(bottomBanner);
        bottomBanner.load();
    }

    //unity ad fail to load
    public void initFailed(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    //unity ad load failed
    public void loadFailed(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

public void showVehicleDistanceDialog(String vid, String prdctdn){
        VehiclesVolley.downloadVehicle(this, vid, (vehicleLat, vehicleLon) -> {
            //calculate distance between vehicle and stop
            float[] resultsStop = new float[1];
            Location.distanceBetween(stopLat, stopLon, vehicleLat, vehicleLon, resultsStop);

            //calculate distance between vehicle and user
            float[] resultsUser = new float[1];
            if(userLocation != null){
                Location.distanceBetween(userLocation.latitude, userLocation.longitude, vehicleLat, vehicleLon, resultsUser);
            } else {
                resultsUser[0] = -1;
            }


            //format distance
            String distanceToStop = formatDistance(resultsStop[0]);
            String distanceToUser = (resultsStop[0] != -1) ? formatDistance(resultsUser[0]) : "Unknown";

            String message;
                    if (resultsUser[0] != -1) {
                        message = String.format(Locale.getDefault(),
                                "Bus# %s is %s (%s min) away from %s.",
                                vid, distanceToUser, prdctdn, stopName);

                    } else {
                        message = String.format(Locale.getDefault(),
                                "Bus# %s is %s (%s min) away from %s.",
                                vid, distanceToStop, prdctdn, stopName);
                    }



                    new AlertDialog.Builder(this)
                            .setTitle("Bus #" + vid)
                            .setMessage(message)
                            .setIcon(R.drawable.bus_icon)
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .setNegativeButton("Show on Map", (d,w) -> {
                                Uri gmmIntentUri = Uri.parse("geo:" + vehicleLat + "," + vehicleLon + "?q=" + vehicleLat + "," + vehicleLon + "(Bus " + vid + ")");
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                } else {
                                    Toast.makeText(this, "Google Maps app is not installed", Toast.LENGTH_LONG).show();
                                }
                            })
                            .show();
        });
}

    private String formatDistance(float meters) {
        if (meters >= 1000) {
            return String.format(Locale.US, "%.1f km", meters / 1000);
        } else {
            return String.format(Locale.US, "%.0f meters", meters);
        }
    }

    // Method to update RecyclerView with predictions data
    public void showPredictionData(ArrayList<Predictions> predictionsList) {
        predictionsAdapter = new PredictionsAdapter(predictionsList, this);
        binding.predicRecyclerView.setAdapter(predictionsAdapter);
    }
}