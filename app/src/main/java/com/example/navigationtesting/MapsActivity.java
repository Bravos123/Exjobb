package com.example.navigationtesting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.renderscript.Float3;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.navigationtesting.SatelliteMVC.Satellite;
import com.example.navigationtesting.SatelliteMVC.SatelliteController;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;


import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Callback<ArrayList<Satellite>>{
    private ArrayList<Satellite> satellites;
    private GoogleMap mMap;

    private SatelliteController satelliteController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        satelliteController = new SatelliteController( this);
        //satelliteController.retrieveSatellites();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }


    private void updateMapFromLocation(Location l){
        if(l == null){
            Log.d("Project", "updateMapFromLocation Location is null");
        }

        LatLng newPosition = new LatLng(l.getLatitude(), l.getLongitude());
        mMap.addMarker(new MarkerOptions().position(newPosition).title("You are here"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
    }




    @Override
    public void callBack(String name, ArrayList<Satellite> satts) {
        /*Make satts a bool that says if satelliteController is ready so this class can get the satellite date from that class instead through some kind of getter*/
        satellites = satts;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.clear();

                for(Satellite s : satellites){
                    Location l = new Location("Dummy provider");
                    l.setLatitude(s.getLatitude());
                    l.setLongitude(s.getLongitude());
                    updateMapFromLocation(l);
                }
            }
        });
    }
}
