package com.example.navigationtesting.MapOfSatellitePositions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.example.navigationtesting.MainActivity;
import com.example.navigationtesting.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MapsShowPositionOfSatellites extends FragmentActivity implements OnMapReadyCallback, OnSatellitePositionControllerReadyCallback {
    private GoogleMap mMap;
    private SatellitePositionController satelliteController;
    private Timer timer;

    private HashMap<Integer, Pair<Polyline, List<LatLng>>> satellitesPolylines;

    private int skipCounter = 0;
    private final int skipTimer = 30;

    private HashMap<Integer, Marker> satelliteMarkerBuffer;

    private boolean terminated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        satelliteMarkerBuffer = new HashMap<Integer, Marker>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        satellitesPolylines = new HashMap<>();
        timer = new Timer();
        satelliteController = new SatellitePositionController( this);
        satelliteController.initialize();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }


    private void updateSatelliteLocation(final LatLng newPosition, int noradId){
        if(terminated){
            return;
        }
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                if(newPosition == null){
                    return;
                }
                //Log.i("Project", "Latitude: "+newPosition.latitude+"   Longitude: "+newPosition.longitude);
                if(satelliteMarkerBuffer.get(noradId) == null){
                    Log.i("Project", "Place marker for: "+noradId);
                    MarkerOptions sattMarkerOption = new MarkerOptions();
                    sattMarkerOption.title("GALILEO NORAD ID:"+noradId);
                    sattMarkerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.satellite));
                    sattMarkerOption.anchor(0.5f, 0.5f);
                    sattMarkerOption.position(newPosition);
                    satelliteMarkerBuffer.put(noradId, mMap.addMarker(sattMarkerOption));
                }else{
                    satelliteMarkerBuffer.get(noradId).setTitle(noradId+"  -  lat: "+newPosition.latitude+"  lng: "+newPosition.longitude);
                    satelliteMarkerBuffer.get(noradId).setPosition(newPosition);
                }






                if(satellitesPolylines.get(noradId) == null){
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.add(newPosition);
                    polylineOptions.width(19);
                    polylineOptions.color(getResources().getColor(R.color.colorSatelliteTrail));
                    polylineOptions.startCap(new RoundCap());
                    polylineOptions.endCap(new RoundCap());
                    List<LatLng> trailPoints = new ArrayList<>();
                    trailPoints.add(newPosition);

                    Pair<Polyline, List<LatLng>> satelliteTrail = new Pair<>(mMap.addPolyline(polylineOptions), trailPoints);
                    satellitesPolylines.put(noradId, satelliteTrail);
                }
                if(skipCounter == 0){
                    //satellitesPolylines.get(noradId).add(newPosition);
                    satellitesPolylines.get(noradId).second.add(newPosition);
                }

                //Add last to become hooked to satellite
                satellitesPolylines.get(noradId).second.add(newPosition);

                satellitesPolylines.get(noradId).first.setPoints(satellitesPolylines.get(noradId).second);



                //mMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
            }
        });

    }



    private void updateSatellites(){
        if(terminated){
            return;
        }
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //mMap.clear();
                Set<Integer> satellitesNoradId = satelliteController.getNORADSatelliteList();


                for(int noradId : satellitesNoradId){
                    //Log.i("Project", noradId);
                    //Log.i("Project", "Latitude: "+satelliteController.getSatelliteLatitude(noradId)+"  longitude: "+satelliteController.getSatelliteLongitude(noradId));
                    LatLng newPosition = satelliteController.getSatelliteCoordinates(noradId);

                    updateSatelliteLocation(newPosition, noradId);
                }

                timer.schedule(new TimerTask(){
                    @Override
                    public void run(){
                        updateSatellites();
                        if(skipCounter == 0){
                                    skipCounter = skipTimer;
                        }else{
                            skipCounter--;
                        }
                    }
                }, SatellitePositionController.REFRESH_TIME_MS);
            }
        });

    }


    @Override
    public void onSatellitePositionControllerReadyCallback() {
        Log.i("Project", "SatelliteController is now ready");
        Set<Integer> satellitesNoradId = satelliteController.getNORADSatelliteList();
        Log.i("Project", "We have: "+satellitesNoradId.size()+" satellites");
        updateSatellites();
    }


    @Override
    public void onBackPressed(){
        terminated = true;
        Intent i = new Intent(MapsShowPositionOfSatellites.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}
