package com.example.navigationtesting.SatelliteMVC;

import android.location.GnssClock;
import android.util.Log;

import com.example.navigationtesting.CalculateSatellitePosition;
import com.example.navigationtesting.GalileoEphemerides;
import com.example.navigationtesting.SatellitePositionData;

public class Satellite {
    private GalileoEphemerides eph;
    private SatellitePositionData myPositionData;
    private int noradId;

    private double pseudorange = -1;
    private GnssClock internalClock;


    public Satellite(String data){
        eph = new GalileoEphemerides(data);
        myPositionData = CalculateSatellitePosition.getGalileoSatellitePosition(eph);

        try {
            noradId = Integer.parseInt(SatelliteNORADId.getGalileoNORADId(eph.getSvid()));
        } catch (NoradIdDoesNotExist noradIdDoesNotExist) {
            noradId = -1;
        }
    }

    public int getNoradId(){
        return noradId;
    }

    public SatellitePositionData getSatellitePositionData(){
        return myPositionData;
    }

    public GalileoEphemerides getEphemerides(){
        return eph;
    }

    public void updatteSatelliteCoordinates(){
        myPositionData = CalculateSatellitePosition.getGalileoSatellitePosition(eph);
    }

    public void updateSatelliteEphemerides(GalileoEphemerides newEph){
        eph = newEph;
    }

    public int getSvid(){
        return eph.getSvid();
    }

    public double getLongitude() {
        //Angle is stored in rade but this returns it in degrees
        return myPositionData.getEllipsoidalCoords()[1] * (180/Math.PI);
    }

    public double getLatitude() {
        //Angle is stored in rade but this returns it in degrees
        return myPositionData.getEllipsoidalCoords()[0] * (180/Math.PI);
    }

    public double getAltitude(){
        return myPositionData.getEllipsoidalCoords()[2];
    }

    public long getEphemeridesUnixTime(){
        return eph.getUnixTime();
    }

    public void setClock(GnssClock clock) {
        internalClock = clock;
    }

    public void setPseudorange(double pseudorange) {
        this.pseudorange = pseudorange;
    }
}
