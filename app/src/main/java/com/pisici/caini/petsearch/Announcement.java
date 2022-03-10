package com.pisici.caini.petsearch;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;

public class Announcement {
    String petId;
    String ownerId;
    String petName;
    String ownerPhone;
    String bounty;
    String ownerName;
    String date;
    String location;

    public Announcement(String petId, String ownerId, String petName, String ownerPhone, String bounty, String ownerName, String date, String location) {
        this.petId = petId;
        this.ownerId = ownerId;
        this.petName = petName;
        this.ownerPhone = ownerPhone;
        this.bounty = bounty;
        this.ownerName = ownerName;
        this.date = date;
        this.location = location;
    }
    public Announcement()
    {}

    public static ArrayList<Announcement> filter(DataSnapshot dataSnapshot) {

        ArrayList<Announcement> arr = new ArrayList<>();

        Log.d("BUGS", "aici incepe sa filtreze");
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Announcement curr = ds.getValue(Announcement.class);
            arr.add(curr);
        }

        return arr;
    }

    public String getPetId() {
        return petId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getPetName() {
        return petName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public String getBounty() {
        return bounty;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public void setBounty(String bounty) {
        this.bounty = bounty;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
