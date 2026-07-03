package com.sneha.wesafe;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FriendDao {

    @Insert
    void insert(Friend friend);

    @Delete
    void delete(Friend friend);

    // Fetch all contacts of current user
    @Query("SELECT * FROM friends WHERE ownerPhone = :ownerPhone")
    List<Friend> getAllByOwner(String ownerPhone);

    // Fetch SOS contacts of current user by UID
    @Query("SELECT * FROM friends WHERE isSOS = 1 AND ownerPhone = :ownerPhone")
    List<Friend> getSOSContactsByOwner(String ownerPhone);

    // Check if a phone already exists for current user by UID
    @Query("SELECT * FROM friends WHERE phone = :phone AND ownerPhone = :ownerPhone LIMIT 1")
    Friend getFriendByPhone(String phone, String ownerPhone);
}
