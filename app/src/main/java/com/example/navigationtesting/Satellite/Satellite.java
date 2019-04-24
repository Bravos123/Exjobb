package com.example.navigationtesting.Satellite;

import android.location.GnssClock;

public class Satellite {
    private int svId;
    private int noradId;

    private double pseudorange = -1;
    private GnssClock internalClock;


    public Satellite(int svId){
        this.svId = svId;
        try {
            noradId = SatelliteNORADId.getGalileoNORADId(this.svId);
        } catch (NoradIdDoesNotExist noradIdDoesNotExist) {
            noradIdDoesNotExist.printStackTrace();
        }
    }


    public int getSvid(){
        return svId;
    }
    public int getNoradId(){
        return noradId;
    }
    


    public void setClock(GnssClock clock) {
        internalClock = clock;
    }

    public void setPseudorange(double pseudorange) {
        this.pseudorange = pseudorange;
    }

    public double getPseudorange(){
        return pseudorange;
    }

    public double getPseudorangeInKilometers(){
        return pseudorange/1000;
    }
}
