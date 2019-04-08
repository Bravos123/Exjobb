package com.example.navigationtesting;

import android.renderscript.Float3;
import android.renderscript.Matrix3f;
import android.renderscript.Matrix4f;
import android.renderscript.*;
import android.util.Log;

import static com.example.navigationtesting.Constants.EARTH_GRAVITATIONAL_CONSTANT;

public class CalculateSatellitePosition {
    private static final double F_OF = -4.442807633e-10;//obliquity factor  I THINKG
    private static final double w_earthsAngularVelocity = 7292115.0e-11;

    public static final SatellitePositionData getGalileoSatellitePosition(double satelliteTime, double pseudorange, GalileoEphemerides eph){



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

        //True anomaly Vk
        double Vk = getTrueAnomaly(eph, Ek);
        //Log.i("Project", "true anomaly:"+Vk);

        /*Compute the argument of latitude uk from the argument of perigee
        ω, true anomaly vk and corrections cuc and cus:*/
        double Uk = eph.getOmega_radians() + Vk + eph.getCuc()*cos2(eph.getOmega_radians()+Vk) + eph.getCus()*sin2(eph.getOmega_radians()+Vk);

        /*Compute the radial distance rk, considering corrections crc and crs:*/
        double majorAxisA = eph.getSqrtSemiMajorAxisA() * eph.getSqrtSemiMajorAxisA();
        double Rk = majorAxisA*(1 - eph.getE_Eccentricity()*Math.cos(Ek)) + eph.getCrc()*cos2(eph.getOmega_radians() + Vk) + eph.getCrs()*sin2(eph.getOmega_radians() + Vk);

        /*Compute the inclination ik of the orbital plane from the inclination
        io at reference time toe, and corrections cic and cis:*/
        double Ik = eph.getI0_radians() + eph.getIdot()*Tk + eph.getCic()*cos2(eph.getOmega_radians() + Vk) + eph.getCis()*sin2(eph.getOmega_radians() + Vk);


        /*Compute the longitude of the ascending node λk (with respect to
        Greenwich). This calculation uses the right ascension at the beginning of the current week (Ωo), the correction from the apparent sidereal time variation in Greenwich between the beginning of the week
        and reference time tk = t − toe, and the change in longitude of the
        ascending node from the reference time toe:*/
        double lambdaK = eph.getOmega0_radians() + (eph.getOmegaDot() - w_earthsAngularVelocity) * Tk - w_earthsAngularVelocity*eph.getToe();


        /*Compute the coordinates in the TRS frame, applying three rotations
        (around uk, ik and λk):*/

        Float3 vecResult = new Float3();
        vecResult.x = (float)Rk;
        vecResult.y = 0.0f;
        vecResult.z = 0.0f;

        Matrix3f matrixResults = new Matrix3f();
        matrixResults.loadMultiply(createRotationMatrixR3(-lambdaK), createRotationMatrixR1(-Ik));
        matrixResults.loadMultiply(matrixResults, createRotationMatrixR3(-Uk));

        Float3 coordinates = multiplyMatrix3Vector3(matrixResults, vecResult);



        Float3 ellipsoidalCoords = convertCartesianToEllipsoidalCoordinates(coordinates);

        SatellitePositionData newPData = new SatellitePositionData(satelliteTime, pseudorange, Tk, Ek, Vk, Uk, Rk, Ik, lambdaK, coordinates, ellipsoidalCoords);

        return newPData;
    }


    private static final double getGPSSystemTimeSeconds(double satelliteTime, double pseudorange, GalileoEphemerides eph){
        double Tsv = satelliteTime - (pseudorange / Constants.SPEED_OF_LIGHT);

        //TODO: TEST IF THIS WORKS with return Tsv-deltaTsvL1;
        double Tr = F_OF * eph.getE_Eccentricity() * eph.getSqrtSemiMajorAxisA() * Math.sin(getEccentricAnomaly(satelliteTime, eph));
        double Tgd = eph.getBgd_E5a_E1_seconds();
        double deltaTsvL1 = eph.getAf0() + eph.getAf1()*(satelliteTime - eph.getToc()) + eph.getAf2()*Math.pow(satelliteTime - eph.getToc(), 2) + Tr - Tgd;



        return Tsv-deltaTsvL1;
        //return Tsv;
    }


