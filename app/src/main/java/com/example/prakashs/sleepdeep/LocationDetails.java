package com.example.prakashs.sleepdeep;

/**
 * Created by prakash.s on 20/06/17.
 */

public class LocationDetails {


    String geofenceaddress;
    String geofenceId;

    public LocationDetails(String geofenceaddress, String geofenceId) {
        this.geofenceaddress = geofenceaddress;
        this.geofenceId = geofenceId;
    }

    //    Getters
    public String getGeofenceaddress() {
        return geofenceaddress;
    }

    public String getGeofenceId() {
        return geofenceId;
    }


}
