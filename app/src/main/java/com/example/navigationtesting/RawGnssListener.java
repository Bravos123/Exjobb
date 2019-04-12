package com.example.navigationtesting;

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

import com.example.navigationtesting.SatelliteEphemerides.OnRetrieveSatelliteEphemeridesCallback;
import com.example.navigationtesting.SatelliteEphemerides.RetrieveSatelliteEphemerides;
import com.example.navigationtesting.SatelliteMVC.Satellite;
import com.example.navigationtesting.callbacks.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import static android.location.GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GLO_TOD_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_KNOWN;
import static android.location.GnssStatus.CONSTELLATION_BEIDOU;
import static android.location.GnssStatus.CONSTELLATION_GALILEO;
import static android.location.GnssStatus.CONSTELLATION_GLONASS;
import static android.location.GnssStatus.CONSTELLATION_GPS;
import static com.example.navigationtesting.RawGnssTest.LOCATION_RATE_NETWORK_MS;

public class RawGnssListener implements OnRetrieveSatelliteEphemeridesCallback {

    private LocationManager locationManager;
    private ArrayList<Satellite> galileoSatellites;

    private RetrieveSatelliteEphemerides ephemeridesRetriever;


    private LocationManager nmeaListener;

    private Callback sendBackSatelliteData;

    public RawGnssListener(Context c, Callback callbackFunc){
        galileoSatellites = new ArrayList<>();

        sendBackSatelliteData = callbackFunc;


        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new GnssLocationListener();

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_RATE_NETWORK_MS, 0.0f, locationListener);


        //locationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener);
        locationManager.registerGnssMeasurementsCallback(gnssMeasurementListener);
        //locationManager.registerGnssStatusCallback(gnssStatusListener);



        ephemeridesRetriever = new RetrieveSatelliteEphemerides(this);
        ephemeridesRetriever.retrieveEmepherides();
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
                        double pseudorange = calculatePseudorange(gnssM, event);

                        //Log.i("Project", "pseudorange: "+pseudorange);

