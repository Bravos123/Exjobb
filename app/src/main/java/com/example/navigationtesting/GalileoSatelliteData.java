package com.example.navigationtesting;

import android.icu.math.BigDecimal;
import android.location.GnssClock;
import android.util.Log;

import java.util.ArrayList;

public class GalileoSatelliteData {
    private GalileoEphemerides eph;


    //Other GNSS DATA
    private double pseudorange = -1;
    private GnssClock internalClock;
    private double satelliteTime = -1;

    public GalileoSatelliteData(String data){//Data is single data entry from RINEX file.
        eph = new GalileoEphemerides(data);
    }




    public static ArrayList<Integer> getAllIndexMatches(String haystack, String needle){
        ArrayList<Integer> matches = new ArrayList<Integer>();
        int index = haystack.indexOf(needle);
        while (index >=0){
            matches.add(index);
            index = haystack.indexOf(needle, index+needle.length())   ;
        }
        return matches;
    }


    public String getSatelliteSystem(){
        return eph.getSatelliteSystem();
    }

    public int getSvid(){
        return eph.getSvid();
    }

    public double getPseudorange(){
        return pseudorange;
    }

    public int getPseudorangeInKilometers(){
        return (int) Math.floor(pseudorange/1000.0);
    }

    public void setPseudorange(double setPseudorange){
        this.pseudorange = setPseudorange;
    }

    public void setClock(GnssClock clock) {
        internalClock = clock;
        satelliteTime = clock.getTimeNanos() - (clock.getFullBiasNanos() + clock.getBiasNanos());
    }

    public double getSatelliteTime(){
        return satelliteTime;
    }

    public String getPosition(){
        if(pseudorange != -1 && satelliteTime != -1){
            return CalculateSatellitePosition.getGalileoSatellitePosition(satelliteTime, pseudorange, eph);
        }

        return "NO KNOWN POSITION";
    }
}
