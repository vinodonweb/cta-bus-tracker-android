package com.vinodsharma.ctabustracker.activities;

import static android.content.Intent.ACTION_VIEW;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.vinodsharma.ctabustracker.R;
import com.vinodsharma.ctabustracker.adapters.RoutesAdapter;
import com.vinodsharma.ctabustracker.ads.BannerViewListener;
import com.vinodsharma.ctabustracker.ads.UnityInitializationListener;
import com.vinodsharma.ctabustracker.databinding.ActivityMainBinding;
import com.vinodsharma.ctabustracker.models.Directions;
import com.vinodsharma.ctabustracker.models.Routes;
import com.vinodsharma.ctabustracker.volley.DirectionVolley;
import com.vinodsharma.ctabustracker.volley.RoutesVolley;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    //for routes menu
    private final ArrayList<Routes> routeListForMenu = new ArrayList<>();
    private View selectedView;

    //location access
    private static final int LOCATION_REQUEST = 111;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLocation;
    private RoutesAdapter routesAdapter;
    private List<Directions> directionsList = new ArrayList<>();

    //uniity add setup
    private static final String unityGameID = "5783381";
    private static final boolean testMode = false;
    private static final String bannerPlacement = "Banner_Android";
    private BannerView.IListener bannerListener;

    //splash screen
    private boolean keepOn = true;
    private static final long minSplashTime = 2000;
    private long startTime;

    //initial screen black
    public View overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //splash screen setup
        SplashScreen.installSplashScreen(this)
                .setKeepOnScreenCondition(
                        new SplashScreen.KeepOnScreenCondition() {
                            @Override
                            public boolean shouldKeepOnScreen() {
                                Log.d(TAG, "shouldKeepOnScreen: " + (System.currentTimeMillis() - startTime));
                                return keepOn || (System.currentTimeMillis() - startTime <= minSplashTime);
                            }
                        }
                );

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize overlay and set it visible
        overlay = binding.overlay;
        overlay.setVisibility(View.VISIBLE);

        EdgeToEdge.enable(this);
//        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //for the splash screen
        startTime = System.currentTimeMillis();

//        //call the volley to download the routes data
//        RoutesVolley.downloadRoutes(this);


        //location access
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();


