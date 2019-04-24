package com.example.navigationtesting.rawGnssTest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.navigationtesting.Satellite.Satellite;
import com.example.navigationtesting.SatellitePseudorangeController.CalculateSatellitePseudorange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import static android.location.GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GLO_TOD_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_KNOWN;
import static android.location.GnssStatus.CONSTELLATION_BEIDOU;
import static android.location.GnssStatus.CONSTELLATION_GALILEO;
import static android.location.GnssStatus.CONSTELLATION_GLONASS;
import static android.location.GnssStatus.CONSTELLATION_GPS;
import static com.example.navigationtesting.rawGnssTest.RawGnssTest.LOCATION_RATE_NETWORK_MS;

public class RawGnssListener{

    private LocationManager locationManager;
    private HashMap<Integer, Satellite> galileoSatellites;


    private LocationManager nmeaListener;

    private OnRawGnssListenerCallback OnRawGnssListenerCallback;

    public RawGnssListener(Context c, OnRawGnssListenerCallback callbackFunc){
        galileoSatellites = new HashMap<>();

        OnRawGnssListenerCallback = callbackFunc;


        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new GnssLocationListener();

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_RATE_NETWORK_MS, 0.0f, locationListener);


        locationManager.registerGnssMeasurementsCallback(gnssMeasurementListener);

        sendUpdatedSatellites();

    }



    private final GnssNavigationMessage.Callback gnssNavigationMessageListener =
            new GnssNavigationMessage.Callback() {
                @Override
                public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                    Log.i("Project", "Nagivation massage: "+event.toString());
                }

                @Override
                public void onStatusChanged(int status) {
                    //Log.i("Project", "GnssNavigationMessage.Callback status: "+Integer.toString(status));
                }
            };

    private final GnssMeasurementsEvent.Callback gnssMeasurementListener =
            new GnssMeasurementsEvent.Callback(){

                @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                    //Log.i("Project", event.toString());
                    //Calculate pseudorange
                    Collection<GnssMeasurement> measurements = event.getMeasurements();
                    //Looping through each satellite in the constelation?
                    for(GnssMeasurement gnssM : measurements){
                        gnssM.getPseudorangeRateMetersPerSecond();
                        //Log.i("Project", );


                        gnssM.getConstellationType();
                        double pseudorange = CalculateSatellitePseudorange.calculatePseudorange(gnssM, event);

                        //Log.i("Project", "pseudorange: "+pseudorange);

                        if(galileoSatellites.get(gnssM.getSvid()) == null){
                            galileoSatellites.put(gnssM.getSvid(), new Satellite(gnssM.getSvid()));
                        }


                        if(pseudorange > 0){
                            Set<Integer> hashMapKeys = galileoSatellites.keySet();
                            for(Integer sId : hashMapKeys){
                                Satellite s = galileoSatellites.get(sId);
                                if(s.getSvid() == gnssM.getSvid()){
                                    s.setClock(event.getClock());
                                    s.setPseudorange(pseudorange);
                                }
                            }
                        }

                    }

                    sendUpdatedSatellites();

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



    private void sendUpdatedSatellites(){

        ArrayList<Satellite> filteredSatelliteData = new ArrayList<>();
        Set<Integer> hashMapKeys = galileoSatellites.keySet();
        for(Integer sId : hashMapKeys){
            Satellite s = galileoSatellites.get(sId);
            filteredSatelliteData.add(s);
        }
        OnRawGnssListenerCallback.onRawGnssListenerCallback(filteredSatelliteData);
    }




}
