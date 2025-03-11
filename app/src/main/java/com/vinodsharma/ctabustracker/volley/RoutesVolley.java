package com.vinodsharma.ctabustracker.volley;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
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
import com.vinodsharma.ctabustracker.models.Routes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RoutesVolley {

    private static final String vehicleUrl = "https://www.ctabustracker.com/bustime/api/v2/getroutes";
    private static final String TAG = "RoutesVolley";
    private static final String key = "QvkNjsEmmt3aYqvJ3hpLLq2yq";

    //content cached
     private static final String CACHE_FILE_NAME = "routes_cache.json";
    private static final String CACHE_PREFS_NAME = "CachePrefs";
    private static final String CACHE_TIME_KEY = "routes_cache_time";
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours in milliseconds


    public static void downloadRoutes(MainActivity mainActivityIn) {

        // Check if cache is valid and use it
        if (isCacheValid(mainActivityIn)) {
            String cachedResponse = readFromCache(mainActivityIn);
            if (cachedResponse != null) {
                Log.d(TAG, "using CACHE data: ");
                mainActivityIn.showCacheLog();
                try {
                    handleSuccess(cachedResponse, mainActivityIn);
                    return;
                } catch (JSONException e) {
                    Log.e(TAG, "Error processing cached data", e);
                    // Proceed to fetch fresh data
                }
            }
        }

        // Check network availability before making the request
        if (!isNetworkAvailable(mainActivityIn)) {
            mainActivityIn.runOnUiThread(() -> {
                mainActivityIn.showNoNetworkAlert();
                mainActivityIn.overlay.setVisibility(View.VISIBLE);
            });
            return;
        }

        //proceed to fetch fresh data
        RequestQueue queue = Volley.newRequestQueue(mainActivityIn);

        Uri.Builder buildURL = Uri.parse(vehicleUrl).buildUpon();
        buildURL.appendQueryParameter("key", key);
        buildURL.appendQueryParameter("format", "json");
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            String responseString = response.toString();
            try {
                handleSuccess(responseString, mainActivityIn);
                saveToCache(mainActivityIn, responseString);
            } catch (JSONException e) {
                Log.d(TAG, "downloadRoutes: " + e.getMessage());
            }
        };

        Response.ErrorListener error = RoutesVolley::handleFailedToLoadVolley;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static boolean isCacheValid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE);
        long lastCacheTime = prefs.getLong(CACHE_TIME_KEY, 0);
        return (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION;
    }

    private static String readFromCache(Context context) {
        FileInputStream fis = null;
        try {
            File cacheFile = new File(context.getFilesDir(), CACHE_FILE_NAME);
            if (!cacheFile.exists()) return null;
            fis = new FileInputStream(cacheFile);
            byte[] data = new byte[(int) cacheFile.length()];
            int bytesRead = fis.read(data);
            if (bytesRead != data.length) {
                Log.w(TAG, "readFromCache: Incomplete read");
            }
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "readFromCache: Error reading cache", e);
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "readFromCache: Error closing stream", e);
                }
            }
        }
    }

        private static void saveToCache(Context context, String response) {
        SharedPreferences prefs = context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(CACHE_TIME_KEY, System.currentTimeMillis()).apply();

        FileOutputStream fos = null;
        try {
            File cacheFile = new File(context.getFilesDir(), CACHE_FILE_NAME);
            fos = new FileOutputStream(cacheFile);
            fos.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e(TAG, "saveToCache: Error writing to cache", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "saveToCache: Error closing stream", e);
                }
            }
        }
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private static void handleSuccess(String responseText, MainActivity mainActivity) throws JSONException {

        JSONObject response = new JSONObject(responseText);
        ArrayList<Routes> routesList = new ArrayList<>();
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray routes = jsonObject.getJSONArray("routes");
        for (int i = 0;  i < routes.length(); i++){
            JSONObject route = routes.getJSONObject(i);
            String rNum = route.getString("rt");
            String rName = route.getString("rtnm");
            String rColor = route.getString("rtclr");

            Log.d(TAG, "handleSuccess: " + rNum + ", " + rName + ", " + rColor);
            routesList.add(new Routes(rNum, rName, rColor));
        }
            mainActivity.runOnUiThread(() -> mainActivity.showRoutesData(routesList));
    }

    private static void acceptFail(VolleyError error, MainActivity mainActivity) {
        Log.d(TAG, "handleFail: " + error.getMessage());
        mainActivity.runOnUiThread(() -> mainActivity.acceptFail());
    }

    private static void handleFailedToLoadVolley(VolleyError error) {
        Log.d(TAG, "handleFailed to load volley content: " + error.getMessage());
    }
}
