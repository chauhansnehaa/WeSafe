package com.sneha.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EnterPasswordActivity extends AppCompatActivity {

    EditText pin1, pin2, pin3, pin4;
    Button continueBtn;
    FirebaseFirestore db;
    String phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_password_activity);

        db = FirebaseFirestore.getInstance();

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        continueBtn = findViewById(R.id.continueBtn);

        phone = getIntent().getStringExtra("phone");



        setPinFilters(pin1, pin2, pin3, pin4);
        setupPinAutoFocus();

        continueBtn.setOnClickListener(v -> {
            String pin = pin1.getText().toString().trim() +
                    pin2.getText().toString().trim() +
                    pin3.getText().toString().trim() +
                    pin4.getText().toString().trim();

            if (pin.length() < 4) {
                Toast.makeText(this, "Enter a valid 4-digit PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch PIN from Firestore and compare
            db.collection("users").document(phone)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String savedPin = documentSnapshot.getString("pin");
                            if (pin.equals(savedPin)) {

                                getSharedPreferences("user_prefs", MODE_PRIVATE)
                                        .edit()
                                        .putString("loggedInPhone", phone)
                                        .apply();

                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(EnterPasswordActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }
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

    private void setupPinAutoFocus() {
        pin1.addTextChangedListener(new PinTextWatcher(pin1, pin2));
        pin2.addTextChangedListener(new PinTextWatcher(pin2, pin3));
        pin3.addTextChangedListener(new PinTextWatcher(pin3, pin4));
        pin4.addTextChangedListener(new PinTextWatcher(pin4, null));
    }

    private static class PinTextWatcher implements TextWatcher {
        private final EditText currentView, nextView;

        PinTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            } else if (s.length() == 0 && currentView != null) {
                currentView.requestFocus();
            }
        }
    }
}
