package com.sneha.wesafe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnFilter;
    private boolean showSOSOnly = false;

    private FriendAdapter adapter;
    private AppDatabase db;
    private String currentUserPhone; // logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        recyclerView = findViewById(R.id.recyclerViewFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnFilter = findViewById(R.id.btnFilter);

        db = AppDatabase.getInstance(this);

        // Load current logged-in user phone from SharedPreferences
        currentUserPhone = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("loggedInPhone", "");


        adapter = new FriendAdapter(db.friendDao().getAllByOwner(currentUserPhone), friend -> {
            db.friendDao().delete(friend); loadFriends(); });
        recyclerView.setAdapter(adapter);

        // Filter button toggle
        btnFilter.setOnClickListener(v -> {
            showSOSOnly = !showSOSOnly;
            btnFilter.setText(showSOSOnly ? "Show All Friends" : "Show SOS Only");
            loadFriends();
        });

        // Load initial list
        loadFriends();
    }

    private void loadFriends() {
        List<Friend> friends = showSOSOnly
                ? db.friendDao().getSOSContactsByOwner(currentUserPhone)
                : db.friendDao().getAllByOwner(currentUserPhone);

        adapter.updateList(friends);
    }



}
