package com.example.navigationtesting.SatelliteEphemerides;

import com.example.navigationtesting.SatelliteMVC.Satellite;

import java.util.ArrayList;

public interface OnRetrieveSatelliteEphemeridesCallback {
    void Callback(String lastModified, ArrayList<Satellite> arrayList);
}
