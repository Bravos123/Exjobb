package com.example.navigationtesting;

import android.util.Log;

import java.util.ArrayList;

public class GalileoSatelliteData {
    //SV / EPOCH / SV CLK
    double satelliteSystem;
    double epochTimeOfClockGALYear;
    double month;
    double day;
    double hour;
    double minute;
    double second;
    double SVClockBiasInSeconds_af;
    double SVClockDriftSec_af1;
    double SVClockDriftRateSec_af2;

    //BROADCAST ORBIT - 1
    double IODnavIssueOfDataOfNavBatch;
    double crs_meters;
    double deltaN_radiansSec;
    double m0_radians;

    //BROADCAST ORBIT - 2
    double cus_radians;
    double e_Eccentricity;
    double cus_radians_2;
    double sqrt_a_sqrt_m;

    //BROADCAST ORBIT - 3
    double toeTimeOfEphemeris_Sec_of_GAL_Week;
    double cic_radians;
    double omega0_radians;
    double cis_radians_2;

    //BROADCAST ORBIT - 4
    double i0_radians;
    double crc_meters;
    double omega_radians;
    double omegaDot_radiansSec;

    //BROADCAST ORBIT - 5
    double idot_radiansSec;
    double dataSources;
    double galWeekNumber;
    double spare;

    //BROADCAST ORBIT - 6
    double sisaSignalInSpaceAccuracy_meters;
    double svHealth;
    double bgd_E5a_E1_seconds;
    double bgd_E5b_E1_seconds;

    //BROADCAST ORBIT - 7
    double transmissionTimeOfMessage;
    double BO7_spare1;//BO7 = BROADCAST ORBIT 7
    double BO7_spare2;
    double BO7_spare3;



    public GalileoSatelliteData(String data){//Data is single data entry from RINEX file.
        //Replace new lines with spaces in order to split it easier
        //REgex matches new line with spaces
        data = data.replaceAll("\\n", " ");
        //Regex replaces an arbitrary number of repeating spaces with one space
        data = data.replaceAll("[ \\t]+", " ");


        /*Example rinex data:
        *E05 2019 04 02 07 40 00 8.120288839560e-05-6.579625733140e-12 0.000000000000e+00
         7.800000000000e+01-9.843750000000e+00 3.053698627380e-09-5.395120863750e-01
        -3.948807716370e-07 1.295380061490e-04 1.359544694420e-05 5.440626609800e+03
         2.004000000000e+05-3.725290298460e-09-2.421556169020e-01-1.117587089540e-08
         9.534028560040e-01 4.209375000000e+01-1.653159749880e+00-5.468799226010e-09
         8.893227581480e-11 2.580000000000e+02 2.047000000000e+03 0.000000000000e+00
         3.120000000000e+00 0.000000000000e+00-1.396983861920e-09 0.000000000000e+00
         2.011300000000e+05
         */

        String svEpochSvClkPart = data.substring(0, 23);
        String theOtherData = data.substring(23);
        ArrayList<String> dataParts= new ArrayList<>();

        while(theOtherData.length() > 19){
            String subDataPart = theOtherData.substring(0, 19);
            theOtherData = theOtherData.substring(19);
            dataParts.add(subDataPart);
        }
        dataParts.add(theOtherData);

        Log.i("Project", "svEpochSvClkPart:"+svEpochSvClkPart+"\n");
        for(String s : dataParts){
            Log.i("Project", "PART:"+s+" \n ");
        }

        //SV / EPOCH / SV CLK


        //BROADCAST ORBIT - 1


        //BROADCAST ORBIT - 2


        //BROADCAST ORBIT - 3


        //BROADCAST ORBIT - 4


        //BROADCAST ORBIT - 5


        //BROADCAST ORBIT - 6


        //BROADCAST ORBIT - 7



        Log.i("Project", "Sattelite individual data: \n\n"+data+"\n\n\n");
    }

}
