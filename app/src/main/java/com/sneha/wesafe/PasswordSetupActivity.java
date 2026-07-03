package com.sneha.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PasswordSetupActivity extends AppCompatActivity {

    EditText pin1, pin2, pin3, pin4;
    Button continueBtn;
    FirebaseFirestore db;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_password_activity);

        db = FirebaseFirestore.getInstance();


        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        continueBtn = findViewById(R.id.continueBtn);

        phone = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("loggedInPhone", null);

        // Restrict to 1 digit each
        setPinFilters(pin1, pin2, pin3, pin4);

        continueBtn.setOnClickListener(v -> {
            String pin = pin1.getText().toString().trim() +
                    pin2.getText().toString().trim() +
                    pin3.getText().toString().trim() +
                    pin4.getText().toString().trim();

            if (pin.length() < 4) {
                Toast.makeText(this, "Enter a valid 4-digit PIN", Toast.LENGTH_SHORT).show();
                return;
            }



            // Save PIN  in Firestore
            Map<String, Object> data = new HashMap<>();
            data.put("pin", pin);
//            if (phone != null) data.put("phone", phone);

            db.collection("users").document(phone)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "PIN saved!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PasswordSetupActivity.this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void setPinFilters(EditText... pins) {
        for (EditText pin : pins) {
            pin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        }
    }
}
