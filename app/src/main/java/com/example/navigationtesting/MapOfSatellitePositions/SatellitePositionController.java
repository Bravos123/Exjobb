package com.example.navigationtesting.MapOfSatellitePositions;

import android.util.Log;

import com.example.navigationtesting.Satellite.LatLngAlt;
import com.example.navigationtesting.httpClient.OkHttpClientHandler;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SatellitePositionController {
    public static final int REFRESH_TIME_MS = 3000;
    private HashMap<Integer, JSONObject> satellitePositionPredictions;

    private HashMap<Integer, LatLng> previouslySentLongLatBuffer;//In case we can't find the current unix time position to send back, send back the previous position
    private HashMap<Integer, LatLngAlt> previouslySentLatLngAltBuffer;

    private Timer scheduleLoadMorePredictions;
    private Thread updateThread;

    private OnSatellitePositionControllerReadyCallback callback;
    private boolean firstTimeLoading = true;

    private JSONArray availibleSatellites;


    private String apiHost = "http://178.62.193.218:8080";//"http://192.168.0.156:8888"


    public SatellitePositionController(OnSatellitePositionControllerReadyCallback callback){
        satellitePositionPredictions = new HashMap<>();
        previouslySentLongLatBuffer = new HashMap<>();
        previouslySentLatLngAltBuffer = new HashMap<>();
        this.callback = callback;
        scheduleLoadMorePredictions = new Timer();
        updateThread = new Thread();
    }


    public void initialize(){
        if(availibleSatellites == null){
            //83.255.110.186
            sendRequest(apiHost+"/SatellitesNavigationApi/retrieveAvailibleSatellites", new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    //e.printStackTrace();
                    Log.i("Project","Failed getting availible satellites. Try again");
                    initialize();
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if(response.code() == 500){//Retry on failure
                            Log.i("Project","Failed getting availible satellites. Try again");
                            initialize();
                        }
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        try {
                            String responseText = responseBody.string();
                            Log.i("Project", "Availible satellites: "+responseText);
                            availibleSatellites = new JSONArray(responseText);
                            //availibleSatellites = new JSONArray("[40128]");//testing
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(availibleSatellites == null){
                            Log.i("Project","Failed getting availible satellites. Try again");
                            initialize();
                        }
                        downloadPredictionData();
                    }
                }
            });
        }

    }




    private void downloadPredictionData(){
        for(int i=0; i<availibleSatellites.length(); i++){
            int satelliteNoradId;

            try {
                satelliteNoradId = availibleSatellites.getInt(i);
                retrieveSatellitePredictions(satelliteNoradId);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scheduleLoadMorePredictions.schedule(new TimerTask() {
            @Override
            public void run() {
                downloadPredictionData();
            }
        }, REFRESH_TIME_MS);
    }


    private void checkFirstBufferDownloadIsDone(){
        if(firstTimeLoading && allSatellitesHaveBuffers()){
            firstTimeLoading = false;
            callback.onSatellitePositionControllerReadyCallback();
        }
    }

    private void retrieveSatellitePredictions(final int noradId){
        //Log.i("Project", "Send this request: "+"http://83.255.110.186/SatellitesNavigationApi/retrieveSatellitePosition?NORADID="+noradId);
        //83.255.110.186
        sendRequest(apiHost+"/SatellitesNavigationApi/retrieveSatellitePosition?NORADID="+Integer.toString(noradId),
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.i("Project", "Failure. Try again");
                        retrieveSatellitePredictions(noradId);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        try (ResponseBody responseBody = response.body()) {
                            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                            String responseText = responseBody.string();
                            //Log.i("Project", "Response for satellite: "+noradId);

                            JSONObject newPositions = new JSONObject(responseText);

                            if(satellitePositionPredictions.get(noradId) == null){
                                satellitePositionPredictions.put(noradId, newPositions);
                            }else{
                                if(newContainerHasMoreFuturePredictions(
                                        satellitePositionPredictions.get(noradId).getJSONObject("positions").keys(),
                                        newPositions.getJSONObject("positions").keys())){
                                    satellitePositionPredictions.replace(noradId, new JSONObject(responseText));
                                }
                            }

                            /*Trigger a request for satellites current position. This also updates
                            the previously retrieved position*/
                            getSatelliteCoordinates(noradId);
                            //Log.i("Project", "downloaded:\n"+satellitePositionPredictions.get(noradId).toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        checkFirstBufferDownloadIsDone();

                    }
                });
    }

    public boolean newContainerHasMoreFuturePredictions(Iterator<String> positionsOld, Iterator<String> positionsNew){
        //Check if this new positions container contains predictions that are
        // further into the future the future than what is currently being used
        //This is because new positions are loaded every 5 seconds without any order
        long furthestPredictionNew = 0;
        while(positionsNew.hasNext()){
            long pp = Long.parseLong(positionsNew.next());
            if(furthestPredictionNew < pp){
                furthestPredictionNew = pp;
            }
        }

        long furthestPredictionOld = 0;
        while(positionsOld.hasNext()){
            long pp = Long.parseLong(positionsOld.next());
            if(furthestPredictionOld < pp){
                furthestPredictionOld = pp;
            }
        }
        if(furthestPredictionNew > furthestPredictionOld){
            return true;
        }
        return false;
    }




    private boolean allSatellitesHaveBuffers(){
        for(int i=0; i<availibleSatellites.length(); i++){
            try {
                if(!satellitePositionPredictions.containsKey(availibleSatellites.getInt(i))){
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }







    public Set<Integer> getNORADSatelliteList() {
        return satellitePositionPredictions.keySet();
    }

    public LatLng getSatelliteCoordinates(int targetNoradId){
        long unixTime = System.currentTimeMillis() / 1000L;

        //Log.i("Project", "Satellite positions has "+targetNoradId+": "+satellitePositionPredictions.containsKey(targetNoradId));
        try {
            if(satellitePositionPredictions.get(targetNoradId).getJSONObject("positions").has(Long.toString(unixTime))){
                LatLng returnPos;

                returnPos = new LatLng(
                        satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                                .getJSONObject(Long.toString(unixTime)).getDouble("lat"),
                        satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                                .getJSONObject(Long.toString(unixTime)).getDouble("lon"));

                if(previouslySentLongLatBuffer.containsKey(targetNoradId)){
                    previouslySentLongLatBuffer.put(targetNoradId, returnPos);
                }else{
                    previouslySentLongLatBuffer.replace(targetNoradId, returnPos);
                }

                return returnPos;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return previouslySentLongLatBuffer.get(targetNoradId);

    }


    public LatLngAlt getSatelliteLatLongAlt(int targetNoradId){
        long unixTime = System.currentTimeMillis() / 1000L;

        //Log.i("Project", "Satellite positions has "+targetNoradId+": "+satellitePositionPredictions.containsKey(targetNoradId));
        try {
            if(satellitePositionPredictions.get(targetNoradId).getJSONObject("positions").has(Long.toString(unixTime))){
                LatLngAlt returnPos;

                returnPos = new LatLngAlt(
                        satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                                .getJSONObject(Long.toString(unixTime)).getDouble("lat"),
                        satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                                .getJSONObject(Long.toString(unixTime)).getDouble("lon"),
                        satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                                .getJSONObject(Long.toString(unixTime)).getDouble("alt"));

                if(previouslySentLatLngAltBuffer.containsKey(targetNoradId)){
                    previouslySentLatLngAltBuffer.put(targetNoradId, returnPos);
                }else{
                    previouslySentLatLngAltBuffer.replace(targetNoradId, returnPos);
                }

                return returnPos;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return previouslySentLatLngAltBuffer.get(targetNoradId);
    }


    public void sendRequest(String requestUrl, Callback cB){
        updateThread = new Thread(){
            @Override
            public void run(){
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .build();
                OkHttpClientHandler.getHttpClient().newCall(request).enqueue(cB);
            }
        };
        updateThread.run();
    }



}
