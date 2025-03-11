package com.vinodsharma.ctabustracker.ads;

import android.app.Activity;
import android.util.Log;

import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.vinodsharma.ctabustracker.activities.MainActivity;

import kotlinx.coroutines.channels.ActorKt;


public class BannerViewListener implements BannerView.IListener {

    private static final String TAG = "BannerViewListener";
    private final Activity activity;

    public BannerViewListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onBannerLoaded(BannerView bannerView) {
        Log.d(TAG, "onBannerLoaded: ");
    }

    @Override
    public void onBannerShown(BannerView bannerAdView) {
        Log.d(TAG, "onBannerShown: ");
    }

    @Override
    public void onBannerClick(BannerView bannerView) {
        Log.d(TAG, "onBannerClick: ");
    }

    @Override
    public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo bannerErrorInfo) {
        Log.d(TAG, "onBannerFailedToLoad: ");
//        activity.loadFailed(bannerErrorInfo.errorMessage);

    }

    @Override
    public void onBannerLeftApplication(BannerView bannerView) {
        Log.d(TAG, "onBannerLeftApplication: ");
    }
}
