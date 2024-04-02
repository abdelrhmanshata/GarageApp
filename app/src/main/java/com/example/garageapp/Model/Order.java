package com.example.garageapp.Model;

import java.io.Serializable;

public class Order implements Serializable {

    String ID, userID, userName, userEmail, date, status;
    String CardholderName, CardNumber, Cvv, ExpirationMonth, ExpirationYear;
    String phoneNumber, Location, LocationDescription;

    public Order() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCardholderName() {
        return CardholderName;
    }

    public void setCardholderName(String cardholderName) {
        CardholderName = cardholderName;
    }

    public String getCardNumber() {
        return CardNumber;
    }

    public void setCardNumber(String cardNumber) {
        CardNumber = cardNumber;
    }

    public String getCvv() {
        return Cvv;
    }

    public void setCvv(String cvv) {
        Cvv = cvv;
    }

    public String getExpirationMonth() {
        return ExpirationMonth;
    }

    public void setExpirationMonth(String expirationMonth) {
        ExpirationMonth = expirationMonth;
    }

    public String getExpirationYear() {
        return ExpirationYear;
    }

    public void setExpirationYear(String expirationYear) {
        ExpirationYear = expirationYear;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getLocationDescription() {
        return LocationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        LocationDescription = locationDescription;
    }
}
