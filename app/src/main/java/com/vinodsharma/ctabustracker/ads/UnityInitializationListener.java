package com.vinodsharma.ctabustracker.ads;

import android.app.Activity;
import android.util.Log;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.vinodsharma.ctabustracker.activities.MainActivity;
import com.vinodsharma.ctabustracker.activities.PredictionsActivity;
import com.vinodsharma.ctabustracker.activities.StopsActivity;

public class UnityInitializationListener implements IUnityAdsInitializationListener {

    private static final String TAG = "UnityInitializationList";
    private final Activity activity;

    public UnityInitializationListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onInitializationComplete() {
        Log.d(TAG, "onInitializationComplete: ");
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).showBanner();
        }

        if (activity instanceof StopsActivity) {
            ((StopsActivity) activity).showBanner();
        }

        if (activity instanceof PredictionsActivity) {
            ((PredictionsActivity) activity).showBanner();
        }
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError unityAdsInitializationError, String s) {
        Log.d(TAG, "onInitializationFailed: ");
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).initFailed(s);
        }

        if (activity instanceof StopsActivity) {
            ((StopsActivity) activity).initFailed(s);
        }

        if (activity instanceof PredictionsActivity) {
            ((PredictionsActivity) activity).initFailed(s);
        }
    }
}
