package com.example.navigationtesting.MapOfSatellitePositions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.navigationtesting.R;
import com.example.navigationtesting.SatelliteMVC.Satellite;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Request;
import okhttp3.Response;

public class MapsShowPositionOfSatellites extends FragmentActivity implements OnMapReadyCallback, OnSatellitePositionControllerReadyCallback {
    private GoogleMap mMap;
    private SatellitePositionController satelliteController;
    private Timer timer;

    private HashMap<String, ArrayList<LatLng>> satellitesPolylines;

    private boolean saveCoordsForPolyline = true;

    private HashMap<String, MarkerOptions> satelliteMarkerBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        satelliteMarkerBuffer = new HashMap<String, MarkerOptions>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        satellitesPolylines = new HashMap<String, ArrayList<LatLng>>();
        timer = new Timer();
        satelliteController = new SatellitePositionController( this);
        satelliteController.initialize();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }


    private void updateSatelliteLocation(final Location l, String noradId){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                //Log.i("Project", "Latitude: "+l.getLatitude()+"   Longitude: "+l.getLongitude());
                LatLng newPosition = new LatLng(l.getLatitude(), l.getLongitude());
                if(satelliteMarkerBuffer.get(noradId) == null){
                    MarkerOptions sattMarker = new MarkerOptions();
                    sattMarker.title("NORAD ID:"+noradId);
                    sattMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.satellite));
                    sattMarker.anchor(0.5f, 0.5f);
                    sattMarker.position(newPosition);
                    satelliteMarkerBuffer.put(noradId, sattMarker);
                    mMap.addMarker(satelliteMarkerBuffer.get(noradId));
                }else{
                    satelliteMarkerBuffer.get(noradId).position(newPosition);
                }




                PolylineOptions polylineOptions = new PolylineOptions();
                if(saveCoordsForPolyline){
                    satellitesPolylines.get(noradId).add(newPosition);
                    for(LatLng pos : satellitesPolylines.get(noradId)){
                        polylineOptions.add(pos);
                    }
                    saveCoordsForPolyline = false;
                }else{
                    saveCoordsForPolyline = true;
                }

                polylineOptions.width(19);
                polylineOptions.color(getResources().getColor(R.color.colorSatelliteTrail));
                Polyline line = mMap.addPolyline(polylineOptions);

                //mMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
            }
        });

    }



    private void updateSatellites(){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //mMap.clear();
                Set<String> satellitesNoradId = satelliteController.getNORADSatelliteList();


                for(String noradId : satellitesNoradId){
                    Location l = new Location("Dummy provider");
                    l.setLatitude(satelliteController.getSatelliteLatitude(noradId));
                    l.setLongitude(satelliteController.getSatelliteLongitude(noradId));

                    updateSatelliteLocation(l, noradId);
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
    public void onSatellitePositionControllerReadyCallback() {
        Log.i("Project", "SatelliteController is now ready");

        //initialize polylines
        Set<String> satellitesNoradId = satelliteController.getNORADSatelliteList();
        for(String noradId : satellitesNoradId){
            satellitesPolylines.put(noradId, new ArrayList<LatLng>());
        }

        updateSatellites();
    }


}
