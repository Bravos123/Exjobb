package com.example.navigationtesting.Satellite;

import com.google.android.gms.maps.model.LatLng;

public class LatLngAlt {
    private double latitude;
    private double longitude;
    private double altitude;

    public LatLngAlt(double latitude, double longitude, double altitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }


    public double latitude(){
        return latitude;
    }

    public double longitude(){
        return longitude;
    }

    public double altitude(){
        return altitude;
    }

    public LatLng toLatLng(){;
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString(){
        return "latitude: "+latitude
                +"\nlongitude: "+longitude
                +"\naltitude: "+altitude
                +"\n";
    }
}
