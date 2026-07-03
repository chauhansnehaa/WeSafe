package com.sneha.wesafe;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friends")
public class Friend {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;      // Friend's name
    public String phone;     // Friend's phone number
    public boolean isSOS;    // Flag if this contact is marked as SOS
    public String ownerPhone;


    public Friend(String name, String phone, boolean isSOS, String ownerPhone) {
        this.name = name;
        this.phone = phone;
        this.isSOS = isSOS;
        this.ownerPhone = ownerPhone;
    }

    public String getName() { return name; }

}

