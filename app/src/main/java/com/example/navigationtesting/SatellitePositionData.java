package com.example.navigationtesting;

import android.renderscript.Float3;

public class SatellitePositionData {
    /*private double satelliteTime;
    private double pseudorange;
    private double Tk;
    private double Ek;
    private double Vk;
    private double Uk;
    private double Rk;
    private double Ik;
    private double lambdaK;*/
    private double[] cartesianCoords;
    private double[] ellipsoidalCoords;


    public SatellitePositionData(double[] cartesianCoords, double[] ellipsoidalCoords){
        /*this.satelliteTime = satelliteTime;
        this.pseudorange = pseudorange;
        this.Tk = Tk;
        this.Ek = Ek;
        this.Vk = Vk;
        this.Uk = Uk;
        this.Rk = Rk;
        this.Ik = Ik;
        this.lambdaK = lambdaK;*/
        this.cartesianCoords = cartesianCoords;
        this.ellipsoidalCoords = ellipsoidalCoords;
    }

    /*public double getSatelliteTime() {
        return satelliteTime;
    }

    public double getPseudorange() {
        return pseudorange;
    }

    public double getTk() {
        return Tk;
    }

    public double getEk() {
        return Ek;
    }

    public double getVk() {
        return Vk;
    }

    public double getUk() {
        return Uk;
    }

    public double getRk() {
        return Rk;
    }

    public double getIk() {
        return Ik;
    }

    public double getLambdaK() {
        return lambdaK;
    }*/

    public double[] getCartesianCoords() {
        return cartesianCoords;
    }

    public double[] getEllipsoidalCoords() {
        return ellipsoidalCoords;
    }

    @Override
    public String toString(){
        String s = "";
        /*s += "satelliteTime = "+satelliteTime+"\n";
        s += "pseudorange = "+pseudorange+"\n";
        s += "Tk = "+Tk+"\n";
        s += "Ek = "+Ek+"\n";
        s += "Vk = "+Vk+"\n";
        s += "Uk = "+Uk+"\n";
        s += "Rk = "+Rk+"\n";
        s += "Ik = "+Ik+"\n";
        s += "lambdaK = "+lambdaK+"\n";*/
        s += "cartesianCoords = {"+cartesianCoords[0]+", "+cartesianCoords[1]+", "+cartesianCoords[2]+"}"+"\n";
        s += "ellipsoidalCoords = {"+ellipsoidalCoords[0]+", "+ellipsoidalCoords[1]+", "+ellipsoidalCoords[2]+"}"+"\n";

        return s;
    }
}
