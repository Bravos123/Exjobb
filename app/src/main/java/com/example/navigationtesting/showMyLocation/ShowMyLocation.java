package com.example.navigationtesting.showMyLocation;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.navigationtesting.R;
import com.example.navigationtesting.Satellite.LatLngAlt;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowMyLocation extends FragmentActivity implements OnMapReadyCallback, OnUserLocationGeneratorNewPosition{
    private UserLocationGenerator galileoGnss;
    private GoogleMap mMap;
    private Marker myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_my_location);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        galileoGnss = new UserLocationGenerator(this);
        galileoGnss.addNewPositionCallback(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



    }

    @Override
    public void onUserLocationGeneratorNewPosition(final LatLngAlt pos) {
        if(pos == null){
            Log.i("Project", "pos is NULL");
        }else{
            //Log.i("Project", pos.toString());
            try{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(myLocation == null){
                            MarkerOptions sattMarkerOption = new MarkerOptions();
                            sattMarkerOption.title("You are here");
                            sattMarkerOption.position(pos.toLatLng());
                            myLocation = mMap.addMarker(sattMarkerOption);
                        }else{
                            myLocation.setPosition(pos.toLatLng());
                        }


                    }
                });
            }catch(Exception e){
                Log.i("Project", e.getMessage());
            }

        }



    }
}
