package com.sneha.wesafe;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    // Foreground location + SMS permissions
    private static final String[] FOREGROUND_PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // Background location (API 29+)
    private static final String BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;

    // Notification permission (API 33+)
    private static final String NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS;

    // Foreground service location (API 34+)
    private static final String FOREGROUND_SERVICE_LOCATION = Manifest.permission.FOREGROUND_SERVICE_LOCATION;

    public static final int REQUEST_CODE = 100;

    /**
     * Check if all required permissions are granted
     */
    public static boolean hasAllPermissions(Activity activity) {
        // Check foreground permissions
        for (String permission : FOREGROUND_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Check background location separately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity, BACKGROUND_LOCATION_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(activity, NOTIFICATION_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Check foreground service location for Android 14+
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(activity, FOREGROUND_SERVICE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Request missing permissions (foreground + notification + Android 14+)
     */
    public static void requestAll(Activity activity) {
        List<String> missing = new ArrayList<>();

        // Check foreground permissions
        for (String permission : FOREGROUND_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(activity, NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            missing.add(NOTIFICATION_PERMISSION);
        }

        // Check foreground service location for Android 14+
        if (Build.VERSION.SDK_INT >= 34 &&
                ContextCompat.checkSelfPermission(activity, FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missing.add(FOREGROUND_SERVICE_LOCATION);
        }

        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(activity, missing.toArray(new String[0]), REQUEST_CODE);
        }
    }

    /**
     * Request background location separately (API 29+)
     */
    public static void requestBackgroundLocation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(activity, BACKGROUND_LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{BACKGROUND_LOCATION_PERMISSION}, REQUEST_CODE);
        }
    }

    /**
     * Check if background location is granted (API 29+)
     */
    public static boolean hasBackgroundLocation(Activity activity) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(activity, BACKGROUND_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }
}