    private static final double getEccentricAnomaly(double satelliteTime, GalileoEphemerides eph){
        /*
         * Got help from:
         * https://github.com/TheGalfins/GNSS_Compare/blob/master/GNSS_Compare/GoGpsExtracts/src/main/java/com/galfins/gogpsextracts/EphemerisSystemGps.java
         * */


        //Semi-major axis
        double A = eph.getSqrtSemiMajorAxisA() * eph.getSqrtSemiMajorAxisA();

        //Time from the ephemerides reference epoch
        double tk = checkGpsTime(satelliteTime - eph.getToe());

        //Computed mean motion [rad/sec]
        double n0 = Math.sqrt(Constants.EARTH_GRAVITATIONAL_CONSTANT / Math.pow(A, 3));

        //Corrected mean motion [rad/sec]
        double n = n0 + eph.getDeltaN();

        //Mean anomaly
        double Mk = eph.getM0_radians() + n * tk;

        //Eccentric anomaly starting value
        Mk = Math.IEEEremainder(Mk + 2 * Math.PI, 2 * Math.PI);
        double Ek = Mk;

        int i;
        double EkOld, dEk;

        // Eccentric anomaly iterative computation
        int maxNumIter = 14;
        for (i = 0; i < maxNumIter; i++) {
            EkOld = Ek;
            Ek = Mk + eph.getE_Eccentricity() * Math.sin(Ek);
            dEk = Math.IEEEremainder(Ek - EkOld, 2 * Math.PI);
            if (Math.abs(dEk) < 1e-12)
                break;
        }

        // TODO Display/log warning message
        if (i == maxNumIter)
            System.out.println("Warning: Eccentric anomaly does not converge.");


        /*Log.i("Project", "Eccentric anomaly results-------------------------------:");
        Log.i("Project", "eph.getE_Eccentricity():"+eph.getE_Eccentricity());
        Log.i("Project", "Mk:"+Mk);
        Log.i("Project", "E:"+Ek);*/

        return Ek;
    }


