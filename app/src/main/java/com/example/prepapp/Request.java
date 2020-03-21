package com.example.prepapp;

import com.google.firebase.firestore.GeoPoint;

public class Request {
    private GeoPoint geoPoint;
    private String message;
    private User requestCreator;

    public Request() {
    }

    public Request(GeoPoint geoPoint, String message, User requestCreator) {
        this.geoPoint = geoPoint;
        this.message = message;
        this.requestCreator = requestCreator;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Request{" +
                "geoPoint=" + geoPoint +
                ", message='" + message + '\'' +
                ", requestCreator=" + requestCreator +
                '}';
    }

    public User getRequestCreator() {
        return requestCreator;
    }

    public void setRequestCreator(User requestCreator) {
        this.requestCreator = requestCreator;
    }
}
