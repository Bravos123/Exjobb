package com.example.navigationtesting;

import android.os.AsyncTask;
import android.util.Log;

import com.example.navigationtesting.callbacks.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpRequest extends AsyncTask<String, Void, JSONArray> {
    private OnHttpRequestCallback callback;
    private int poolOrder;

    public HttpRequest(OnHttpRequestCallback cb, int poolOrder){
        callback = cb;
        this.poolOrder = poolOrder;
    }

    @Override
    protected JSONArray doInBackground(String... strings) {
        Log.i("Project", "Loading http request");
        JSONArray container = new JSONArray();

        try {
            int counter = 0;
            for(String s : strings){
                URL url = new URL(s);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                String jsonString = "";
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while((line = br.readLine()) != null){
                    jsonString += line;
                }



                container.put(counter, new JSONObject(jsonString));
                counter++;

                Log.i("Project", counter+"/"+strings.length);
            }




        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("Project", "Http request done");
        return container;
    }


    @Override
    public void onPostExecute(JSONArray jsonArr){
        callback.OnHttpRequestCallback(poolOrder, jsonArr);
    }


}
