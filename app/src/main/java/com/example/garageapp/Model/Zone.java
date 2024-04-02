package com.example.garageapp.Model;

import java.io.Serializable;

public class Zone implements Serializable {
    private String ID, ownerID, parkingID, zoneName;
    private int zoneFloor;

    public Zone() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getParkingID() {
        return parkingID;
    }

    public void setParkingID(String parkingID) {
        this.parkingID = parkingID;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public int getZoneFloor() {
        return zoneFloor;
    }

    public void setZoneFloor(int zoneFloor) {
        this.zoneFloor = zoneFloor;
    }
}
