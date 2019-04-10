package com.example.navigationtesting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.navigationtesting.SatelliteMVC.OnSatelliteControllerReadyCallback;
import com.example.navigationtesting.SatelliteMVC.Satellite;
import com.example.navigationtesting.SatelliteMVC.SatelliteController;
import com.example.navigationtesting.callbacks.Callback;
import com.example.navigationtesting.callbacks.CallbackBoolean;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnSatelliteControllerReadyCallback {
    private ArrayList<Satellite> satellites;
    private GoogleMap mMap;

    private SatelliteController satelliteController;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        timer = new Timer();
        satelliteController = new SatelliteController( this, 1);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }


    private void updateMapFromLocation(final Location l){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                LatLng newPosition = new LatLng(l.getLatitude(), l.getLongitude());
                mMap.addMarker(new MarkerOptions().position(newPosition).title("You are here"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
            }
        });

    }



    private void updateSatellites(){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //mMap.clear();
                Set<Integer> satellitesNoradId = satelliteController.getNORADSatelliteList();

                for(int norad : satellitesNoradId){
                    Location l = new Location("Dummy provider");
                    l.setLatitude(satelliteController.getSatelliteLatitude(norad));
                    l.setLongitude(satelliteController.getSatelliteLongitude(norad));
                    updateMapFromLocation(l);
                }

                timer.schedule(new TimerTask(){
                    @Override
                    public void run(){
                        updateSatellites();
                    }
                }, 500);
            }
        });

    }

    @Override
    public void callBack(boolean b) {
        Log.i("Project", "SatelliteController is now ready");
        updateSatellites();
    }
}
