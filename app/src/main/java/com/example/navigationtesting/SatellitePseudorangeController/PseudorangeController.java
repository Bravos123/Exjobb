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

import java.util.ArrayList;
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
            Pair<Long, Double> addTime = new Pair<>(System.currentTimeMillis() / 1000L, -1.0);
            noradAndPseudorande.put(noradId, addTime);
        }
    }

    private final GnssMeasurementsEvent.Callback gnssMeasurementListener =
            new GnssMeasurementsEvent.Callback(){

                @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                    //Calculate pseudorange
                    Collection<GnssMeasurement> measurements = event.getMeasurements();
                    //Looping through each satellite in the constelation?
                    //Log.i("Project", "measurements size: "+measurements.size());
                    int counter = 0;
                    for(GnssMeasurement gnssM : measurements){
                        //Log.i("Project", "counter: "+counter);
                        //Log.i("Project", gnssM.toString());


                        double pseudorange = CalculateSatellitePseudorange.calculatePseudorange(gnssM, event, "meters");
                        if(SatelliteNORADId.noradIdExistFromSvId(gnssM.getSvid())) {
                            try {
                                updateSatellitePseudorange(SatelliteNORADId.getGalileoNORADId(gnssM.getSvid()), pseudorange);
                            } catch (NoradIdDoesNotExist noradIdDoesNotExist) {
                                noradIdDoesNotExist.printStackTrace();
                            }
                        }else{
                            //Log.i("Project", "ERROR: there is no norad id in app for svId "+ gnssM.getSvid());
                        }


                    }

                    //Log.i("Project", "Size: "+Integer.toString(noradAndPseudorande.size()));
                    readyCallback.onPseudorangeControllerReadyCallback();

                }

                @Override
                public void onStatusChanged(int status) {
                    Log.i("Project", "GnssMeasurementsEvent.Callback status: "+Integer.toString(status));
                }
            };


    private void updateSatellitePseudorange(int noradId, double newPseudoRange){
        if((newPseudoRange/1000L) != 0){
            //Log.i("Project", "New pseudorange: "+(newPseudoRange/1000L));
        }

        if(newPseudoRange/1000L <= 0){
            return;
        }

        Pair<Long, Double> addTime = new Pair<>(System.currentTimeMillis() / 1000L, newPseudoRange);
        if(noradAndPseudorande.get(noradId) != null) {
            noradAndPseudorande.replace(noradId, addTime);
        }else{
            noradAndPseudorande.put(noradId, addTime);
        }
    }


    public ArrayList<Pair<Integer, Double>> getArrayOfSatellitePseudoranges(){
        ArrayList<Pair<Integer, Double>> sattsAndPseudoR = new ArrayList<>();

        Long currentUnixTime = System.currentTimeMillis() / 1000L;
        int precision = 5;
        while(sattsAndPseudoR.size()<4 && precision < 60){
            sattsAndPseudoR.clear();
            for(int noradId : noradIdSet){
                if(currentUnixTime - noradAndPseudorande.get(noradId).first < precision && noradAndPseudorande.get(noradId).second > 0){//If the pseudorange date is 10 seconds or younger
                    sattsAndPseudoR.add(new Pair<>(noradId, noradAndPseudorande.get(noradId).second));
                }
            }
            precision++;
        }

        return sattsAndPseudoR;
    }


}
