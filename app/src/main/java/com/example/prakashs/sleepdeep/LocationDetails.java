package com.example.prakashs.sleepdeep;

/**
 * Created by prakash.s on 20/06/17.
 */

public class LocationDetails {


    String geofenceaddress;
    String geofenceId;
    int geofenceRadius;
    String geofenceLat;
    String geofenceLong;

    public LocationDetails(String geofenceaddress, String geofenceId) {
        this.geofenceaddress = geofenceaddress;
        this.geofenceId = geofenceId;
    }

    public LocationDetails(String geofenceaddress, String geofenceId, int geofenceRadius, String geofenceLat, String geofenceLong) {
        this.geofenceaddress = geofenceaddress;
        this.geofenceId = geofenceId;
        this.geofenceRadius = geofenceRadius;
        this.geofenceLat = geofenceLat;
        this.geofenceLong = geofenceLong;
    }

    //    Getters
    public String getGeofenceaddress() {
        return geofenceaddress;
    }

    public String getGeofenceId() {
        return geofenceId;
    }

    public int getGeofenceRadius() {
        return geofenceRadius;
    }

    public String getGeofenceLat() {
        return geofenceLat;
    }

    public String getGeofenceLong() {
        return geofenceLong;
    }
}
