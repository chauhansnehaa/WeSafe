package com.sneha.wesafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FriendCheckboxAdapter extends RecyclerView.Adapter<FriendCheckboxAdapter.FriendViewHolder> {

    private List<String> friends;
    private List<String> selectedFriends = new ArrayList<>();

    public FriendCheckboxAdapter(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getSelectedFriends() {
        return selectedFriends;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_checkbox, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        String friendName = friends.get(position);
        holder.friendCheckbox.setText(friendName);

        holder.friendCheckbox.setOnCheckedChangeListener(null);
        holder.friendCheckbox.setChecked(selectedFriends.contains(friendName));

        holder.friendCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedFriends.contains(friendName)) {
                    selectedFriends.add(friendName);
                }
            } else {
                selectedFriends.remove(friendName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        CheckBox friendCheckbox;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendCheckbox = itemView.findViewById(R.id.friendCheckbox);
        }
    }
}