    public static final double getTrueAnomaly(GalileoEphemerides eph, double eccentricAnomaly){
        /*
         * Implemented from:
         * http://www.jgiesen.de/kepler/kepler.html
         * */
        double dp = 14;
        double K = Math.PI / 180;
        double S = Math.sin(eccentricAnomaly);
        double C = Math.cos(eccentricAnomaly);
        double fak = Math.sqrt(1.0-(eph.getE_Eccentricity()*eph.getE_Eccentricity()));
        double phi = Math.atan2(fak*S, C-eph.getE_Eccentricity())/K;

        return Math.round(phi*Math.pow(10, dp))/Math.pow(10, dp) * (Math.PI/180)/*Convert true anomaly from degrees to radians*/;
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

    private static final Matrix3f createRotationMatrixR3(double rot){
        Matrix3f R3_0 = new Matrix3f();
        R3_0.set(0, 0, (float)Math.cos(rot));
        R3_0.set(1, 0, (float)Math.sin(rot));
        R3_0.set(2, 0, 0);

        R3_0.set(0, 1, (float)-Math.sin(rot));
        R3_0.set(1, 1, (float)Math.cos(rot));
        R3_0.set(2, 1, 0);

        R3_0.set(0, 2, 0);
        R3_0.set(1, 2, 0);
        R3_0.set(2, 2, 1);

        return R3_0;
    }


    private static final Float3 multiplyMatrix3Vector3(Matrix3f mat3, Float3 vec3){
        Float3 newVec = new Float3();

        float x = 0;
        float y = 0;
        float z = 0;

        for(int i=0; i<3; i++){
            x += (mat3.get(0, 0) * vec3.x) + (mat3.get(1, 0) * vec3.y) + (mat3.get(2, 0) * vec3.z);
            y += (mat3.get(0, 1) * vec3.x) + (mat3.get(1, 1) * vec3.y) + (mat3.get(2, 1) * vec3.z);
            z += (mat3.get(0, 2) * vec3.x) + (mat3.get(1, 2) * vec3.y) + (mat3.get(2, 2) * vec3.z);
        }

        newVec.x = x;
        newVec.y = y;
        newVec.z = z;

        return newVec;
    }


    private static final Float3 convertCartesianToEllipsoidalCoordinates(Float3 cartesian){
        double latitude = Math.atan2(cartesian.z, Math.sqrt(Math.pow(cartesian.x, 2) + Math.pow(cartesian.y, 2)));
        double longitude = Math.atan2(cartesian.y, cartesian.x);

        Float3 ellipsoidalCoords = new Float3();
        ellipsoidalCoords.x = (float)(latitude*(180/Math.PI));
        ellipsoidalCoords.y = (float)(longitude*(180/Math.PI));

        return ellipsoidalCoords;

        /*double X = cartesian.x;
        double Y = cartesian.y;
        double Z = cartesian.z;

        //this.geod = new SimpleMatrix(3, 1);

        double a = Constants.WGS84_SEMI_MAJOR_AXIS;
        double e = Constants.WGS84_ECCENTRICITY;

        // Radius computation
        double r = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2) + Math.pow(Z, 2));

        // Geocentric longitude
        double lamGeoc = Math.atan2(Y, X);

        // Geocentric latitude
        double phiGeoc = Math.atan(Z / Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2)));

        // Computation of geodetic coordinates
        double psi = Math.atan(Math.tan(phiGeoc) / Math.sqrt(1 - Math.pow(e, 2)));
        double phiGeod = Math.atan((r * Math.sin(phiGeoc) + Math.pow(e, 2) * a
                / Math.sqrt(1 - Math.pow(e, 2)) * Math.pow(Math.sin(psi), 3))
                / (r * Math.cos(phiGeoc) - Math.pow(e, 2) * a * Math.pow(Math.cos(psi), 3)));
        double lamGeod = lamGeoc;
        double N = a / Math.sqrt(1 - Math.pow(e, 2) * Math.pow(Math.sin(phiGeod), 2));
        double h = r * Math.cos(phiGeoc) / Math.cos(phiGeod) - N;


        Float3 ellipsoidalCoords = new Float3();
        ellipsoidalCoords.x = (float)Math.toDegrees(lamGeod);
        ellipsoidalCoords.y = (float)Math.toDegrees(phiGeod);
        ellipsoidalCoords.x = (float)h;

        return ellipsoidalCoords;*/

        /*double longitude = Math.atan(cartesian.y/cartesian.x);
        double p = Math.sqrt(Math.pow(cartesian.x, 2) + Math.pow(cartesian.y, 2));

        //TODO: Implement improved latitude by iterating some other equation:  https://gssc.esa.int/navipedia/index.php/Ellipsoidal_and_Cartesian_Coordinates_Conversion (From Cartesian to Ellipsoidal coordinates)


        double a = 6378137;
        double f = 1/298.257;
        double e = Math.sqrt(2*f-Math.pow(f,2));

        double h = 0;
        double latitude = Math.atan(cartesian.z/((1-Math.exp(2))*p));
        double N = a/Math.pow((1-Math.pow((e*Math.sin(latitude)),2)),0.5);
        double delta_h = 1000000;
        while (delta_h > 0.01){
            double prev_h = h;
            latitude = Math.atan(cartesian.z/p*(1-Math.pow(e,2)*(N/(N+h))));
            N = a/Math.pow(1-Math.pow(e*Math.sin(latitude), 2), 0.5);
            h = p/Math.cos(latitude)-N;
            delta_h = Math.abs(h-prev_h);
        }



        Float3 ellipsoidalCoords = new Float3();
        ellipsoidalCoords.x = (float)longitude;
        ellipsoidalCoords.y = (float)latitude;
        return ellipsoidalCoords;*/
    }
}
