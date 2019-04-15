package com.example.navigationtesting.MapOfSatellitePositions;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SatellitePositionController {
    private HashMap<String, JSONObject> satellitePositionPredictions;
    private Timer scheduleLoadMorePredictions;

    private OnSatellitePositionControllerReadyCallback callback;
    private boolean firstTimeLoading = true;

    private int requestsPending = 0;

    private JSONArray availibleSatellites;

    private final OkHttpClient client = new OkHttpClient();


    public SatellitePositionController(OnSatellitePositionControllerReadyCallback callback){
        satellitePositionPredictions = new HashMap<>();
        this.callback = callback;
        scheduleLoadMorePredictions = new Timer();
    }


    public void initialize(){
        sendRequest("http://83.255.110.186/SatelliteNavigation/retrieveAvailibleSatellites", new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    try {
                        String responseText = responseBody.string();
                        Log.i("Project", "Availible satellites: "+responseText);
                        availibleSatellites = new JSONArray(responseText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    downloadPredictionData();
                }
            }
        });
    }




    private void downloadPredictionData(){

        //Get how many satellites are availible
        for(int i=0; i<availibleSatellites.length(); i++){
            String satelliteNoradId;

            try {
                satelliteNoradId = availibleSatellites.getString(i);
                retrieveSatellitePredictions(satelliteNoradId);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    private void retrieveSatellitePredictions(final String noradId){
        requestsPending++;
        Log.i("Project", "Send this request: "+"http://83.255.110.186/SatelliteNavigation/retrieveSatellitePosition?NORADID="+noradId);
        sendRequest("http://83.255.110.186/SatelliteNavigation/retrieveSatellitePosition?NORADID="+noradId,
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.i("Project", "Failure");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        try (ResponseBody responseBody = response.body()) {
                            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                            String responseTExt = responseBody.string();
                            Log.i("Project", "Response");
                            if(satellitePositionPredictions.get(noradId) == null){
                                try {
                                    satellitePositionPredictions.put(noradId, new JSONObject(responseTExt));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                try {
                                    satellitePositionPredictions.replace(noradId, new JSONObject(responseTExt));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            requestsPending--;

                            if(requestsPending <= 0){
                                requestsPending = 0;
                                requestsAreComplete();
                            }
                        }



                    }
                });
    }

    private void requestsAreComplete(){
        if(firstTimeLoading){
            firstTimeLoading = false;
            callback.onSatellitePositionControllerReadyCallback();
        }
        scheduleLoadMorePredictions.schedule(new TimerTask() {
            @Override
            public void run() {
                downloadPredictionData();
            }
        }, 10*1000);
    }










    public Set<String> getNORADSatelliteList() {
        return satellitePositionPredictions.keySet();
    }

    public double getSatelliteLongitude(String targetNoradId) {
        long unixTime = System.currentTimeMillis() / 1000L;
        try {
            long checkID = Long.parseLong(targetNoradId);
            while(!satellitePositionPredictions.containsKey(Long.toString(checkID))){
                checkID++;
            }
            return satellitePositionPredictions.get(Long.toString(checkID)).getJSONObject("positions")
                    .getJSONObject(Long.toString(unixTime)).getDouble("lon");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getSatelliteLatitude(String targetNoradId) {
        long unixTime = System.currentTimeMillis() / 1000L;
        try {
            long checkID = Long.parseLong(targetNoradId);
            while(!satellitePositionPredictions.containsKey(Long.toString(checkID))){
                checkID++;
            }
            return satellitePositionPredictions.get(Long.toString(checkID)).getJSONObject("positions")
                    .getJSONObject(Long.toString(unixTime)).getDouble("lat");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public void sendRequest(String requestUrl, Callback cB){
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        client.newCall(request).enqueue(cB);
    }



}
