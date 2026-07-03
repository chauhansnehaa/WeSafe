package com.sneha.wesafe;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<Friend> friends;
    private final OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(Friend friend);
    }

    public FriendAdapter(List<Friend> friends, OnDeleteClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    // Update list using DiffUtil for smooth updates
    public void updateList(List<Friend> newFriends) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return friends.size();
            }

            @Override
            public int getNewListSize() {
                return newFriends.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return friends.get(oldItemPosition).phone.equals(newFriends.get(newItemPosition).phone);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Friend oldFriend = friends.get(oldItemPosition);
                Friend newFriend = newFriends.get(newItemPosition);
                return oldFriend.name.equals(newFriend.name)
                        && oldFriend.phone.equals(newFriend.phone)
                        && oldFriend.isSOS == newFriend.isSOS;
            }
        });

        this.friends = newFriends;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);

        holder.tvName.setText(friend.name);
        holder.tvPhone.setText(friend.phone);

        // Avatar first letter
        holder.tvAvatar.setText(friend.name != null && !friend.name.isEmpty()
                ? friend.name.substring(0, 1).toUpperCase()
                : "?");

        // Circular avatar color
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(friend.isSOS
                ? holder.itemView.getResources().getColor(android.R.color.holo_red_light)
                : holder.itemView.getResources().getColor(android.R.color.darker_gray));
        holder.tvAvatar.setBackground(bg);

        // Show badge only for SOS
        holder.tvSOSBadge.setVisibility(friend.isSOS ? View.VISIBLE : View.GONE);

        // Delete action
        holder.btnRemove.setOnClickListener(v -> listener.onDelete(friend));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAvatar, tvSOSBadge;
        Button btnRemove;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvSOSBadge = itemView.findViewById(R.id.tvSOSBadge); // new
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
    }
