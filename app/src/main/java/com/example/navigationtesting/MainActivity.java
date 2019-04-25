package com.example.navigationtesting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.navigationtesting.MapOfSatellitePositions.MapsShowPositionOfSatellites;
import com.example.navigationtesting.rawGnssTest.RawGnssTest;
import com.example.navigationtesting.showMyLocation.ShowMyLocation;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int returnedAllowedPremissions = 0;
    private final int REQUESTCODE = 1;
    private int premissionsAskingFor = 0;
    private Button startGMapsButton;
    private Button RawGnssTestButton;
    private Button showMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askForALlPermissions();
    }


    private void askForALlPermissions(){
        String[] targetPermissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS
        };


        List<String> permissionRequests = new ArrayList<>();
        for(String permission : targetPermissions){
            if(!hasPremission( permission)){
                permissionRequests.add(permission);
            }
        }
        premissionsAskingFor = permissionRequests.size();
        ActivityCompat.requestPermissions(this,
                targetPermissions,
                REQUESTCODE);
    }


    public boolean hasPremission(String premissionName){
        if(ContextCompat.checkSelfPermission(this, premissionName) !=
                PackageManager.PERMISSION_GRANTED){
            return false;
        }else{
            return true;
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
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode){
            case REQUESTCODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permission granted
                    returnedAllowedPremissions++;
                    if(returnedAllowedPremissions >= premissionsAskingFor){
                        init();
                    }

                }

            }
        }
    }
}
