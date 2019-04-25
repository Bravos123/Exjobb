package com.example.navigationtesting.MapOfSatellitePositions;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SatellitePositionController {
    public static final int REFRESH_TIME_MS = 2000;
    private HashMap<Integer, JSONObject> satellitePositionPredictions;
    private HashMap<Integer, LatLng> previouslySentLongLatBuffer;//In case we can't find the current unix time position to send back, send back the previous position
    private Timer scheduleLoadMorePredictions;

    private OnSatellitePositionControllerReadyCallback callback;
    private boolean firstTimeLoading = true;

    private int requestsPending = 0;

    private JSONArray availibleSatellites;

    private final OkHttpClient client = new OkHttpClient();

    private String apiHost = "http://178.62.193.218:8080";//"http://192.168.0.156:8888"


    public SatellitePositionController(OnSatellitePositionControllerReadyCallback callback){
        satellitePositionPredictions = new HashMap<>();
        previouslySentLongLatBuffer = new HashMap<>();
        this.callback = callback;
        scheduleLoadMorePredictions = new Timer();
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
        //Log.i("Project", "Download more prediction data");
        //Get how many satellites are availible
        if(requestsPending != 0){
            return;
        }
        requestsPending = availibleSatellites.length();
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

                            String responseTExt = responseBody.string();
                            //Log.i("Project", "Response for satellite: "+noradId);
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

                            /*Trigger a request for satellites current position. This also updates
                            the previously retrieved position*/
                            getSatelliteCoordinates(noradId);
                            //Log.i("Project", "downloaded:\n"+satellitePositionPredictions.get(noradId).toString());

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
        }, REFRESH_TIME_MS);
    }










    public Set<Integer> getNORADSatelliteList() {
        return satellitePositionPredictions.keySet();
    }

    public LatLng getSatelliteCoordinates(int targetNoradId){
        long unixTime = System.currentTimeMillis() / 1000L;

        try {

            LatLng returnPos;
            long targetUnixTime = unixTime;

            returnPos = new LatLng(
                    satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                            .getJSONObject(Long.toString(targetUnixTime)).getDouble("lat"),
                    satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                            .getJSONObject(Long.toString(targetUnixTime)).getDouble("lon"));

            if(previouslySentLongLatBuffer.containsKey(targetNoradId)){
                previouslySentLongLatBuffer.put(targetNoradId, returnPos);
            }else{
                previouslySentLongLatBuffer.replace(targetNoradId, returnPos);
            }

            return returnPos;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return previouslySentLongLatBuffer.get(targetNoradId);

    }


    public double[] getSatelliteLatLongAlt(int targetNoradId){
        long unixTime = System.currentTimeMillis() / 1000L;

        try {

            LatLng returnPos;
            long targetUnixTime = unixTime;


            double latitude = satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                    .getJSONObject(Long.toString(targetUnixTime)).getDouble("lat");
            double longitude = satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                    .getJSONObject(Long.toString(targetUnixTime)).getDouble("lon");
            double altitude = satellitePositionPredictions.get(targetNoradId).getJSONObject("positions")
                    .getJSONObject(Long.toString(targetUnixTime)).getDouble("alt");
            return new double[]{latitude, longitude, altitude};


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new double[0];
    }


    public void sendRequest(String requestUrl, Callback cB){
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        client.newCall(request).enqueue(cB);
    }



}
