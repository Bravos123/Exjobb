package com.example.navigationtesting.httpClient;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpClientHandler {
    private static OkHttpClient httpClient;

    static public OkHttpClient getHttpClient() {
        if(httpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(60, TimeUnit.SECONDS);
            builder.readTimeout(60, TimeUnit.SECONDS);
            builder.writeTimeout(60, TimeUnit.SECONDS);
            builder.retryOnConnectionFailure(true);

            httpClient = builder.build();
        }
        return httpClient;
    }
}
