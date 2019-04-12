package com.example.navigationtesting.SatelliteMVC;

import android.util.Log;

public class SatelliteView {

    public void printSatelliteInfo(Satellite sat){
        Log.i("Project", Integer.toString(sat.getEphemerides().getSvid()));
    }


}