                        if(pseudorange > 0){
                            for(Satellite s : galileoSatellites){
                                if(s.getSvid() == gnssM.getSvid()){
                                    //Log.i("Project", "Match: "+gl.getSvid()+"   pseudorange="+pseudorange);
                                    s.setClock(event.getClock());
                                    s.setPseudorange(pseudorange);
                                    //s.calculateNewPositionData();
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
        galileoSatellites.sort(new Comparator<Satellite>() {
            @Override
            public int compare(Satellite o1, Satellite o2) {
                return o1.getSvid() - o2.getSvid();
            }
        });

        ArrayList<Satellite> filteredSatelliteData = new ArrayList<>();
        for(Satellite g : galileoSatellites){
            //if(g.getPosition() != null){
                filteredSatelliteData.add(g);
            //}
        }
        sendBackSatelliteData.callBack("Satellite data", filteredSatelliteData);
    }


    private double calculatePseudorange(GnssMeasurement gnssM, GnssMeasurementsEvent event){
        double TrxGnss = event.getClock().getTimeNanos() + gnssM.getTimeOffsetNanos()
                - (event.getClock().getFullBiasNanos() + event.getClock().getBiasNanos());
        //double TrxGnss = event.getClock().getTimeNanos() - (event.getClock().getFullBiasNanos() + event.getClock().getBiasNanos());

        double measurementTime = -1;
        int contellationType = gnssM.getConstellationType();
        int satelliteId = gnssM.getSvid();
        String satelliteType;

        if(gnssM.getState() == STATE_TOW_DECODED){

        }else if(gnssM.getState() == STATE_GAL_E1C_2ND_CODE_LOCK){

        }

        double pseudoRange = 0;

        //GNSS State descriptions: https://source.android.com/reference/hidl/android/hardware/gnss/1.0/IGnssMeasurementCallback#gnssmeasurementstate

        switch(contellationType){
            case CONSTELLATION_GPS:{
                if((gnssM.getState() & STATE_TOW_DECODED) > 0){
                    /*double gpsTime = event.getClock().getTimeNanos() - (event.getClock().getFullBiasNanos() + event.getClock().getBiasNanos());
                    double tRxGPS = gpsTime + gnssM.getTimeOffsetNanos();


                    //double weekNumberNanos = Math.floor((-1.0 * event.getClock().getFullBiasNanos()) / NUMBER_NANO_SECONDS_PER_WEEK) * NUMBER_NANO_SECONDS_PER_WEEK;
                    double weekNumberNanos = (-1.0 * event.getClock().getFullBiasNanos()) % Constants.NUMBER_NANO_SECONDS_PER_WEEK;

                    double gpsPseudoRange = (tRxGPS - weekNumberNanos - gnssM.getReceivedSvTimeNanos()) / 1e9 * Constants.SPEED_OF_LIGHT;

                    //Health check on gps
                    int measState = gnssM.getState();
                    boolean codeLock = (measState & STATE_CODE_LOCK) > 0;
                    boolean towDecoded = (measState & STATE_TOW_DECODED) > 0;
                    boolean towUncertanity = gnssM.getReceivedSvTimeUncertaintyNanos() < MAX_TOW_UNCERTAINTIES;

                    if(codeLock && towDecoded && towUncertanity && gpsPseudoRange < 1e9){
                        //Log.i("Project", "CONSTELLATION_GPS");
                        pseudoRange = gpsPseudoRange;
                    }*/
                }
            }
            case 2:{//CONSTELLATION_SBAS

            }
            case CONSTELLATION_GLONASS:{
                if((gnssM.getState() & STATE_GLO_TOD_DECODED) > 0){
                    //Log.i("Project", "CONSTELLATION_GLONASS");
                    //measurementTime = (TrxGnss % NUMBER_NANO_SECONDS_DAY) + NUMBER_NANO_SECONDS_THREE_HOURS;
                }
            }
            case 4:{//CONSTELLATION_QZSS

            }
            case CONSTELLATION_BEIDOU:{
                if((gnssM.getState() & STATE_TOW_DECODED) > 0){
                    //Log.i("Project", "CONSTELLATION_BEIDOU");
                    /*documentation says 14s so I asume it's gotta be in nano seconds 14 seconds long:
                    2.4.2.2 Approach 2 in https://www.gsa.europa.eu/system/files/reports/gnss_raw_measurement_web_0.pdf*/
                    //measurementTime = (TrxGnss % NUMBER_NANO_SECONDS_PER_WEEK) + NUMBER_NANO_SECONDS_14;
                }
            }
            case CONSTELLATION_GALILEO:{
                double galileoTime = event.getClock().getTimeNanos() - (event.getClock().getFullBiasNanos() + event.getClock().getBiasNanos());
                double tTxGalileo = gnssM.getReceivedSvTimeNanos() + gnssM.getTimeOffsetNanos();

                //Galileo give two signals, we need to check their health status and use the best one: https://gnss-compare.readthedocs.io/en/latest/user_manual/android_gnssMeasurements.html
                if(((gnssM.getState() & STATE_TOW_DECODED) > 0) || ((gnssM.getState() & STATE_TOW_KNOWN) > 0)){
                    double tRxGalileoTOW = TrxGnss % Constants.NUMBER_NANO_SECONDS_PER_WEEK;
                    //Log.i("Project", "CONSTELLATION_GALILEO");
                    pseudoRange = (tRxGalileoTOW - tTxGalileo) * 1e-9 * (double)Constants.SPEED_OF_LIGHT;
                    if(pseudoRange >= 4000000){/*Sometimes unrealistically large pseudoranges area created here*/
                        //Log.i("Project", "Pseudorange problem GALILEO Constellation STATE_TOW_DECODED");
                    }
                }else if((gnssM.getState() & STATE_GAL_E1C_2ND_CODE_LOCK) > 0){//FIXME GIVES NEGATIVE RESULTS: DON'T THINK THAT'S SUPPOSED TO HAPPEN
                    double tRxGalileoE1_2nd = galileoTime % Constants.NumberNanoSeconds100Milli;
                    //Log.i("Project", "CONSTELLATION_GALILEO");
                    pseudoRange = ((galileoTime - tTxGalileo) % Constants.NumberNanoSeconds100Milli) * 1e-9 * (double)Constants.SPEED_OF_LIGHT;

                }



            }
            case 9:{//CONSTELLATION_UNKNOWN

            }
        }

        //Bad fix to a problem
        /*if(pseudoRange >= 4000000){
            pseudoRange = 0;
        }*/


        return pseudoRange;
    }


    @Override
    public void Callback(String lastModified, ArrayList<Satellite> updatedSatellies) {
        Log.i("Project", "updatedSatellies.size: "+updatedSatellies.size()+"-------------------------------------");

        /*for(int i=0; i<updatedSatellies.size(); i++){
            Log.i("Project", "svid: "+updatedSatellies.get(i).getSvid());
        }*/
        //Check if satellites number already has been added
        /*for(GalileoSatelliteData glUpdated : updatedSatellies){

            boolean satelliteWasUpdated = false;
            for(int i=0; i<galileoSatellites.size(); i++){
                if(galileoSatellites.get(i).getSvid() == glUpdated.getSvid()){
                    galileoSatellites.set(i, glUpdated);
                    satelliteWasUpdated = true;
                    break;
                }
            }

            if(!satelliteWasUpdated){
                //satellite system has not been added, add it
                galileoSatellites.add(glUpdated);
            }

        }*/

        galileoSatellites = updatedSatellies;



        sendUpdatedSatellites();
    }
}
