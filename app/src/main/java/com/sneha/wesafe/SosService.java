package com.sneha.wesafe;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

public class SosService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "SOSServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float accelCurrentValue;
    private float accelLastValue;
    private float shake;
    private int shakeCount = 0;

    private long lastSOS =0;
    private int triggerCount=3;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        accelCurrentValue = SensorManager.GRAVITY_EARTH;
        accelLastValue = SensorManager.GRAVITY_EARTH;
        shake = 0.0f;

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Foreground notification
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WE SAFE")
                .setContentText("SOS Service is running in background")
                .setSmallIcon(R.drawable.applogo)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        startForeground(NOTIFICATION_ID, builder.build());

        // Register accelerometer listener
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
//
//        // Always fetch latest trigger count
//        SharedPreferences prefs = getSharedPreferences("sos_prefs", MODE_PRIVATE);
//        triggerCount = prefs.getInt("shake_trigger_count", 3);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        accelLastValue = accelCurrentValue;
        accelCurrentValue = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = accelCurrentValue - accelLastValue;
        shake = shake * 0.9f + delta;

        // Always fetch latest trigger count
        SharedPreferences prefs = getSharedPreferences("sos_prefs", MODE_PRIVATE);
        triggerCount = prefs.getInt("shake_trigger_count", 3);


//        if (shake > 12) { // shake threshold
//            shakeCount++;
//            if (shakeCount >= triggerCount) {
//                shakeCount = 0; // reset
//                triggerSOS();

                if (shake > 12) { // threshold
                    shakeCount++;
                    if (shakeCount >= triggerCount) {
                        shakeCount = 0;

                        long now = System.currentTimeMillis();
                        if (now - lastSOS > 30_000) { // 30 sec debounce
                            lastSOS = now;
                            triggerSOS();
                        }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void triggerSOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(this, "Permissions missing. Cannot send SOS.", Toast.LENGTH_SHORT).show()
            );
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        sendSOSMessages(location);
                    } else {
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(this, "Unable to get location for SOS.", Toast.LENGTH_SHORT).show()
                        );
                    }
                })
                .addOnFailureListener(e -> new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                ));
    }

    private void sendSOSMessages(Location location) {
        String message = "🚨 SOS! I need help.\nMy location: "
                + "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();

        String currentUserPhone = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("loggedInPhone", "");

        new Thread(() -> {
            List<Friend> sosFriends = AppDatabase.getInstance(this)
                    .friendDao()
                    .getSOSContactsByOwner(currentUserPhone);

            SmsManager smsManager = SmsManager.getDefault();

            for (Friend friend : sosFriends) {
                String phoneNumber = "+91" + friend.phone;
                String Name = friend.name;

                try {
                    // Split message if too long
                    ArrayList<String> messageParts = smsManager.divideMessage(message);

                    // Sent & delivered PendingIntents
                    ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                    ArrayList<PendingIntent> deliveredIntents = new ArrayList<>();
                    for (int i = 0; i < messageParts.size(); i++) {
                        sentIntents.add(null);       // You can replace with real PendingIntent if needed
                        deliveredIntents.add(null);
                    }

                    // Send multipart SMS
                    smsManager.sendMultipartTextMessage(phoneNumber, null, messageParts, sentIntents, deliveredIntents);

                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, "SOS sent to " + Name, Toast.LENGTH_SHORT).show()
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, "Failed to send SOS to " + Name, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();

        // Vibrate
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else vibrator.vibrate(500);
        }

        // Play sound
        try {
            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationUri);
            ringtone.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "SOS Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
