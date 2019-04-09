package com.example.navigationtesting;

import android.location.GnssClock;
import android.renderscript.Float3;
import android.renderscript.Matrix3f;
import android.renderscript.Matrix4f;
import android.renderscript.*;
import android.util.Log;

import static com.example.navigationtesting.Constants.EARTH_GRAVITATIONAL_CONSTANT;

public class CalculateSatellitePosition {
    private static final double F_OF = -4.442807633e-10;//obliquity factor  I THINKG
    private static final double w_earthsAngularVelocity = 7292115.0e-11;
    //WGS-84 Earth Univ. Grav. parameter (m3/s2)
    private static final double xmu = 3.986005e+14;

    //WGS-84 Earth rotation rate (rad/s)
    private static final double om_e = 7.2921151467e-5;

    public static final SatellitePositionData getGalileoSatellitePosition(GalileoEphemerides eph){



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

        //##########################################################################################
        /*Algorithm from https://gssc.esa.int/navipedia/GNSS_Book/ESA_GNSS-Book_TM-23_Vol_I.pdf
         * at 3.3.1*/

        /*Compute the time tk from the ephemerides reference epoch toe (t and
            toe are expressed in seconds in the GPS week):*/
        /*double t = satelliteTime;//getGPSSystemTimeSeconds(satelliteTime, pseudorange, eph);
        double Tk = t - eph.getToe();*/
        int idoy = eph.getEpochTimeOfClockGALYear();
        double xy = (double)idoy;

        //GPS day: (1980jan6.0 => JD=2444244.5 => id_GPS=1.0)
        int id_GPS = (int)(365.25*(xy-1.0))+idoy-722835;

        //Day of week:
        int idw = id_GPS % 7;
        //Number of GPS week:
        int nw = (id_GPS-idw)/7;
        //seconds in the week:
        double sw = (double)(idw)*86400.0+eph.getSecondsInTheDay();

        double tk = sw - eph.getToe();
        tk = checkGpsTime(tk);



        /*True Anomaly fk*/
        double xMo = eph.getM0_radians();
        double a12 = eph.getSqrtSemiMajorAxisA();
        double dn = eph.getDeltaN();
        double e = eph.getE_Eccentricity();

        double xMk = xMo + (Math.sqrt(xmu)/(Math.pow(a12, 3))+dn)*tk;
        double Ek = sub_nSteffensen(xMk, e); //eccentric anomaly (rad)
        double fk=Math.atan2(Math.sqrt(1.0-Math.pow(e,2))*Math.sin(Ek), Math.cos(Ek)-e);

        /*Arg. of Latitude uk,radius rk, inclination ik:*/
        double omgp = eph.getOmega_radians();
        double uk = omgp + fk + eph.getCuc() * Math.cos((omgp + fk) * 2.0) + eph.getCus() * Math.sin((omgp + fk) * 2.0);
        double rk=(Math.pow(a12, 2))*(1.0-e*Math.cos(Ek))+eph.getCrc()*Math.cos(2.0*(omgp+fk)) +  eph.getCrs()*Math.sin(2.0*(omgp+fk));
        double xIk=eph.getI0_radians()+eph.getIdot()*tk+eph.getCic()*Math.cos(2.0*(omgp+fk)) + eph.getCis()*Math.sin(2.0*(omgp+fk));


        /*positions in orbital plane*/
        double xp = rk * Math.cos(uk);
        double yp = rk * Math.sin(uk);

        /*Longitude of ascending node xlmk:*/
        double xlmk = eph.getOmega0_radians()+(eph.getOmegaDot()-om_e)*tk-om_e*eph.getToe();

        /*CT-System coordinates*/
        double x=xp*Math.cos(xlmk)-yp*Math.cos(xIk)*Math.sin(xlmk);
        double y=xp*Math.sin(xlmk)+yp*Math.cos(xIk)*Math.cos(xlmk);
        double z=yp*Math.sin(xIk);




        double[] coordsCartesian = new double[]{x, y, z};
        double[] coordsEllipsoidal = convertCartesianToEllipsoidalCoordinates(coordsCartesian);


        SatellitePositionData newPData = new SatellitePositionData(coordsCartesian, coordsEllipsoidal);
        return newPData;
    }



