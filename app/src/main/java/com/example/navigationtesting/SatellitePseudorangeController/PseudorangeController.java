package com.example.navigationtesting.SatellitePseudorangeController;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Pair;

import com.example.navigationtesting.rawGnssTest.GnssLocationListener;
import com.example.navigationtesting.Satellite.NoradIdDoesNotExist;
import com.example.navigationtesting.Satellite.SatelliteNORADId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import static com.example.navigationtesting.rawGnssTest.RawGnssTest.LOCATION_RATE_NETWORK_MS;

public class PseudorangeController {
    private Set<Integer> noradIdSet;
    private LocationManager locationManager;
    private HashMap<Integer, Pair<Long, Double>> noradAndPseudorande;
    private OnPseudorangeControllerReadyCallback readyCallback;


    public PseudorangeController(Context c, OnPseudorangeControllerReadyCallback callback){
        readyCallback = callback;
        noradAndPseudorande = new HashMap<>();


        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new GnssLocationListener();

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_RATE_NETWORK_MS, 0.0f, locationListener);


        //locationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener);
        locationManager.registerGnssMeasurementsCallback(gnssMeasurementListener);
        //locationManager.registerGnssStatusCallback(gnssStatusListener);
    }


    public void setAvailibleSatellitesNORADList(Set<Integer> noradList){
        noradIdSet = noradList;
        for(int noradId : noradList){
            Pair<Long, Double> addTime = new Pair(System.currentTimeMillis() / 1000L, -1.0);
            noradAndPseudorande.put(noradId, addTime);
        }
    }

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


                        try {
                            int satelliteNoradId = SatelliteNORADId.getGalileoNORADId(gnssM.getSvid());
                            updateSatellitePseudorange(satelliteNoradId, pseudorange);
                        } catch (NoradIdDoesNotExist noradIdDoesNotExist) {
                            noradIdDoesNotExist.printStackTrace();
                        }

                    }


                }

                @Override
                public void onStatusChanged(int status) {
                    Log.i("Project", "GnssMeasurementsEvent.Callback status: "+Integer.toString(status));
                }
            };


    private void updateSatellitePseudorange(int noradId, double newPseudoRange){
        if(newPseudoRange > 10000){
            if(noradAndPseudorande.get(noradId) != null){
                Pair<Long, Double> addTime = new Pair(System.currentTimeMillis() / 1000L, newPseudoRange);
                noradAndPseudorande.replace(noradId, addTime);
            }
        }
        int pseudorangesAvailible = 0;
        for(int nId : noradIdSet){
            if(noradAndPseudorande.get(nId) != null){
                pseudorangesAvailible++;
            }
        }
        if(pseudorangesAvailible > 3){
            readyCallback.onPseudorangeControllerReadyCallback();
        }
    }

    public double getSatellitePseudorange(String noradId){
        if(noradAndPseudorande.get(noradId) == null){
            return -1;
        }
        Long currentUnixTime = System.currentTimeMillis() / 1000L;
        if(currentUnixTime - noradAndPseudorande.get(noradId).first > 60){//If the pseudorange was added 1 minute ago then it's too old to be used
            return -1;
        }
        return noradAndPseudorande.get(noradId).second;
    }




}
