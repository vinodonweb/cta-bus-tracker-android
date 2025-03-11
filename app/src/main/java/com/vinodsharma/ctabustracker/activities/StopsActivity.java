package com.vinodsharma.ctabustracker.activities;

import android.content.SharedPreferences;
import android.location.Location;
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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.vinodsharma.ctabustracker.R;
import com.vinodsharma.ctabustracker.adapters.StopsAdapter;
import com.vinodsharma.ctabustracker.ads.BannerViewListener;
import com.vinodsharma.ctabustracker.ads.UnityInitializationListener;
import com.vinodsharma.ctabustracker.databinding.ActivityStopsBinding;
import com.vinodsharma.ctabustracker.models.Alert;
import com.vinodsharma.ctabustracker.models.Routes;
import com.vinodsharma.ctabustracker.models.Stops;
import com.vinodsharma.ctabustracker.volley.StopsVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StopsActivity extends AppCompatActivity {
    ActivityStopsBinding binding;
    private static final String TAG = "StopsActivity";
    private final ArrayList<Stops> stopsList = new ArrayList<>();
    private StopsAdapter stopsAdapter;

    //uniity add setup
    private static final String unityGameID = "5783381";
    private static final boolean testMode = false;
    private static final String bannerPlacement = "Banner_android_3";
    private BannerView.IListener bannerListener;

    public LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityStopsBinding.inflate(getLayoutInflater());
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
        Routes routes = (Routes) getIntent().getSerializableExtra("ROUTE");
        String routeNumber = routes.getrNum();
        String routeName = routes.getrName();
        String direction = getIntent().getStringExtra("DIRECTION");
         userLocation = getIntent().getParcelableExtra("USER_LOCATION");

        // Set the title of the activity
        if(routes != null) {
            String finalString = "Route " + routes.getrNum() + " - " + routes.getrName();
            binding.routeTitle.setText(finalString);

            String boundText = direction + " Stops";
            binding.boundTitle.setText(boundText);
        }

        //setup adapter
        stopsAdapter = new StopsAdapter(stopsList, this, routeNumber, routeName, direction);
        binding.stopsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.stopsRecyclerView.setAdapter(stopsAdapter);

        //download the stops
        StopsVolley.downloadStops(this, routes.getrNum(), direction, stops -> runOnUiThread(() -> {
            stopsList.clear();
//            stopsList.addAll(stops);
            stopsList.addAll(filterdStops(stops));
            stopsAdapter.notifyDataSetChanged();

            // DEBUG: Verify data count
            Log.d(TAG, "Adapter item count: " + stopsAdapter.getItemCount());

        }));

        //fetch Alert of route
        fetchServiceAlerts(routeNumber);
    }

    private void fetchServiceAlerts(String routeId) {
        String url = "https://www.transitchicago.com/api/1.0/alerts.aspx?routeid=" + routeId + "&activeonly=true&outputType=JSON";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                this::parseAlertsResponse,
                error -> Log.e(TAG, "Error fetching alerts: ", error));

        request.setShouldCache(false); // Ensure fresh data
        Volley.newRequestQueue(this).add(request);
    }

    private void parseAlertsResponse(JSONObject response) {
        try {
            JSONObject ctaAlerts = response.getJSONObject("CTAAlerts");
            if (ctaAlerts.getInt("ErrorCode") == 0) {
                JSONArray alertsArray = ctaAlerts.optJSONArray("Alert");
                List<Alert> alerts = new ArrayList<>();

                if (alertsArray != null) {
                    for (int i = 0; i < alertsArray.length(); i++) {
                        JSONObject alertJson = alertsArray.getJSONObject(i);
                        alerts.add(new Alert(
                                alertJson.getString("AlertId"),
                                alertJson.getString("Headline"),
                                alertJson.optString("ShortDescription", "")
                        ));
                    }
                }

                processAlerts(alerts);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing alerts", e);
        }
    }

    private void processAlerts(List<Alert> alerts) {
        Set<String> shownAlertIds = getShownAlertIds();
        List<Alert> newAlerts = new ArrayList<>();

        for (Alert alert : alerts) {
            if (!shownAlertIds.contains(alert.getAlertId())) {
                newAlerts.add(alert);
            }
        }

        if (!newAlerts.isEmpty()) {
            showAlertsDialogs(newAlerts);
        }
    }

    private void showAlertsDialogs(List<Alert> alerts) {
        Iterator<Alert> iterator = alerts.iterator();
        showNextAlertDialog(iterator);
    }

    private void showNextAlertDialog(Iterator<Alert> iterator) {
        if (!iterator.hasNext()) return;

        Alert alert = iterator.next();
        markAlertAsShown(alert.getAlertId());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(alert.getHeadline())
                .setMessage(alert.getShortDescription())
                .setPositiveButton("OK", null)
                .create();

        dialog.setOnDismissListener(d -> showNextAlertDialog(iterator));
        dialog.show();
    }

    // SharedPreferences handling
    private Set<String> getShownAlertIds() {
        SharedPreferences prefs = getSharedPreferences("AlertsPrefs", MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet("shownAlerts", new HashSet<>()));
    }

    private void markAlertAsShown(String alertId) {
        SharedPreferences prefs = getSharedPreferences("AlertsPrefs", MODE_PRIVATE);
        Set<String> shownAlerts = new HashSet<>(getShownAlertIds());
        shownAlerts.add(alertId);
        prefs.edit().putStringSet("shownAlerts", shownAlerts).apply();
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


    private float getDistance(Stops stop) {
        try {
            double lat = Double.parseDouble(stop.getLat());
            double lon = Double.parseDouble(stop.getLon());
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.latitude,
                    userLocation.longitude,
                    lat,
                    lon,
                    results
            );
            return results[0];
        } catch (NumberFormatException e) {
            return Float.MAX_VALUE; // Return a large value if there is an error
        }
    }

    private ArrayList<Stops> filterdStops(ArrayList<Stops> stops) {
        ArrayList<Stops> filtered = new ArrayList<>();
        for (Stops stop : stops) {
            if (getDistance(stop) <= 1000) {
                filtered.add(stop);
            }
        }
        // Sort the filtered stops by distance
        filtered.sort((s1, s2) -> Float.compare(getDistance(s1), getDistance(s2)));
        return filtered;
    }

}