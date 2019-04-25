package com.example.navigationtesting.trueRangeMultilateration;

import com.example.navigationtesting.Satellite.LatLngAlt;

import java.util.ArrayList;

public class TrueRangeMultilateration {



    public static LatLngAlt calculateSpacePoint(ArrayList<SpacePoint> spacePointsList){
        /*Implementerat från:
        * https://en.wikipedia.org/wiki/True_range_multilateration
        * at part: "Three Cartesian dimensions, three measured slant ranges"*/
        double x = 0;
        double y = 0;
        double z = 0;






        double[] ellipsoidalCoords = convertCartesianToEllipsoidalCoordinates(new double[]{x, y, z});
        return new LatLngAlt(ellipsoidalCoords[0], ellipsoidalCoords[1], ellipsoidalCoords[2]);
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

        //int iunits = 0;//Input is in meters

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

    }

}
