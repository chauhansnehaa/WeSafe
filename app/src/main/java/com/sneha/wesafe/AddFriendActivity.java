package com.sneha.wesafe;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class AddFriendActivity extends AppCompatActivity {

    private ImageView friendsGif;
    private EditText phoneInput, nameInput;
    private CheckBox cbMakeSOS;
    private Button addFriendBtn;

    private AppDatabase db;
    private String currentUserPhone; // logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // Init views
        friendsGif = findViewById(R.id.friendsGif);
        phoneInput = findViewById(R.id.phoneInput);
        nameInput = findViewById(R.id.nameInput);
        cbMakeSOS = findViewById(R.id.cbMakeSOS);
        addFriendBtn = findViewById(R.id.addFriendBtn);

        // Init database
        db = AppDatabase.getInstance(this);

        // Load GIF using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.friends_animation)
                .into(friendsGif);

        // Load current logged-in user phone (stored in SharedPreferences at login)
        currentUserPhone = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("loggedInPhone", "");

        // Disable button initially
        addFriendBtn.setEnabled(false);

        // Watch input fields
        TextWatcher inputWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateInputs(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        phoneInput.addTextChangedListener(inputWatcher);
        nameInput.addTextChangedListener(inputWatcher);

        // Button click -> Save in Room
        addFriendBtn.setOnClickListener(v -> saveFriend());
    }

    private void saveFriend() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        boolean isSOS = cbMakeSOS.isChecked();

        // Check duplicate for current user
        if (db.friendDao().getFriendByPhone(phone, currentUserPhone) != null) {
            Toast.makeText(this, "Friend already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save Friend with isSOS flag and ownerPhone
        db.friendDao().insert(new Friend(name, phone, isSOS, currentUserPhone));
        Toast.makeText(this, "Friend saved: " + name , Toast.LENGTH_SHORT).show();

        // Clear inputs
        nameInput.setText("");
        phoneInput.setText("");
        cbMakeSOS.setChecked(false);
    }

    private void validateInputs() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        addFriendBtn.setEnabled(!name.isEmpty() && phone.length() == 10);
    }
}
