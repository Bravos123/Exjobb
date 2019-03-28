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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class RawGnssTest extends AppCompatActivity {
    private LocationManager locationManager;
    private static final long LOCATION_RATE_NETWORK_MS = TimeUnit.SECONDS.toMillis(60L);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_gnss_test);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new GnssLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_RATE_NETWORK_MS, 0.0f, locationListener);


        //locationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener);
        locationManager.registerGnssMeasurementsCallback(gnssMeasurementListener);
        //locationManager.registerGnssStatusCallback(gnssStatusListener);



        /*Log.i("Project", "Fetching Time data Command sent");
        Bundle bundle = new Bundle();
        locationManager.sendExtraCommand("gps", "force_time_injection", bundle);*/

    }


    private final GnssNavigationMessage.Callback gnssNavigationMessageListener =
            new GnssNavigationMessage.Callback() {
                @Override
                public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                    Log.i("Project", event.toString());
                }

                @Override
                public void onStatusChanged(int status) {
                    Log.i("Project", "GnssNavigationMessage.Callback status: "+Integer.toString(status));
                }
            };

    private final GnssMeasurementsEvent.Callback gnssMeasurementListener =
            new GnssMeasurementsEvent.Callback(){

            @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                    Log.i("Project", "GNSS Time: "+Double.toString(calculateGpsTime(event.getClock()))+"\n");
                    //Log.i("Project", event.toString());
                }

                @Override
                public void onStatusChanged(int status) {
                    Log.i("Project", "GnssMeasurementsEvent.Callback status: "+Integer.toString(status));
                }
            };

    private final GnssStatus.Callback gnssStatusListener =
            new GnssStatus.Callback() {
                @Override
                public void onStarted() {}

                @Override
                public void onStopped() {}

                @Override
                public void onFirstFix(int ttff) {}

                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    Log.i("Project","getSatelliteCount: "+Integer.toString(status.getSatelliteCount()));
                }
            };


    /*From Google's GNSSLogger UiLogger.java*/
    private double calculateGpsTime(GnssClock gnssClock) {

        double GNSSNanosecTime;
        GNSSNanosecTime = (gnssClock.getTimeNanos() - (gnssClock.getFullBiasNanos() + gnssClock.getBiasNanos()));

        return GNSSNanosecTime;
    }


    private double calculatePseudoRange(){
        //Look at 2.4.2 Pseudorange generation in https://www.gsa.europa.eu/system/files/reports/gnss_raw_measurement_web_0.pdf
        

        return 0.0;
    }
}
