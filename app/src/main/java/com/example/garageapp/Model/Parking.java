package com.example.garageapp.Model;

import java.io.Serializable;

public class Parking implements Serializable {

    private String ID, ownerID, parkingName, parkingLocation, parkingLocationDescription;
    private String longitude;
    private String latitude;

    public Parking() {
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

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public String getParkingLocation() {
        return parkingLocation;
    }

    public void setParkingLocation(String parkingLocation) {
        this.parkingLocation = parkingLocation;
    }

    public String getParkingLocationDescription() {
        return parkingLocationDescription;
    }

    public void setParkingLocationDescription(String parkingLocationDescription) {
        this.parkingLocationDescription = parkingLocationDescription;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
