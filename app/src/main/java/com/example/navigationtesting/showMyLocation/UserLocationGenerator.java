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
import com.example.navigationtesting.trueRangeMultilateration.SolvePositionSystem;

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
        //Log.i("Project", "Done");
        lastKnownPosition = calculateCoordinates();
        if(lastKnownPosition != null){
            for(OnUserLocationGeneratorNewPosition callback : callbacksForNewPositions){
                callback.onUserLocationGeneratorNewPosition(lastKnownPosition);
            }
        }

    }

    private void checkCanProceed(){
        if(satellitePositionsReady && pseudorangeControllerReady){
            generateNewPosition();
        }
    }


    private LatLngAlt calculateCoordinates(){
        //Log.i("Project", "\n\n");
        ArrayList<Pair<Integer, Double>> arrayOfSatellitePseudoranges = pseudorangeController.getArrayOfSatellitePseudoranges();
        if(arrayOfSatellitePseudoranges.size() > 2){
            ArrayList<SpacePoint> spacePointParameters = new ArrayList<>();
            //Log.i("Project", "arrayOfSatellitePseudoranges: "+arrayOfSatellitePseudoranges.size());
            for(Pair<Integer, Double> p : arrayOfSatellitePseudoranges){
                //Log.i("Project", "Pseudorange: "+p.second);
                LatLngAlt satCoordsEllipsoid = satelliteController.getSatelliteLatLongAlt(p.first);
                //Log.i("Project", satCoordsEllipsoid.toString());
                //Log.i("Project", "satCoordsEllipsoid: "+satCoordsEllipsoid.length);
                if(satCoordsEllipsoid != null){
                    SpacePoint sp = new SpacePoint(satCoordsEllipsoid.latitude(), satCoordsEllipsoid.longitude(), satCoordsEllipsoid.altitude(), p.second);
                    spacePointParameters.add(sp);
                    if(spacePointParameters.size() == 3){
                        break;
                    }
                }

            }

            if(spacePointParameters.size() >= 3){
                return SolvePositionSystem.calculateSpacePoint(spacePointParameters);
            }


        }else{
            //Log.i("Project", "Too few satellites to calculate: "+arrayOfSatellitePseudoranges.size());
        }

        return null;
    }

}
