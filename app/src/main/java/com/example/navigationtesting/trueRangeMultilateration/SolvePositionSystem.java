package com.example.navigationtesting.trueRangeMultilateration;

import android.util.Log;

import com.example.navigationtesting.Satellite.LatLngAlt;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;

public class SolvePositionSystem {



    public static LatLngAlt calculateSpacePoint(ArrayList<SpacePoint> spacePointsList){
        //Log.i("Project", "calculateSpacePoint: "+spacePointsList.size());
        double x = 0;
        double y = 0;
        double z = 0;

        /*Log.i("Project", "\n\n\n\n######################################################");
        for(SpacePoint sp : spacePointsList){
            Log.i("Project", "x: "+sp.getCartesianX()+"\ny: "+sp.getCartesianY()+"\nz: "+sp.getCartesianZ()+"\nDistance: "+sp.getPseudorangeToTarget()+"\n\n");
        }*/


        double[][] coefficientsArray = new double[spacePointsList.size()][3];
        for(int i=0; i<spacePointsList.size(); i++){
            SpacePoint sp = spacePointsList.get(i);
            coefficientsArray[i] = new double[]{sp.getCartesianX(), sp.getCartesianY(), sp.getCartesianZ()};
        }

        RealMatrix coefficients = new Array2DRowRealMatrix(coefficientsArray, false);

        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();

        double[] constantsArray = new double[spacePointsList.size()];
        for(int i=0; i<spacePointsList.size(); i++){
            constantsArray[i] = spacePointsList.get(i).getPseudorangeToTarget();
        }

        RealVector constants = new ArrayRealVector(constantsArray, false);
        RealVector solution = solver.solve(constants);

        x = solution.getEntry(0);
        y = solution.getEntry(1);
        z = solution.getEntry(2);

        //Log.i("Project", "Positions: ("+x+", "+y+", "+z+")");

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
        double xlon;
        double xlat;
        double h;


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