//         Check network connectivity
        if (!isNetworkAvailable()) {
            showNoNetworkAlert();
            overlay.setVisibility(View.VISIBLE);
            return;
        }

        // Wait for splash screen to finish before checking permissions
        // Trigger after splash
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLocationPermission, minSplashTime);



        //banner ad initialization
        bannerListener = new BannerViewListener(this);
        // Initialize the SDK:
        UnityAds.initialize(this, unityGameID, testMode,
                new UnityInitializationListener(this));


        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(routesAdapter != null){
                    routesAdapter.filterRoutes(s.toString());
                }
            }
        });
    }

    //show log for if the cache is using
    public void showCacheLog(){
        Toast.makeText(this, "using CACHE data: ", Toast.LENGTH_LONG).show();
    }

    //error for the RoutesVolley for the splash screen
    public void acceptFail() {
        Log.d(TAG, "acceptFail: ");
        keepOn = false;
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

    //check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

            return networkCapabilities != null &&
                    (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        }

        return false;
    }

    //alert for the network unavailable
    public void showNoNetworkAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Bus Tracker - CTA")
                .setMessage("Unable to contact Bus Tracker API due to network problem. please check your network connection.")
                .setPositiveButton("OK", (dialog, which) -> finishAndRemoveTask())
                .setCancelable(false)
                .show();
    }

    //alert for the location unavailable
    private void showNoLocationAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Bus Tracker - CTA")
                .setMessage("Unable to determine device location. if this is an emulator, please set a location")
                .setPositiveButton("OK", (dialog, which) -> finishAndRemoveTask())
                .setCancelable(false)
                .show();
    }

    //popup menu
    @Override
    public void onClick(View v){
        Log.d("MainActivity", "Item clicked!");
        selectedView = v;
        int pos = binding.routeRecycler.getChildLayoutPosition(v);
        Routes r = routesAdapter.getRouteAtPosition(pos);
        directionsList.clear();


        // Call the volley to download the directions data
        DirectionVolley.downloadDirection(this, r.getrNum(), () -> {
            // This is a callback that will be called after directions are loaded
            runOnUiThread(() -> buildPopup(r));
        });

    }

    public void updateDirections(List<Directions> directions) {
        this.directionsList.clear();
        this.directionsList.addAll(directions);

//         verify directions are received
        Log.d("MainActivity", "Directions received: " + directionsList.size());
    }

    private void buildPopup(Routes r){
        if (directionsList.isEmpty()) {
            Log.d(TAG, "buildPopup: No directions found");
            return;
        }
        PopupMenu popupMenu = new PopupMenu(selectedView.getContext(), selectedView);

        //clear previous menu items
        popupMenu.getMenu().clear();


        for(int i = 0; i < directionsList.size(); i++){
            String direction = directionsList.get(i).getDir();
            popupMenu.getMenu().add(Menu.NONE, i + 1, Menu.NONE, direction);
        }


        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int selectedId = menuItem.getItemId();
            String selectedDirection = directionsList.get(selectedId - 1).getDir();

            //start StopActivity with route and direction
            Intent intent = new Intent(this, StopsActivity.class);
            intent.putExtra("ROUTE", r);
            intent.putExtra("DIRECTION", selectedDirection);
            intent.putExtra("USER_LOCATION", userLocation);
            startActivity(intent);

//            Toast.makeText(this, "Selected: " + r.getrName() + ": " + menuItem.getTitle() + selectedId, Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Selected: " + r.getrName() + ": " + selectedDirection, Toast.LENGTH_LONG).show();
            return true;
        });
        popupMenu.show();

    }


    //check for location permissions
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show rationale dialog (after first denial)
                showLocationAlert();
            } else {
                // First-time request or "Don't ask again" was checked
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST);
            }
        } else {
            // Permission already granted
            getUserLocation();
        }
    }

    //get user location
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            // Hide overlay and load data
                            overlay.setVisibility(View.GONE);
                            RoutesVolley.downloadRoutes(MainActivity.this); //call the volley to download the routes data
                            makeApiRequest(userLocation);
                        } else {
                            showNoLocationAlert();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Toast.makeText(this, "Error getting location: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showNoLocationAlert();
                    });
        }
    }

    //make initially bus api request
    private void makeApiRequest(LatLng location) {
        // Example of how you might use the location
        double latitude = location.latitude;
        double longitude = location.longitude;
        // Make your API request here with the coordinates
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation(); // Success: proceed
            } else {
                // Handle denial
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // User denied but didn't check "Don't ask again": show rationale
                    showLocationAlert();
                }
            }
        }
    }

    //show location alert
    private void showLocationAlert() {
        // Dismiss splash screen explicitly (if needed)
        keepOn = false;

        new AlertDialog.Builder(this)
                .setTitle("Location Access Required")
                .setMessage("This app needs your location to find nearby bus stops. " +
                        "Please allow location access.")
                .setIcon(R.drawable.bus_icon)
                .setPositiveButton("Allow", (dialog, which) ->
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_REQUEST
                        )
                )
                .setNegativeButton("Deny", (dialog, which) -> showExitLocationAlert())
                .setCancelable(false)
                .show();
    }

    //show exit location alert and exit the app
    private void showExitLocationAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Fine Accuracy Needed");
        alertDialogBuilder.setMessage("This application needs Fine Accuracy permission in order to determine the closet stops bus to your location." +
                " it will not function properly without it. please start the application again and allow this permission.");
        alertDialogBuilder.setIcon(R.drawable.bus_icon);
        alertDialogBuilder.setPositiveButton("Ok", (dialogInterface, which) ->
                finishAndRemoveTask()); //exit the app

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false); //this will prevent the user from dismissing the dialog by clicking outside the dialog
        alertDialog.setCanceledOnTouchOutside(false); //this will prevent the user from dismissing the dialog by clicking the back button
        alertDialog.show();
    }


    //handle the Routes data and display in the UI
    public void showRoutesData(ArrayList<Routes> routeList){
        this.routeListForMenu.clear();
        this.routeListForMenu.addAll(routeList);
        //display the routes data in the UI
        routesAdapter = new RoutesAdapter(routeList, this);
        binding.routeRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.routeRecycler.setAdapter(routesAdapter);
        keepOn = false;
    }

    public void attributionAlert(View view) {
        String url = "https://www.transitchicago.com/developers/bustracker/";
        String message = "CTA Bus Tracker data provided by Chicago Transit Authority\n" + url;

        SpannableString spannableMessage = new SpannableString(message);
        spannableMessage.setSpan(new URLSpan(url), message.indexOf(url), message.length(), 0);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.splash_logo)
                .setTitle("Bus Tracker - CTA")
                .setMessage(spannableMessage)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            TextView textView = ((AlertDialog) dialog).findViewById(android.R.id.message);
            if (textView != null) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setGravity(Gravity.CENTER);
            }
        });
        alertDialog.show();
    }
}