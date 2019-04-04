package com.example.navigationtesting;

import static com.example.navigationtesting.Constants.EARTH_GRAVITATIONAL_CONSTANT;

public class CalculateSatellitePosition {
    private static final double F_OF = -4.442807633e-10;//obliquity factor  I THINKG

    public static final String getGalileoSatellitePosition(double satelliteTime, double pseudorange, GalileoEphemerides eph){
        /*
         * Reference: https://gssc.esa.int/navipedia/GNSS_Book/ESA_GNSS-Book_TM-23_Vol_I.pdf at 3.3.1
         * The ephemerides parameters needed to calculate the satellite position:
         *
         * toeTimeOfEphemeris_Sec_of_GAL_Week
         * sqrt_a_sqrt_m
         * e_Eccentricity
         * m0_radians;
         * omega_radians
         * i0_radians
         * omega0_radians
         * deltaN
         * idot
         * omegaDot
         *
         * cuc, cus
         * crc, crs
         * cic, cis
         *
         * SVClockBiasInSeconds_af0
         * SVClockDriftSec_af1
         * SVClockDriftRateSec_af2
         *
         * */

        /*Algorithm from https://gssc.esa.int/navipedia/GNSS_Book/ESA_GNSS-Book_TM-23_Vol_I.pdf
        * at 3.3.1*/

        /*Compute the time tk from the ephemerides reference epoch toe (t and
            toe are expressed in seconds in the GPS week):*/
        double t = getGPSSystemTimeSeconds(satelliteTime, pseudorange, eph);
        double Tk = t - eph.getToe();
        Tk = checkGpsTime(Tk);



        //eccentric anomaly Ek:
        double Ek = getEccentricAnomaly(satelliteTime, eph);


        return "HERE ARE THE COORDINATES FOR THE SATELLITE";
    }


    private static final double getGPSSystemTimeSeconds(double satelliteTime, double pseudorange, GalileoEphemerides eph){
        double Tsv = satelliteTime - (pseudorange / Constants.SPEED_OF_LIGHT);

        //TODO: TEST IF THIS WORKS with return Tsv-deltaTsvL1;
        double Tr = F_OF * eph.getE_Eccentricity() * eph.getSqrtSemiMajorAxisA() * Math.sin(getEccentricAnomaly(satelliteTime, eph));
        double Tgd = eph.getBgd_E5a_E1_seconds();
        double deltaTsvL1 = eph.getAf0() + eph.getAf1()*(satelliteTime - eph.getToc()) + eph.getAf2()*Math.pow(satelliteTime - eph.getToc(), 2) + Tr - Tgd;



        //return Tsv-deltaTsvL1;
        return Tsv;
    }


    private static final double getEccentricAnomaly(double satelliteTime, GalileoEphemerides eph){
        /*
        * This calculates the eccentric anomaly from iterating the kepler equation
        * */
        //Implemented from: http://www.jgiesen.de/kepler/kepler.html
        double E_anomaly = 0.0;
        satelliteTime = checkGpsTime(satelliteTime);
        //Mean anomaly
        double Mk = eph.getM0_radians() + ((Math.sqrt(EARTH_GRAVITATIONAL_CONSTANT)/eph.getSqrtSemiMajorAxisA()) + eph.getDeltaN() ) * satelliteTime;

        int dp = 200;//Number of decimal places
        double delta = Math.pow(10, -dp);

        double K = Math.PI/180;

        double E, F;

        Mk = Mk/360.0;
        Mk = 2.0*Math.PI*(Mk - Math.floor(Mk));

        /*For small eccentricities the mean anomaly M can be used as an initial value E0 for the iteration. In case of e>0.8 the initial value E0=π is taken.*/
        if(eph.getE_Eccentricity() < 0.8){
            E = Mk;
        }else{
            E = Math.PI;
        }

        int maxIter = 30;
        int i=0;

        F = E = - eph.getE_Eccentricity()*Math.sin(Mk) - Mk;

        while((Math.abs(F) > delta) && (i<maxIter)){
            E = E - F/(1.0-eph.getE_Eccentricity()*Math.cos(E));
            F = E - eph.getE_Eccentricity()*Math.sin(E) - Mk;
            i++;
        }

        E = E/K;

        return Math.round(E * (E*Math.pow(10, dp)) / Math.pow(10, dp));
    }


    private static final double checkGpsTime(double time) {

        // Account for beginning or end of week crossover:
        //https://gssc.esa.int/navipedia/GNSS_Book/ESA_GNSS-Book_TM-23_Vol_I.pdf 3.3.1
        if (time > Constants.SEC_IN_HALF_WEEK) {
            time = time - 2 * Constants.SEC_IN_HALF_WEEK;
        } else if (time < -Constants.SEC_IN_HALF_WEEK) {
            time = time + 2 * Constants.SEC_IN_HALF_WEEK;
        }
        return time;
    }

}
