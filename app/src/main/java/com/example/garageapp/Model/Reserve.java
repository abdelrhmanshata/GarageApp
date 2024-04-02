package com.example.garageapp.Model;

import android.annotation.SuppressLint;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Reserve implements Serializable {

    String ID, adminID;
    String userID, userName, userEmail;
    String parkingID, zoneID, slotID;
    String parking, zone, slot;

    String startTime, endTime;
    double slotPrice;

    public Reserve() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getParkingID() {
        return parkingID;
    }

    public void setParkingID(String parkingID) {
        this.parkingID = parkingID;
    }

    public String getZoneID() {
        return zoneID;
    }

    public void setZoneID(String zoneID) {
        this.zoneID = zoneID;
    }

    public String getSlotID() {
        return slotID;
    }

    public void setSlotID(String slotID) {
        this.slotID = slotID;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getSlotPrice() {
        return slotPrice;
    }

    public void setSlotPrice(double slotPrice) {
        this.slotPrice = slotPrice;
    }

    @SuppressLint("SimpleDateFormat")
    public String getDate(String time) {
        Date currentDate = new Date(Long.parseLong(time));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(currentDate);
    }

    @SuppressLint("SimpleDateFormat")
    public String getTime(String time) {
        Date currentDate = new Date(Long.parseLong(time));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
        return sdf.format(currentDate);
    }


    public int getBetweenTime(String startTime, String endTime) {
        // Calculate the time difference
        long timeDifference = Math.abs(Long.parseLong(endTime) - Long.parseLong(startTime));
        // Convert milliseconds to seconds, minutes, hours, etc. as needed
        long seconds = timeDifference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return Math.toIntExact(hours);
    }


}
