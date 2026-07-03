package com.sneha.wesafe;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SosSettingsActivity extends AppCompatActivity {

    private Switch sosSwitch;
    private Spinner triggerSpinner;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_settings);

        sosSwitch = findViewById(R.id.sosSwitch);
        triggerSpinner = findViewById(R.id.triggerSpinner);
        prefs = getSharedPreferences("sos_prefs", MODE_PRIVATE);

        // Spinner values (shake trigger count)
        Integer[] counts = {3, 4, 5};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, counts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        triggerSpinner.setAdapter(adapter);

        // Load saved trigger count
        int savedCount = prefs.getInt("shake_trigger_count", 3);
        triggerSpinner.setSelection(adapter.getPosition(savedCount));

        // Load saved switch state
        boolean isServiceRunning = prefs.getBoolean("sos_enabled", false);
        sosSwitch.setChecked(isServiceRunning);

        // Switch listener
        sosSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                requestPermissionsAndStartService();
            } else {
                stopSosService();
            }
        });

        // Spinner listener
        triggerSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                int selectedCount = (Integer) parent.getItemAtPosition(position);
                prefs.edit().putInt("shake_trigger_count", selectedCount).apply();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    /**
     * Handles the permission flow:
     * 1. Request all foreground permissions
     * 2. Then request background location (API 29+)
     */
    private void requestPermissionsAndStartService() {
        if (PermissionHelper.hasAllPermissions(this)) {
            startSosService();
        } else {
            PermissionHelper.requestAll(this);
        }
    }

    private void startSosService() {

        // Check for battery optimization and request to ignore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            boolean ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());

            if (!ignoringBatteryOptimizations) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                Toast.makeText(this, "Please allow the app to ignore battery optimizations for reliable SOS alerts", Toast.LENGTH_LONG).show();
            }
        }


        Intent serviceIntent = new Intent(this, SosService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        prefs.edit().putBoolean("sos_enabled", true).apply();
        Toast.makeText(this, "SOS Service Started", Toast.LENGTH_SHORT).show();
    }

    private void stopSosService() {
        Intent serviceIntent = new Intent(this, SosService.class);
        stopService(serviceIntent);

        prefs.edit().putBoolean("sos_enabled", false).apply();
        Toast.makeText(this, "SOS Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Foreground permissions granted, now request background location if needed
                if (!PermissionHelper.hasBackgroundLocation(this)) {
                    showBackgroundLocationExplanation();
                } else {
                    startSosService();
                    sosSwitch.setChecked(true);
                }
            } else {
                Toast.makeText(this, "Permissions required for SOS service", Toast.LENGTH_SHORT).show();
                sosSwitch.setChecked(false);
            }
        }
    }

    /**
     * Show explanation before requesting background location
     */
    private void showBackgroundLocationExplanation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            new AlertDialog.Builder(this)
                    .setTitle("Background Location Required")
                    .setMessage("To send SOS alerts even when the app is not open, we need access to your location in the background. Your location will only be used for emergency alerts.")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionHelper.requestBackgroundLocation(SosSettingsActivity.this);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SosSettingsActivity.this,
                                    "Background location denied. SOS may not work fully.", Toast.LENGTH_SHORT).show();
                            sosSwitch.setChecked(false);
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            startSosService();
            sosSwitch.setChecked(true);
        }
    }
}
