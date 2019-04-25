package com.example.navigationtesting.showMyLocation;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.navigationtesting.R;
import com.example.navigationtesting.Satellite.LatLngAlt;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
    public void onUserLocationGeneratorNewPosition(LatLngAlt pos) {
        if(myLocation == null){
            MarkerOptions sattMarkerOption = new MarkerOptions();
            sattMarkerOption.title("You are here");
            myLocation = mMap.addMarker(sattMarkerOption);
        }
        myLocation.setPosition(pos.toLatLng());
    }
}
