package com.sneha.wesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private LinearLayout profileDetails;
    private ImageView editProfile, closeBtn;
    private EditText editName, editEmail;
    private TextView userName, userPhone, logoutBtn, avatarText;
    private Button saveProfile;

    private SharedPreferences prefs;
    private String phone; // logged in phone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); //

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Get logged-in phone
        phone = prefs.getString("loggedInPhone", "");

        // UI references
        profileDetails = findViewById(R.id.profileDetails);
        editProfile = findViewById(R.id.editProfile);
        closeBtn = findViewById(R.id.closeBtn);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        saveProfile = findViewById(R.id.saveProfile);
        logoutBtn = findViewById(R.id.logoutBtn);
        avatarText = findViewById(R.id.avatarText);

        // Keys for current phone
        String nameKey = "userName_" + phone;
        String emailKey = "userEmail_" + phone;

        // Initialize defaults if first time
        if (!prefs.contains(nameKey)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(nameKey, "Your Name");
            editor.putString(emailKey, "");
            editor.apply();
        }

        // Load saved profile for this phone
        String savedName = prefs.getString(nameKey, "Your Name");
        String savedEmail = prefs.getString(emailKey, "");

        // Set UI
        userPhone.setText(phone.isEmpty() ? "+91 xxxxxxxx" : phone);
        userName.setText(savedName);
        editName.setText(savedName);
        editEmail.setText(savedEmail);

        // Set avatar first letter
        if (!savedName.isEmpty()) {
            avatarText.setText(savedName.substring(0, 1).toUpperCase());
        } else {
            avatarText.setText("U"); // default = User
        }

        // Expand/collapse profile details
        editProfile.setOnClickListener(v -> {
            if (profileDetails.getVisibility() == View.GONE) {
                profileDetails.setVisibility(View.VISIBLE);
            } else {
                profileDetails.setVisibility(View.GONE);
            }
        });

        // Save profile for this phone
        saveProfile.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();

            if (newName.isEmpty()) {
                editName.setError("Enter name");
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(nameKey, newName);
            editor.putString(emailKey, newEmail);
            editor.apply();

            userName.setText(newName);
            userPhone.setText(phone);

            // Update avatar dynamically
            avatarText.setText(newName.substring(0, 1).toUpperCase());

            Toast.makeText(MenuActivity.this,
                    "Profile saved for " + phone, Toast.LENGTH_SHORT).show();

            profileDetails.setVisibility(View.GONE);
        });

        // Logout (clear only session, not profile data)
        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("loggedInPhone"); // just clear current session
            editor.apply();

            Toast.makeText(MenuActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            finish();
        });

        // Close profile
        closeBtn.setOnClickListener(v -> finish());
    }
}
