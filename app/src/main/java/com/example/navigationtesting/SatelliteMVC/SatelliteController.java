package com.example.navigationtesting.SatelliteMVC;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.navigationtesting.GnssLocationListener;
import com.example.navigationtesting.PseudorangeCalculator;
import com.example.navigationtesting.SatelliteEphemerides.OnRetrieveSatelliteEphemeridesCallback;
import com.example.navigationtesting.SatelliteEphemerides.RetrieveSatelliteEphemerides;
import com.example.navigationtesting.callbacks.CallbackBoolean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static com.example.navigationtesting.RawGnssTest.LOCATION_RATE_NETWORK_MS;

public class SatelliteController implements OnRetrieveSatelliteEphemeridesCallback, CallbackBoolean {
    private SatellitesFutureCoordinateBuffer futureCoordinatesBuffer;
    private Satellite model;
    private SatelliteView view;
    private boolean waitingForCallback = false;
    private OnSatelliteControllerReadyCallback callback;

    private RetrieveSatelliteEphemerides ephemeridesRetriever;

    ArrayList<Satellite> storedSatellites;

    private LocationManager locationManager;


    public SatelliteController(Context c, OnSatelliteControllerReadyCallback cb, int minBufferSizeMultiplier){
        futureCoordinatesBuffer = new SatellitesFutureCoordinateBuffer(this, minBufferSizeMultiplier);
        callback = cb;



        Log.i("Project", "Calling RetrieveSatelliteEphemerides");
        ephemeridesRetriever = new RetrieveSatelliteEphemerides(this);
        ephemeridesRetriever.retrieveEmepherides();
        waitingForCallback = true;

        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new GnssLocationListener();

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_RATE_NETWORK_MS, 0.0f, locationListener);
        //locationManager.registerGnssMeasurementsCallback(gnssMeasurementListener);

    }

    public void setSatellite(Satellite targetModel, SatelliteView targetView){
        model = targetModel;
        view = targetView;
    }


    public void retrieveSatellites(){
        if(!waitingForCallback){
            ephemeridesRetriever.retrieveEmepherides();
        }
    }




    private final GnssMeasurementsEvent.Callback gnssMeasurementListener =
            new GnssMeasurementsEvent.Callback(){

                @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                    Collection<GnssMeasurement> measurements = event.getMeasurements();

                    for(GnssMeasurement gnssM : measurements){
                        gnssM.getPseudorangeRateMetersPerSecond();
                        //Log.i("Project", );


                        gnssM.getConstellationType();
                        double pseudorange = PseudorangeCalculator.calculatePseudorange(gnssM, event);

                        //Log.i("Project", "pseudorange: "+pseudorange);

                        if(pseudorange > 0){
                            futureCoordinatesBuffer.setSatellitePseudorande(gnssM.getSvid(), pseudorange);
                        }

                    }

                }

                @Override
                public void onStatusChanged(int status) {

                }
            };




    @Override
    public void callBack(boolean b) {//Satellite future coordinates is ready
        callback.callBack(b);
    }

    @Override
    public void Callback(String lastModifiedDate, ArrayList<Satellite> satts) {
        Log.i("Project", "Got response from ephemerides loader");
        storedSatellites = satts;
        futureCoordinatesBuffer.startNewBufferFromEphemerides(storedSatellites);

        /*waitingForCallback = false;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Date currentDate = new Date();
            Date modifiedDate = format.parse(lastModifiedDate);

            long diffInMs = currentDate.getTime() - modifiedDate.getTime();

            long diffSeconds = diffInMs / 1000 % 60;
            long diffMinutes = diffInMs / (60 * 1000) % 60;
            long diffHours = diffInMs / (60 * 60 * 1000) % 24;
            long diffDays = diffInMs / (24 * 60 * 60 * 1000);

            double numberOfHours = diffInMs / millisecInHour;

            Log.i("Project",diffDays + " days, ");
            Log.i("Project",diffHours + " hours, ");
            Log.i("Project",diffMinutes + " minutes, ");
            Log.i("Project",diffSeconds + " seconds.");



            //Schedule another ephemerides download update
            long updateDelay = (long)(millisecInHour - (numberOfHours * millisecInHour));
            if(updateDelay <= 0){
                updateDelay = 1000;
            }
            long updateDelay = millisecInHour;//Keep it simple and update every hour
            Log.i("Project", "Update delay: "+updateDelay+"   numberOfHours: "+numberOfHours);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    retrieveSatellites();
                }
            }, updateDelay);//Assume the ephemerides data is updated every 2 hours

        } catch (ParseException e) {
            Log.i("Project", "Date parse error");
            e.printStackTrace();
        }


        Log.i("Project", "Retrieved "+satts.size()+" number of satellites from RetrieveSatelliteEphemerides");
        storedSatellites = satts;
        callback.callBack("", storedSatellites);*/
    }



    public Set<Integer> getNORADSatelliteList() {
        return futureCoordinatesBuffer.getCoordinateBufferKeyset();
    }

    public double getSatelliteLongitude(int noradid) {
        long unixTime = System.currentTimeMillis() / 1000L;
        return futureCoordinatesBuffer.getSatelliteLongitudeFromUnixtime(noradid, unixTime, true);
    }

    public double getSatelliteLatitude(int noradid) {
        long unixTime = System.currentTimeMillis() / 1000L;
        return futureCoordinatesBuffer.getSatelliteLatitudeFromUnixtime(noradid, unixTime, true);
    }

    public double getSatelliteLongitudePseudorangeNotRequired(int noradid) {
        long unixTime = System.currentTimeMillis() / 1000L;
        return futureCoordinatesBuffer.getSatelliteLongitudeFromUnixtime(noradid, unixTime, false);
    }

    public double getSatelliteLatitudePseudorangeNotRequired(int noradid) {
        long unixTime = System.currentTimeMillis() / 1000L;
        return futureCoordinatesBuffer.getSatelliteLatitudeFromUnixtime(noradid, unixTime, false);
    }

}
