package com.example.navigationtesting.showMyLocation;

import android.content.Context;

import com.example.navigationtesting.MapOfSatellitePositions.OnSatellitePositionControllerReadyCallback;
import com.example.navigationtesting.MapOfSatellitePositions.SatellitePositionController;
import com.example.navigationtesting.SatellitePseudorangeController.OnPseudorangeControllerReadyCallback;
import com.example.navigationtesting.SatellitePseudorangeController.PseudorangeController;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class UserLocationGenerator implements OnSatellitePositionControllerReadyCallback, OnPseudorangeControllerReadyCallback {
    private LatLng lastKnownPosition;
    private SatellitePositionController satelliteController;
    private boolean satellitePositionsReady = false;

    private PseudorangeController pseudorangeController;
    private boolean pseudorangeControllerReady = false;

    private ArrayList<OnUserLocationGeneratorNewPosition> callbacksForNewPositions;


    public UserLocationGenerator(Context c){
        satelliteController = new SatellitePositionController( this);
        satelliteController.initialize();

        pseudorangeController = new PseudorangeController(c, this);
    }

    public void addNewPositionCallback(OnUserLocationGeneratorNewPosition callback){
        callbacksForNewPositions.add(callback);
    }


    @Override
    public void onSatellitePositionControllerReadyCallback() {
        satellitePositionsReady = true;
        pseudorangeController.setAvailibleSatellitesNORADList(satelliteController.getNORADSatelliteList());
        checkCanProceed();
    }

    @Override
    public void onPseudorangeControllerReadyCallback() {
        pseudorangeControllerReady = true;
        checkCanProceed();
    }


    private void checkCanProceed(){
        if(satellitePositionsReady && pseudorangeControllerReady){
            calculateCoordinates();
            for(OnUserLocationGeneratorNewPosition callback : callbacksForNewPositions){
                callback.onUserLocationGeneratorNewPosition(lastKnownPosition);
            }
        }
    }


    private void calculateCoordinates(){


    }

}
