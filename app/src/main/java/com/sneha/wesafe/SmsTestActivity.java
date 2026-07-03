package com.sneha.wesafe;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class SmsTestActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_test);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button sendBtn = findViewById(R.id.sendBtn);

        sendBtn.setOnClickListener(v -> {
            // Check SMS & Location permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            } else {
                sendLocationSms();
            }
        });
    }

    private void sendLocationSms() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        sendSms(location);
                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendSms(Location location) {
        String phoneNumber = "+919372837235"; // Replace with your number
        String message = "SOS! My location: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();

        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentPI = PendingIntent.getBroadcast(
                this, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
            Toast.makeText(this, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int res : grantResults) if (res != PackageManager.PERMISSION_GRANTED) granted = false;

            if (granted) {
                Toast.makeText(this, "Permissions granted, try again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
