package com.example.navigationtesting;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.navigationtesting.MapOfSatellitePositions.MapsShowPositionOfSatellites;
import com.example.navigationtesting.callbacks.Callback;
import com.example.navigationtesting.showMyLocation.ShowMyLocation;

public class MainActivity extends AppCompatActivity implements Callback {
    private int targetPermissions = 3;
    private int allowedPermissions = 0;

    private Button startGMapsButton;
    private Button RawGnssTestButton;
    private Button showMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askForALlPermissions();
    }


    private void askForALlPermissions(){
        new AskForPremission(this, Manifest.permission.INTERNET);
        new AskForPremission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        new AskForPremission(this, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS);
    }

    @Override
    public void callBack(String name, Object in) {
        if(in.equals(true)){
            allowedPermissions++;
        }
        Log.d("Project", name+": "+in.toString());

        if(allowedPermissions == targetPermissions){//All required permissions were granted, move on
            init();
        }
    }


    private void init(){
        setContentView(R.layout.activity_main);

        //Log.d("Project", "Hello");
        startGMapsButton = findViewById(R.id.startGMaps);
        startGMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsShowPositionOfSatellites.class);
                startActivity(i);
            }
        });

        showMyLocation = findViewById(R.id.ShowMyLocation);
        showMyLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowMyLocation.class);
                startActivity(i);
            }
        });

        RawGnssTestButton = findViewById(R.id.rawGnssTestButton);
        RawGnssTestButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RawGnssTest.class);
                startActivity(i);
            }
        });
    }
}
