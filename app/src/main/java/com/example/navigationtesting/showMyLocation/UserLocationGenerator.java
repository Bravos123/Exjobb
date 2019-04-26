package com.example.navigationtesting.showMyLocation;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.navigationtesting.MapOfSatellitePositions.OnSatellitePositionControllerReadyCallback;
import com.example.navigationtesting.MapOfSatellitePositions.SatellitePositionController;
import com.example.navigationtesting.SatellitePseudorangeController.OnPseudorangeControllerReadyCallback;
import com.example.navigationtesting.SatellitePseudorangeController.PseudorangeController;
import com.example.navigationtesting.Satellite.LatLngAlt;
import com.example.navigationtesting.trueRangeMultilateration.SpacePoint;
import com.example.navigationtesting.trueRangeMultilateration.BancroftMethod;

import java.util.ArrayList;

public class UserLocationGenerator implements OnSatellitePositionControllerReadyCallback, OnPseudorangeControllerReadyCallback {
    private LatLngAlt lastKnownPosition;
    private SatellitePositionController satelliteController;
    private boolean satellitePositionsReady = false;

    private PseudorangeController pseudorangeController;
    private boolean pseudorangeControllerReady = false;

    private ArrayList<OnUserLocationGeneratorNewPosition> callbacksForNewPositions;


    public UserLocationGenerator(Context c){
        callbacksForNewPositions = new ArrayList<>();
        satelliteController = new SatellitePositionController( this);
        satelliteController.initialize();

        pseudorangeController = new PseudorangeController(c, this);
    }

    public void addNewPositionCallback(OnUserLocationGeneratorNewPosition callback){
        callbacksForNewPositions.add(callback);
    }


    @Override
    public void onSatellitePositionControllerReadyCallback() {
        if(satellitePositionsReady == false){
            satellitePositionsReady = true;
            pseudorangeController.setAvailibleSatellitesNORADList(satelliteController.getNORADSatelliteList());
            checkCanProceed();
        }

    }

    @Override
    public void onPseudorangeControllerReadyCallback() {
        if(pseudorangeControllerReady == false){
            pseudorangeControllerReady = true;
            checkCanProceed();
        }else{
            generateNewPosition();
        }
    }


    private void generateNewPosition(){
        calculateCoordinates();
        for(OnUserLocationGeneratorNewPosition callback : callbacksForNewPositions){
            callback.onUserLocationGeneratorNewPosition(lastKnownPosition);
        }
    }

    private void checkCanProceed(){
        if(satellitePositionsReady && pseudorangeControllerReady){
            generateNewPosition();
        }
    }


    private void calculateCoordinates(){
        Log.i("Project", "--------------------------------------");
        ArrayList<Pair<Integer, Double>> arrayOfSatellitePseudoranges = pseudorangeController.getArrayOfSatellitePseudoranges();
        if(arrayOfSatellitePseudoranges.size() > 3){
            ArrayList<SpacePoint> spacePointParameters = new ArrayList<>();
            for(Pair<Integer, Double> p : arrayOfSatellitePseudoranges){
                double[] satCoordsEllipsoid = satelliteController.getSatelliteLatLongAlt(p.first);
                SpacePoint sp = new SpacePoint(satCoordsEllipsoid[0], satCoordsEllipsoid[1], satCoordsEllipsoid[2], p.second);
                spacePointParameters.add(sp);
            }
            lastKnownPosition = BancroftMethod.calculateSpacePoint(spacePointParameters);

        }


    }

}