    private static final double sub_nSteffensen(double xm, double e){
        /*Method for accelerating the convergence of the Method
        of Newton-Rapson.
        Equations of this kind p=g(p)   (==> E=M+e*sin(E))
        The method requires that g'(p)<>1  (==> p single root)*/

        double tol = 1.0e-15;
        xm = Math.atan2(Math.sin(xm), Math.cos(xm));
        double p = xm;

        while(true){
            double p0 = p;
            double p1 = xm + e * Math.sin(p0);
            double p2 = xm + e * Math.sin(p1);
            double dd = Math.abs(p2-2.0*p1+p0);
            if(dd < tol){
                break;
            }
            p = p0-Math.pow(p1-p0, 2)/(p2-2.0*p1+p0);
            if(Math.abs(p-p0) < tol){
                break;
            }
        }

        return p;

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

    private static final double cos2(double input){
        return Math.pow(Math.cos(input), 2);
    }

    private static final double sin2(double input){
        return Math.pow(Math.sin(input), 2);
    }


    private static final Matrix3f createRotationMatrixR1(double rot){
        Matrix3f R1_0 = new Matrix3f();
        R1_0.set(0, 0, 1);
        R1_0.set(1, 0, 0);
        R1_0.set(2, 0, 0);

        R1_0.set(0, 1, 0);
        R1_0.set(1, 1, (float) Math.cos(rot));
        R1_0.set(2, 1, (float) Math.sin(rot));

        R1_0.set(0, 2, 0);
        R1_0.set(1, 2, (float) -Math.sin(rot));
        R1_0.set(2, 2, (float) Math.cos(rot));

        return R1_0;
    }



    private static final double[] convertCartesianToEllipsoidalCoordinates(double[] coordsCartesian){
        /*implemented from car2geo.f in PROG/src/F77_src from gLab CD:
        * https://gssc.esa.int/navipedia/index.php/GNSS:Tools*/

        /*Value declaration*/
        double tol = 1.0e-11;
        //WGS84 parameters (in meters): https://gssc.esa.int/navipedia/index.php/Reference_Frames_in_GNSS
        double a = 6378137.0;
        double f = 1.0/298.257223563;
        double b = a*(1.0-f);
        double e2=(Math.pow(a, 2)-Math.pow(b, 2))/Math.pow(a, 2);

        int iunits = 0;//Input is in meters

        double x = coordsCartesian[0];
        double y = coordsCartesian[1];
        double z = coordsCartesian[2];


        //Output
        double xlon = 0;
        double xlat = 0;
        double h = 0;


        double xl = Math.atan2(y, x);
        double p = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double fi = Math.atan(z/p/(1.0-e2));
        double fia = fi;

        while(true){
            double xn = Math.pow(a, 2)/(Math.sqrt(Math.pow(a*Math.cos(fi), 2)+Math.pow(b*Math.sin(fi), 2)));

            h = p/Math.cos(fi) - xn;
            fi = Math.atan(z/p/(1.0-e2*xn/(xn+h)));
            if(Math.abs(fi-fia) > tol){
                fia = fi;
            }else{
                break;
            }
        }

        xlon = xl;
        xlat = fi;


        double[] ellipsoid = new double[]{xlat, xlon, h};

        return ellipsoid;


        /*double latitude = Math.atan2(coordsCartesian[2], Math.sqrt(Math.pow(coordsCartesian[0], 2) + Math.pow(coordsCartesian[1], 2)));
        double longitude = Math.atan2(coordsCartesian[1], coordsCartesian[0]);

        return new double[]{(latitude*(180/Math.PI)), (longitude*(180/Math.PI))};*/





    }
}
