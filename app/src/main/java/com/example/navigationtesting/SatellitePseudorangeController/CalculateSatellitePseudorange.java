package com.example.navigationtesting.SatellitePseudorangeController;

import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.util.Log;

import com.example.navigationtesting.rawGnssTest.Constants;

import static android.location.GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK;
import static android.location.GnssMeasurement.STATE_GLO_TOD_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_DECODED;
import static android.location.GnssMeasurement.STATE_TOW_KNOWN;
import static android.location.GnssStatus.CONSTELLATION_BEIDOU;
import static android.location.GnssStatus.CONSTELLATION_GALILEO;
import static android.location.GnssStatus.CONSTELLATION_GLONASS;
import static android.location.GnssStatus.CONSTELLATION_GPS;

public class CalculateSatellitePseudorange {

    public static final double calculatePseudorange(GnssMeasurement gnssM, GnssMeasurementsEvent event){
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

}
