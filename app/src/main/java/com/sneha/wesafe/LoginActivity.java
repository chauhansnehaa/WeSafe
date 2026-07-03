package com.sneha.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    EditText phoneInput;
    Button continueBtn;
    RelativeLayout googleBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String verificationId;
    String fullPhone;
    PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        phoneInput = findViewById(R.id.phoneInput);
        continueBtn = findViewById(R.id.continueBtn);
        googleBtn = findViewById(R.id.googleBtn);

        continueBtn.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();

            if (phone.isEmpty() || phone.length() != 10) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            } else {
                fullPhone = "+91" + phone; // Add country code
                checkUserExists(fullPhone);
            }
        });

        googleBtn.setOnClickListener(v ->
                Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show());
    }

    // Check if user exists in Firestore
    private void checkUserExists(String phoneNumber) {
        db.collection("users").document(phoneNumber)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Existing user → password flow
                        Intent intent = new Intent(LoginActivity.this, EnterPasswordActivity.class);
                        intent.putExtra("phone", phoneNumber);
                        startActivity(intent);
                        finish();
                    } else {
                        // New user → send OTP
                        sendVerificationCode(phoneNumber);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Send OTP for new users
    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                // Auto-verification
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                super.onCodeSent(s, token);
                                verificationId = s;
                                resendToken = token;
                                showOtpSentToast();

                                // Go to OTP Activity
                                Intent intent = new Intent(LoginActivity.this, VerifyOtpActivity.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("resendToken", resendToken);
                                intent.putExtra("phone", fullPhone);
                                startActivity(intent);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOtpSentToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    // Handle auto verification
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();

                        if (user != null) {
                            String loggedInPhone = user.getPhoneNumber();


                            // ✅ Save  logged-in number to SharedPreferences
                            getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("loggedInPhone", loggedInPhone)
                                    .apply();
                        }

                        Toast.makeText(LoginActivity.this, "Phone Verified!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
