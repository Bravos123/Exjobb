package com.example.navigationtesting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static android.location.GnssMeasurement.STATE_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GLO_TOD_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_KNOWN;
import static android.location.GnssStatus.CONSTELLATION_BEIDOU;
import static android.location.GnssStatus.CONSTELLATION_GALILEO;
import static android.location.GnssStatus.CONSTELLATION_GLONASS;
import static android.location.GnssStatus.CONSTELLATION_GPS;

public class RawGnssTest extends AppCompatActivity implements Callback<ArrayList<GalileoSatelliteData>>{
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
