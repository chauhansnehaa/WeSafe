package com.sneha.wesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String phone = prefs.getString("loggedInPhone", "");

            if (phone.isEmpty()) {
                // Not logged in → go to LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                // Already logged in → go to HomeActivity
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            }
            finish(); // prevent back to splash
        }, SPLASH_DELAY);
    }
}
