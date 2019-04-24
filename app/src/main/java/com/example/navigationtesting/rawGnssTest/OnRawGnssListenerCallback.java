package com.example.navigationtesting.rawGnssTest;

import com.example.navigationtesting.Satellite.Satellite;

import java.util.ArrayList;

public interface OnRawGnssListenerCallback {
    void onRawGnssListenerCallback(ArrayList<Satellite> arrOfSatellites);
}
