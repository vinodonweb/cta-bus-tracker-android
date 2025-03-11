package com.vinodsharma.ctabustracker.volley;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vinodsharma.ctabustracker.activities.MainActivity;
import com.vinodsharma.ctabustracker.activities.PredictionsActivity;
import com.vinodsharma.ctabustracker.models.Predictions;
import com.vinodsharma.ctabustracker.models.Routes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PredictionsVolley {
    private static final String vehicleUrl = "https://www.ctabustracker.com/bustime/api/v2/getpredictions";
    private static final String key = "QvkNjsEmmt3aYqvJ3hpLLq2yq";
    private static final String TAG = "PredictionsVolley";

    public static void downloadPredictions(PredictionsActivity predictionsActivityIn, String routeNumber, String stopId) {

        RequestQueue queue = Volley.newRequestQueue(predictionsActivityIn);

        Uri.Builder buildURL = Uri.parse(vehicleUrl).buildUpon();
        buildURL.appendQueryParameter("key", key);
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt", routeNumber);
        buildURL.appendQueryParameter("stpid", stopId);
        String urlToUse = buildURL.build().toString();
        Log.d(TAG, "downloadPredictions: " + urlToUse);

        Response.Listener<JSONObject> listener = response -> {
            try {
                handleSuccess(response.toString(), predictionsActivityIn);
            } catch (JSONException e) {
                Log.d(TAG, "downloadRoutes: " + e.getMessage());
            }
        };

        Response.ErrorListener error = PredictionsVolley::handleFail;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleSuccess(String responseText, PredictionsActivity predictionsActivity) throws JSONException {

        JSONObject response = new JSONObject(responseText);
        ArrayList<Predictions> predictionsList = new ArrayList<>();
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray predictions = jsonObject.getJSONArray("prd");
        for (int i = 0;  i < predictions.length(); i++){
            JSONObject prediction = predictions.getJSONObject(i);
            String vehicleID = prediction.getString("vid");
            String routeDirection = prediction.getString("rtdir");
            String routeDestination= prediction.getString("des");
            String routeTime = prediction.getString("prdtm");
            String delayed = prediction.getString("dly");
            String busArrivalTime = prediction.getString("prdctdn");

            Log.d(TAG, "predictions handle success: " + vehicleID + " " + routeDirection + " " + routeDestination + " " + routeTime);
            predictionsList.add(new Predictions(vehicleID, routeDirection, routeDestination, routeTime, delayed, busArrivalTime));
        }
        predictionsActivity.runOnUiThread(() -> predictionsActivity.showPredictionData(predictionsList));
    }

    private static void handleFail(VolleyError error) {
        Log.d(TAG, "handleFail: " + error.getMessage());
    }
}
