package com.example.navigationtesting;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements Callback{
    private int targetPermissions = 1;
    private int allowedPermissions = 0;

    private Button startGMapsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askForALlPermissions();
    }


    private void askForALlPermissions(){
        new AskForPremission(this, Manifest.permission.ACCESS_FINE_LOCATION);
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
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });
    }
}
