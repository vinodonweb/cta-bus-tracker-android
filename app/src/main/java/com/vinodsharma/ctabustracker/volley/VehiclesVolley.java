package com.vinodsharma.ctabustracker.volley;

import android.content.Context;
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
import com.vinodsharma.ctabustracker.models.Routes;
import com.vinodsharma.ctabustracker.models.Stops;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VehiclesVolley {
    private static final String vehicleUrl = "https://www.ctabustracker.com/bustime/api/v2/getvehicles";
    private static final String key = "QvkNjsEmmt3aYqvJ3hpLLq2yq";
    private static final String TAG = "VehiclesVolley";

    public interface VehicleCallback {
        void onVehicleReceived(double lat, double lon);
    }

    public static void downloadVehicle(Context context, String vehicleID, VehicleCallback callback) {

        RequestQueue queue = Volley.newRequestQueue(context);

        Uri.Builder buildURL = Uri.parse(vehicleUrl).buildUpon();
        buildURL.appendQueryParameter("key", key);
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("vid", vehicleID);
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                JSONObject jsonObject = response.getJSONObject("bustime-response");
                JSONArray vehicles = jsonObject.getJSONArray("vehicle");
                JSONObject vehicle = vehicles.getJSONObject(0);

                double lat = vehicle.getDouble("lat");
                double lon = vehicle.getDouble("lon");
                callback.onVehicleReceived(lat, lon);
            } catch (JSONException e) {
                Log.d(TAG, "Error parsing vehicle date: " + e.getMessage());
            }
        };

        Response.ErrorListener error = VehiclesVolley::handleFail;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

//    private static void handleSuccess(String responseText, MainActivity mainActivity) throws JSONException {
//
//        JSONObject response = new JSONObject(responseText);
//        ArrayList<String> vehicleLatLon = new ArrayList<>();
//        JSONObject jsonObject = response.getJSONObject("bustime-response");
//        JSONArray vehicles = jsonObject.getJSONArray("vehicle");
//        for (int i = 0;  i < vehicles.length(); i++){
//            JSONObject vehicle = vehicles.getJSONObject(i);
//            double lat = vehicle.getDouble("lat");
//            double lon = vehicle.getDouble("lon");
//
//            Log.d(TAG, "Vehicle Volley: " + lat + " " + lon);
//            vehicleLatLon.add(rNum, rName, rColor);
//        }
////        mainActivity.runOnUiThread(() -> mainActivity.showRoutesData(routesList));
//    }

    private static void handleFail(VolleyError error) {
        Log.d(TAG, "handleFail: " + error.getMessage());
    }
}
