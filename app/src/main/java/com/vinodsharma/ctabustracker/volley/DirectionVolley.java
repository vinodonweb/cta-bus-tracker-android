package com.vinodsharma.ctabustracker.volley;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vinodsharma.ctabustracker.activities.MainActivity;
import com.vinodsharma.ctabustracker.models.Directions;
import com.vinodsharma.ctabustracker.models.Routes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DirectionVolley {
    private static final String vehicleUrl = "https://www.ctabustracker.com/bustime/api/v2/getdirections";
    private static final String key = "QvkNjsEmmt3aYqvJ3hpLLq2yq";
    private static final String TAG = "DirectionVolley";

    private static final String CACHE_PREFIX_DIR = "dir_cache_";
    private static final String CACHE_TIME_PREFIX_DIR = "dir_cache_time_";
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours


    //Runnable onCompleteCallback
    public static void downloadDirection(MainActivity mainActivityIn, String routeNumber, Runnable onCompleteCallback) {

        String cacheKey = CACHE_PREFIX_DIR + routeNumber;
        String cacheTimeKey = CACHE_TIME_PREFIX_DIR + routeNumber;

        // Check cache first
        if (isCacheValid(mainActivityIn, cacheTimeKey)) {
            String cachedResponse = readFromCache(mainActivityIn, cacheKey);
            if (cachedResponse != null) {
                Log.d(TAG, "Using CACHED directions");
                try {
                    handleSuccess(cachedResponse, mainActivityIn);
                    onCompleteCallback.run();
                    return;
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing cached directions", e);
                }
            }
        }

        // Proceed with network request if no valid cache
        if (!isNetworkAvailable(mainActivityIn)) {
            onCompleteCallback.run();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(mainActivityIn);

        Uri.Builder buildURL = Uri.parse(vehicleUrl).buildUpon();
        buildURL.appendQueryParameter("key", key);
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt", routeNumber);
        String urlToUse = buildURL.build().toString();

        Log.d(TAG, "downloadDirection: " + urlToUse);


        Response.ErrorListener error = DirectionVolley::handleFail;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, urlToUse, null,
                response -> {
                    String responseString = response.toString();
                    try {
                        handleSuccess(responseString, mainActivityIn);
                        saveToCache(mainActivityIn, cacheKey, cacheTimeKey, responseString);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing directions", e);
                    }
                    onCompleteCallback.run();
                },
                e -> {
                    Log.e(TAG, "Directions request failed", e);
                    onCompleteCallback.run();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private static boolean isCacheValid(Context context, String timeKey) {
        SharedPreferences prefs = context.getSharedPreferences("DirCache", Context.MODE_PRIVATE);
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

    private static void handleSuccess(String responseText, MainActivity mainActivity) throws JSONException {
        JSONObject response = new JSONObject(responseText);
        ArrayList<Directions> directionsList = new ArrayList<>();
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray direction = jsonObject.getJSONArray("directions");

        for (int i = 0;  i < direction.length(); i++){
            JSONObject direc = direction.getJSONObject(i);
            String dir = direc.getString("dir");

            Log.d(TAG, "direction: " + dir);
            directionsList.add(new Directions(dir));

        }

        // Pass the directionsList back to MainActivity
        mainActivity.runOnUiThread(() -> mainActivity.updateDirections(directionsList));
    }

    private static void handleFail(VolleyError error) {
        Log.d(TAG, "handleFail: " + error.getMessage());
    }
}
