package com.example.navigationtesting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.navigationtesting.callbacks.Callback;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RawGnssTest extends AppCompatActivity implements Callback<ArrayList<GalileoSatelliteData>> {
    public static final long LOCATION_RATE_NETWORK_MS = TimeUnit.SECONDS.toMillis(60L);
    ArrayList<GalileoSatelliteData> satelliteData;

    LinearLayout layoutSatellites;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_gnss_test);

        layoutSatellites = findViewById(R.id.scrollViewSatellites);

        new RawGnssListener(this, this);

    }






    @Override
    public void callBack(String name, ArrayList<GalileoSatelliteData> satelliteData) {
        //Log.i("Project", "updatedSatellies.size: "+satelliteData.size()+"-------------------------------------");
        this.satelliteData = satelliteData;
        updateSatalliteDataUI();

    }




    private void updateSatalliteDataUI(){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                layoutSatellites.removeAllViews();



                for(GalileoSatelliteData g : satelliteData){
                    if(g.getPseudorange() > 0){
                        TextView newSatId = new TextView(RawGnssTest.this);
                        newSatId.setText("svid: "+Integer.toString(g.getSvid()));
                        layoutSatellites.addView(newSatId);

                        TextView satPseudorange = new TextView(RawGnssTest.this);
                        satPseudorange.setText("pseudorange: "+Double.toString(g.getPseudorangeInKilometers())+" kilometers");
                        layoutSatellites.addView(satPseudorange);

                        TextView satPosition = new TextView(RawGnssTest.this);
                        satPosition.setText("position: "+g.getPosition().toString());
                        layoutSatellites.addView(satPosition);
                    }

                }

            }
        });

    }





}
