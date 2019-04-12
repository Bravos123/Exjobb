package com.example.navigationtesting.SatelliteMVC;

import android.util.Log;

import com.example.navigationtesting.callbacks.CallbackBoolean;
import com.example.navigationtesting.callbacks.CallbackJsonArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class SatellitesFutureCoordinateBuffer implements OnUpdateSatellitesFuturePositionCallback {
    private UpdateSatellitesFuturePosition futureSatellitePositionUpdater;
    //             NORAD ID       UNIX TIME            Satellite coords
    private HashMap<Integer, TreeMap<Long, SatelliteCoordsSimple>> coordinateBuffer;
    private Date dateSinceEphemerisUpdate;

    private CallbackBoolean callbackBoolean;

    private int targetMinBufferSize;

    private Timer scheduleLoadMorePredictions;

    private boolean upstartDone = false;

    public SatellitesFutureCoordinateBuffer(CallbackBoolean callback, int bufferSizeMultiplier){
        if(bufferSizeMultiplier <= 0){
            bufferSizeMultiplier = 1;
        }
        targetMinBufferSize = 200*bufferSizeMultiplier;
        coordinateBuffer = new HashMap<>();
        scheduleLoadMorePredictions = new Timer();
        callbackBoolean = callback;
        dateSinceEphemerisUpdate = new Date();

        futureSatellitePositionUpdater = new UpdateSatellitesFuturePosition(this);
    }

    public void startNewBufferFromEphemerides(ArrayList<Satellite> newSatts){
        long unixTime = System.currentTimeMillis() / 1000L;
        if(!upstartDone){
            ArrayList<SatelliteCoordsSimple> startCoordsSattelites = new ArrayList<>();
            Log.i("Project", "newSatts.size: "+newSatts.size());
            long oldDataOffset = -1;
            for(Satellite s : newSatts){
                SatelliteCoordsSimple coords = new SatelliteCoordsSimple(s.getLatitude(),
                        s.getLongitude(), s.getAltitude(), s.getEphemeridesUnixTime(), s.getNoradId());
                coordinateBuffer.put(s.getNoradId(), new TreeMap<>());
                coordinateBuffer.get(s.getNoradId()).put(coords.getUnixTime(), coords);
                if(oldDataOffset == -1 || oldDataOffset < (unixTime - s.getEphemeridesUnixTime())){
                    oldDataOffset = (unixTime - s.getEphemeridesUnixTime());
                }
                Log.i("Project", "start position is "+(s.getEphemeridesUnixTime())+" seconds from the past");
            }

            //Since the ephemerides data is old we first need to load satellites position to catch up with the current unix time
            int timeItTakesToDownloadPackage = 6;
            int numberOfUpdatesNeeded = Math.round(oldDataOffset/(300-timeItTakesToDownloadPackage));
            Log.i("Project", "Need to make roughlt "+numberOfUpdatesNeeded+" requests to catch up to current unix time");


            //futureSatellitePositionUpdater.updateSatelliteArrayIfAvailible(startCoordsSattelites);
            retrieveMorePredictionData();
        }
    }


    private void retrieveMorePredictionData(){
        long currentUnixTime = (System.currentTimeMillis() / 1000L);
        ArrayList<SatelliteCoordsSimple> startCoordsSattelites = new ArrayList<>();

        Set<Integer> noradIds = coordinateBuffer.keySet();

        for(Integer noradId : noradIds){
            Object[] unixTimes = coordinateBuffer.get(noradId).keySet().toArray();

            SatelliteCoordsSimple s = coordinateBuffer.get(noradId).get(unixTimes[unixTimes.length-1]);

            long diff = (currentUnixTime - (long)unixTimes[unixTimes.length-1]);
            Log.i("Project", "base unix time: "+unixTimes[unixTimes.length-1]+"       is "+(diff/60)+" min old");

            startCoordsSattelites.add(s);
        }

        futureSatellitePositionUpdater.updateSatelliteArrayIfAvailible(startCoordsSattelites);
    }

    private boolean isBufferIsBigEnough(){
        //This also removes outdated predictions
        long unixTime = System.currentTimeMillis() / 1000L;

        Set<Integer> noradIds = coordinateBuffer.keySet();
        int smallesUseableBuffer = 0;
        boolean projectionsAreUpToDate = true;
        for(int noradId : noradIds){
            int futureCoordinatesStillUseable = 0;
            //ArrayList<SatelliteCoordsSimple> coordsArr = coordinateBuffer.get(noradId);
            TreeMap<Long, SatelliteCoordsSimple> coordsHashMap = coordinateBuffer.get(noradId);
            Set<Long> unixTimes = coordsHashMap.keySet();

            ArrayList<Long> oldUnixTimes = new ArrayList<>();
            for(Long projectedUnixTime : unixTimes){

                if(projectedUnixTime >= unixTime){
                    futureCoordinatesStillUseable++;
                }else{

                    oldUnixTimes.add(projectedUnixTime);
                }
            }
            if(futureCoordinatesStillUseable == 0){
                projectionsAreUpToDate = false;
            }

            //remove all old projections except the last which will be our next base projection to continue our satellite projection from
            while(oldUnixTimes.size() > 1){
                coordinateBuffer.get(noradId).remove(oldUnixTimes.get(0));
                oldUnixTimes.remove(0);
            }

            if(smallesUseableBuffer == 0 || smallesUseableBuffer > futureCoordinatesStillUseable){
                smallesUseableBuffer = futureCoordinatesStillUseable;
            }
        }

        //Log.i("Project", "Buffer test: "+smallesUseableBuffer+" < "+targetMinBufferSize);
        if(smallesUseableBuffer < targetMinBufferSize || projectionsAreUpToDate == false){
            if(smallesUseableBuffer < targetMinBufferSize){
                Log.i("Project", "Projection buffer is too small");
            }
            if(projectionsAreUpToDate == false){
                Log.i("Project", "Projection buffer is too small");
            }
            return false;
        }

        return true;
    }




    public Set<Integer> getCoordinateBufferKeyset(){
        return coordinateBuffer.keySet();
    }

    public void setSatellitePseudorande(int svid, double pseudorange){
        long currentUnixTime = System.currentTimeMillis() / 1000L;
        TreeMap<Long, SatelliteCoordsSimple> storedPositions;
        //set new pseudorange to each satellite with unix time "currentUnixTime" and after that time
        try {
            storedPositions = coordinateBuffer.get(SatelliteNORADId.getGalileoNORADId(svid));
            Set<Long> unixTimes = storedPositions.keySet();

            for(Long ut : unixTimes){
                if(ut >= currentUnixTime){
                    storedPositions.get(ut).setPseudorange(pseudorange);
                }
            }

        } catch (NoradIdDoesNotExist noradIdDoesNotExist) {
            Log.i("Project", "setSatellitePseudorande");
            noradIdDoesNotExist.printStackTrace();
        }

    }


    public double getSatelliteLatitudeFromUnixtime(int noradId, long unixTargetTime, boolean pseudorandeSetIsRequired){
        return getDataFromPredictionSet(noradId, unixTargetTime, "latitude", pseudorandeSetIsRequired);
    }

    public double getSatelliteLongitudeFromUnixtime(int noradId, long unixTargetTime, boolean pseudorandeSetIsRequired){
        return getDataFromPredictionSet(noradId, unixTargetTime, "longitude",pseudorandeSetIsRequired);
    }

    public double getSatelliteAltitudeFromUnixtime(int noradId, long unixTargetTime, boolean pseudorandeSetIsRequired){
        return getDataFromPredictionSet(noradId, unixTargetTime, "altitude", pseudorandeSetIsRequired);
    }

    public double getDataFromPredictionSet(int noradId, long unixTargetTime, String dataType, boolean pseudorandeSetIsRequired){
        Set<Long> storedFutureUnixTimes = coordinateBuffer.get(noradId).keySet();
        //CHeck if target unixtime matches some stored unix time exactly
        long closestUnixTimeMatch = 0;
        long tempNearestunixMatchDelta = 0;
        int checksDone = 0;

        for(Long l : storedFutureUnixTimes){
            if(l == unixTargetTime){
                if((pseudorandeSetIsRequired && coordinateBuffer.get(noradId).get(unixTargetTime).getPseudorange() > 0) || pseudorandeSetIsRequired == false){
                    if(dataType.equals("latitude")){
                        return coordinateBuffer.get(noradId).get(unixTargetTime).getLatitude();
                    }else if(dataType.equals("longitude")){
                        return coordinateBuffer.get(noradId).get(unixTargetTime).getLongitude();
                    }else if(dataType.equals("altitude")){
                        return coordinateBuffer.get(noradId).get(unixTargetTime).getAltitude();
                    }
                }
            }

            checksDone++;

            if(Math.abs(unixTargetTime - l) < tempNearestunixMatchDelta || closestUnixTimeMatch == 0){
                if((pseudorandeSetIsRequired && coordinateBuffer.get(noradId).get(unixTargetTime).getPseudorange() > 0) || pseudorandeSetIsRequired == false){
                    closestUnixTimeMatch = l;
                    tempNearestunixMatchDelta = Math.abs(unixTargetTime - l);
                }

            }
        }

        /*if(checksDone >= storedFutureUnixTimes.size()){
            Log.i("Project", "Target unix time does not exist load more data");
            //user is probably trying to see predictions we haven't loaded yet, load more prediction data.
            retrieveMorePredictionData();
        }*/

        //No perfect match, use closest unix time instead
        if(closestUnixTimeMatch != 0){
            if(dataType.equals("latitude")){
                return coordinateBuffer.get(noradId).get(closestUnixTimeMatch).getLatitude();
            }else if(dataType.equals("longitude")){
                return coordinateBuffer.get(noradId).get(closestUnixTimeMatch).getLongitude();
            }else if(dataType.equals("altitude")){
                return coordinateBuffer.get(noradId).get(closestUnixTimeMatch).getAltitude();
            }
        }


        Log.i("Project", "getDataFromPredictionSet dataType string is not valid: "+dataType+"  satellites have no pseudorange with you set to required potentially");
        return -1;
    }


    private void insertNewProjectedCoordinates(int noradId, double latitude, double longitude, double altitude, long targetUnixTime){
        SatelliteCoordsSimple coords = new SatelliteCoordsSimple(latitude, longitude, altitude, targetUnixTime, noradId);
        coordinateBuffer.get(noradId).put(targetUnixTime, coords);
    }


    @Override
    public void onUpdateSatellitesFuturePositionCallback(JSONArray jsonArray) {
        for(int i=0; i<jsonArray.length(); i++){
            int noradId = 0;
            try {
                noradId = jsonArray.getJSONObject(i).getJSONObject("info").getInt("satid");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Object[] keyset = coordinateBuffer.get(noradId).keySet().toArray();
            long baseUnixTime = (long)keyset[keyset.length-1];
            try {
                JSONArray arrayOfPositions = jsonArray.getJSONObject(i).getJSONArray("positions");
                for(int ai=0; ai<arrayOfPositions.length(); ai++){
                    insertNewProjectedCoordinates(
                            noradId,
                            arrayOfPositions.getJSONObject(ai).getDouble("satlatitude"),
                            arrayOfPositions.getJSONObject(ai).getDouble("satlongitude"),
                            arrayOfPositions.getJSONObject(ai).getDouble("sataltitude"),
                            baseUnixTime+(ai+1));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(isBufferIsBigEnough()){
            if(!upstartDone){
                callbackBoolean.callBack(true);
                upstartDone = true;
            }
            scheduleLoadMorePredictions.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!isBufferIsBigEnough()){
                        retrieveMorePredictionData();
                    }
                }
            }, 100*1000);
        }else{
            //Log.i("Project", "Buffer is not big enough, load more!");
            retrieveMorePredictionData();
        }

    }




}
