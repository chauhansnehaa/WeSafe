package com.sneha.wesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class HomeActivity extends AppCompatActivity {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker myLocationMarker;

    private Button trackMeButton;

    LinearLayout addFriendBtn , listFriendBtn, homeSosBtn ;

    private ImageView menuIcon;


    // Launcher for runtime permissions
    private final ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (granted) {
                    checkIfLocationEnabled();
                    startLocationUpdates();
                } else {
                    showToast("Location permission required");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName()); // Required by osmdroid
        setContentView(R.layout.home_activity);

        initViews();
        setupMap();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermissions();

        setupClickListeners();


    }

    // ---------- Initialization ----------
    private void initViews() {
        mapView = findViewById(R.id.mapView);
        trackMeButton = findViewById(R.id.trackMeButton);
        addFriendBtn = findViewById(R.id.addFriendSection);
        listFriendBtn = findViewById(R.id.ListFriendSection);
        homeSosBtn = findViewById(R.id.HomeSosSection);
        menuIcon = findViewById(R.id.menuIcon);

    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(5.0);
        mapView.getController().setCenter(new GeoPoint(20.5937, 78.9629)); // Default India center
    }

    private void setupClickListeners() {
        trackMeButton.setOnClickListener(v -> showToast("Track Me started"));
        menuIcon.setOnClickListener(v -> showToast("Menu clicked"));

        addFriendBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddFriendActivity.class);
            startActivity(intent);
        });

        listFriendBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FriendsListActivity.class);
            startActivity(intent);
        });

        homeSosBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SosSettingsActivity.class);
            startActivity(intent);
        });

        menuIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        trackMeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TrackMeBottomSheet.class);
            startActivity(intent);
        });
    }

    // ---------- Permissions ----------
    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            checkIfLocationEnabled();
            startLocationUpdates();
        }
    }

    private void checkIfLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            showToast("Please enable GPS for tracking");
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    // ---------- Location ----------
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                updateLocationOnMap(location);
            }
        }
    };

    private void updateLocationOnMap(Location location) {
        if (location == null) return;

        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (myLocationMarker == null) {
            myLocationMarker = new Marker(mapView);
            myLocationMarker.setTitle("You are here");
            myLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(myLocationMarker);
        }

        myLocationMarker.setPosition(geoPoint);
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(geoPoint);
        mapView.invalidate();
    }

    // ---------- Utility ----------
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
