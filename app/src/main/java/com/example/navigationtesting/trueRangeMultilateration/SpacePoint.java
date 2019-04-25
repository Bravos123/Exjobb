package com.example.navigationtesting.trueRangeMultilateration;

import com.google.android.gms.maps.model.LatLng;

public class SpacePoint {
    private double x;
    private double y;
    private double z;
    private double pseudorangeToTarget;

    public SpacePoint(double latitude, double longitude, double altitude, double pseudorange){
        double[] cartesianPositions = ellipsoidalToCartesianCoords(latitude, longitude, altitude);
        x = cartesianPositions[0];
        y = cartesianPositions[1];
        z = cartesianPositions[2];
        pseudorangeToTarget = pseudorange;
    }



    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getZ(){
        return z;
    }

    public double getPseudorangeToTarget(){
        return pseudorangeToTarget;
    }




    private double[] ellipsoidalToCartesianCoords(double latitude, double longitude, double h){
        /*
        * Algorithm implemented from:
        * https://gssc.esa.int/navipedia/index.php/Ellipsoidal_and_Cartesian_Coordinates_Conversion
        * */
        //WGS84 parameters (in meters): https://gssc.esa.int/navipedia/index.php/Reference_Frames_in_GNSS
        double a = 6378137.0;
        double f = 1.0/298.257223563;
        double b = a*(1.0-f);
        double e2=(Math.pow(a, 2)-Math.pow(b, 2))/Math.pow(a, 2);

        double N = a/(Math.sqrt(1-e2*Math.pow(Math.sin(latitude), 2)));

        double xPos = (N + h) * Math.cos(latitude) * Math.cos(longitude);
        double yPos = (N + h) * Math.cos(latitude) * Math.sin(longitude);
        double zPos = ((1-e2) * N + h) * Math.sin(latitude);

        return new double[]{xPos, yPos, zPos};

    }

}
