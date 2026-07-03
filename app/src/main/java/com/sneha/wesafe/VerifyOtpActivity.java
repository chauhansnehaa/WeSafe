package com.sneha.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOtpActivity extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    Button submitOtpBtn;
    TextView otpMessageText, resendOtpText;
    String verificationId, phone;
    FirebaseAuth mAuth;
    PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification_activity);

        mAuth = FirebaseAuth.getInstance();

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        submitOtpBtn = findViewById(R.id.submitOtpBtn);
        otpMessageText = findViewById(R.id.otpMessageText);
        resendOtpText = findViewById(R.id.resendOtpText);
        resendOtpText.setPaintFlags(resendOtpText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        setOtpFilters(otp1, otp2, otp3, otp4, otp5, otp6);
        setupOtpInputs();

        verificationId = getIntent().getStringExtra("verificationId");
        phone = getIntent().getStringExtra("phone");
        resendToken = getIntent().getParcelableExtra("resendToken");

        otpMessageText.setText("A 6-digit OTP has been sent to " + phone);

        // Start countdown for resend OTP
        startResendCountdown();

        submitOtpBtn.setOnClickListener(v -> {
            String code = otp1.getText().toString().trim() +
                    otp2.getText().toString().trim() +
                    otp3.getText().toString().trim() +
                    otp4.getText().toString().trim() +
                    otp5.getText().toString().trim() +
                    otp6.getText().toString().trim();

            if (code.length() != 6) {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        });

        resendOtpText.setOnClickListener(v -> resendOtp());
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {


                            // Save phone in SharedPreferences
                            getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("loggedInPhone", phone) // optional
                                    .apply();


                        Intent intent = new Intent(VerifyOtpActivity.this, PasswordSetupActivity.class);
                        intent.putExtra("phone", phone);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendOtp() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(VerifyOtpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String newVerificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken newToken) {
                                verificationId = newVerificationId;
                                resendToken = newToken;
                                Toast.makeText(VerifyOtpActivity.this, "OTP resent successfully", Toast.LENGTH_SHORT).show();
                                startResendCountdown(); // restart countdown after resend
                            }
                        })
                        .setForceResendingToken(resendToken)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Countdown timer for resend OTP
    private void startResendCountdown() {
        resendOtpText.setEnabled(false);
        resendOtpText.setTextColor(Color.GRAY); // greyed out during countdown

        new CountDownTimer(60000, 1000) { // 60 seconds
            public void onTick(long millisUntilFinished) {
                resendOtpText.setText("Didn’t receive the OTP? Resend OTP in " + millisUntilFinished / 1000 + " seconds");
            }

            public void onFinish() {
                resendOtpText.setText("Didn’t receive the OTP? Resend OTP");
                resendOtpText.setEnabled(true);
                resendOtpText.setTextColor(Color.parseColor("#da65a5"));
            }
        }.start();
    }

    private void setOtpFilters(EditText... otps) {
        for (EditText otp : otps) otp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
    }

    private void setupOtpInputs() {
        otp1.addTextChangedListener(new OtpTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OtpTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OtpTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OtpTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(new OtpTextWatcher(otp5, otp6));
        otp6.addTextChangedListener(new OtpTextWatcher(otp6, null));
    }

    private static class OtpTextWatcher implements TextWatcher {
        private final EditText currentView, nextView;
        OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView; this.nextView = nextView;
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) nextView.requestFocus();
            else if (s.length() == 0 && currentView != null) currentView.requestFocus();
        }
    }
}
