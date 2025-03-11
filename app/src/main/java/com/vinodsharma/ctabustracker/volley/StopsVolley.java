package com.vinodsharma.ctabustracker.volley;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vinodsharma.ctabustracker.activities.MainActivity;
import com.vinodsharma.ctabustracker.activities.StopsActivity;
import com.vinodsharma.ctabustracker.models.Directions;
import com.vinodsharma.ctabustracker.models.Stops;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class StopsVolley {
    private static final String stopUrl = "https://www.ctabustracker.com/bustime/api/v2/getstops";
    private static final String key = "QvkNjsEmmt3aYqvJ3hpLLq2yq";
    private static final String TAG = "StopVolley";

    //cache content for 24 hours
    private static final String CACHE_PREFIX_STOPS = "stops_cache_";
    private static final String CACHE_TIME_PREFIX_STOPS = "stops_cache_time_";
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours


    public static void downloadStops(StopsActivity stopsActivity, String routeNumber, String direction, StopsCallback callback) {

        // Generate unique cache key for this route+direction
        String cacheKey = CACHE_PREFIX_STOPS + routeNumber + "_" + direction;
        String cacheTimeKey = CACHE_TIME_PREFIX_STOPS + routeNumber + "_" + direction;

        // Check cache first
        if (isCacheValid(stopsActivity, cacheTimeKey)) {
            String cachedResponse = readFromCache(stopsActivity, cacheKey);
            if (cachedResponse != null) {
                Log.d(TAG, "Using CACHED stops data");
                try {
                    ArrayList<Stops> stops = handleSuccess(cachedResponse);
                    callback.onStopsReceived(stops);
                    return;
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing cached stops", e);
                }
            }
        }

    // Proceed with network request if no valid cache
        if (!isNetworkAvailable(stopsActivity)) {
            callback.onStopsReceived(new ArrayList<>());
            return;
        }


        RequestQueue queue = Volley.newRequestQueue(stopsActivity);

        Uri.Builder buildURL = Uri.parse(stopUrl).buildUpon();
        buildURL.appendQueryParameter("key", key);
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt", routeNumber);
        buildURL.appendQueryParameter("dir", direction);
        String urlToUse = buildURL.build().toString();

        Log.d(TAG, "downloadStops: " + urlToUse);


        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, urlToUse, null,
                response -> {
                    String responseString = response.toString();
                    try {
                        ArrayList<Stops> stops = handleSuccess(responseString);
                        saveToCache(stopsActivity, cacheKey, cacheTimeKey, responseString);
                        callback.onStopsReceived(stops);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing stops response", e);
                    }
                },
                error2 -> {
                    Log.e(TAG, "Stops request failed", error2);
                    callback.onStopsReceived(new ArrayList<>());
                }
        );
        queue.add(jsonObjectRequest);
    }

    private static boolean isCacheValid(Context context, String timeKey) {
        SharedPreferences prefs = context.getSharedPreferences("StopsCache", Context.MODE_PRIVATE);
        long lastCacheTime = prefs.getLong(timeKey, 0);
        return (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION;
    }

    private static String readFromCache(Context context, String cacheKey) {
        try {
            FileInputStream fis = context.openFileInput(cacheKey);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static void saveToCache(Context context, String cacheKey,
                                    String timeKey, String response) {
        // Save timestamp
        SharedPreferences prefs = context.getSharedPreferences("StopsCache", Context.MODE_PRIVATE);
        prefs.edit().putLong(timeKey, System.currentTimeMillis()).apply();

        // Save response
        try {
            FileOutputStream fos = context.openFileOutput(cacheKey, Context.MODE_PRIVATE);
            fos.write(response.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to cache stops data", e);
        }
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }


    private static ArrayList<Stops> handleSuccess(String responseText) throws JSONException {
        JSONObject response = new JSONObject(responseText);
        ArrayList<Stops> stopsArrayList = new ArrayList<>();
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray direction = jsonObject.getJSONArray("stops");

        for (int i = 0;  i < direction.length(); i++){
            JSONObject stop = direction.getJSONObject(i);
            String stpid = stop.getString("stpid");
            String stpnm = stop.getString("stpnm");
            String lat = stop.getString("lat");
            String lon = stop.getString("lon");

            Log.d(TAG, "Stops data: " + stpid + " " + stpnm + " " + lat + " " + lon);
            stopsArrayList.add(new Stops(stpid, stpnm, lat, lon));

        }
        Log.d(TAG, "Parsed stops count: " + stopsArrayList.size());

        return stopsArrayList;
    }

    private static void handleFail(VolleyError error) {
        Log.d(TAG, "handleFail: " + error.getMessage());
    }

    public interface StopsCallback {
        void onStopsReceived(ArrayList<Stops> stops);
    }
}