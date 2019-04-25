package com.example.navigationtesting.rawGnssTest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.navigationtesting.R;
import com.example.navigationtesting.Satellite.Satellite;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RawGnssTest extends AppCompatActivity implements OnRawGnssListenerCallback {
    public static final long LOCATION_RATE_NETWORK_MS = TimeUnit.SECONDS.toMillis(60L);
    ArrayList<Satellite> satelliteData;

    LinearLayout layoutSatellites;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_gnss_test);

        layoutSatellites = findViewById(R.id.scrollViewSatellites);

        new RawGnssListener(this, this);

    }



    private void updateSatalliteDataUI(){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                layoutSatellites.removeAllViews();



                for(Satellite s : satelliteData){
                    if(s.getPseudorange() > 0){
                        TextView newSatId = new TextView(RawGnssTest.this);
                        newSatId.setText("svid: "+Integer.toString(s.getSvid())+"   -   NORAD ID: "+Integer.toString(s.getNoradId()));
                        layoutSatellites.addView(newSatId);

                        TextView satPseudorange = new TextView(RawGnssTest.this);
                        satPseudorange.setText("pseudorange: "+Double.toString(s.getPseudorangeInKilometers())+" kilometers");
                        layoutSatellites.addView(satPseudorange);

                    }

                }

            }
        });

    }


    @Override
    public void onRawGnssListenerCallback(ArrayList<Satellite> satelliteData) {
        this.satelliteData = satelliteData;
        updateSatalliteDataUI();
    }
}
