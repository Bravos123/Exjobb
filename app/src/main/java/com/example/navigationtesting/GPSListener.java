package com.example.navigationtesting;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSListener implements LocationListener {
    Callback callback;

    public GPSListener(Callback callbackCLass){
        callback = (Callback) callbackCLass;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
