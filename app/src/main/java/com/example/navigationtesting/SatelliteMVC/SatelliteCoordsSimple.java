package com.example.navigationtesting.SatelliteMVC;

public class SatelliteCoordsSimple {

    private double longitude;
    private double latitude;
    private double altitude;

    private int noradId;

    private long unixTime;

    private double pseudorange = 0;

    public SatelliteCoordsSimple(double latitude, double longitude, double altitude, long unixTime, int noradId){
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.unixTime = unixTime;
        this.noradId = noradId;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public long getUnixTime() {
        return unixTime;
    }

    public int getNoradId() {
        return noradId;
    }

    public void setPseudorange(double pr){
        pseudorange = pr;
    }

    public double getPseudorange(){
        return pseudorange;
    }

}
