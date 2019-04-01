package com.example.navigationtesting;

public class SatelliteCoordinates {
    /*
    * https://gssc.esa.int/navipedia/GNSS_Book/ESA_GNSS-Book_TM-23_Vol_I.pdf
    * part 3.3
    * */
    //Ephimeris parameters
    double a = 604800;//Seconds in a week?: Ephemerides reference epoch in seconds within the week
    double b;//Square root of semi-major axis
    double c;//Eccentricity
    double d;//Mean anomaly at reference epoch
    double e;//Argument of perigee
    double f;//Inclination at reference epoch
    double g;//Longitude of ascending node at the beginning of the week
    double h;//Mean motion difference
    double i;//Rate of inclination angle
    double j;//Rate of node’s right ascension
    double k;//Latitude argument correction
    double l;//Orbital radius correction
    double m;//Inclination correction
    double n;//Satellite clock offset
    double o;//Satellite clock drift
    double p;//Satellite clock drift rate

    //Constants
    static double SPEED_OF_LIGHT_C = 2.99792458 * 1e8;// m/s
    static double EARTHS_GRAVITATION = 3.986005 * 1e14;// m^3/s^2
    static double EARTHS_ROTATION_RATE = 7.2921151467 * 1e-5;// Rad/s
    static double PI = 3.1415926535898;



    public void calculateCoordinates(){
        double Tk;
    }


}
