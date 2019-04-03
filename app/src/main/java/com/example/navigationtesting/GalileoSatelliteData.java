package com.example.navigationtesting;

import android.icu.math.BigDecimal;
import android.util.Log;

import java.util.ArrayList;

public class GalileoSatelliteData {
    /*Hjälp med att förstå paremetrarna här:
    * https://github.com/TheGalfins/GNSS_Compare/blob/master/GNSS_Compare/GoGpsExtracts/src/main/java/com/galfins/gogpsextracts/EphGalileo.java*/
    //EPHEMERIS PARAMETERS
    //SV / EPOCH / SV CLK
    String satelliteSystem;
    String svid;
    int epochTimeOfClockGALYear;
    int month;
    int day;
    int hour;
    int minute;
    int second;
    BigDecimal SVClockBiasInSeconds_af0;
    BigDecimal SVClockDriftSec_af1;
    BigDecimal SVClockDriftRateSec_af2;

    //BROADCAST ORBIT - 1
    BigDecimal IODnavIssueOfDataOfNavBatch;
    BigDecimal crs;
    BigDecimal deltaN;
    BigDecimal m0_radians;/*Mean anomaly at reference epoch*/

    //BROADCAST ORBIT - 2
    BigDecimal cus;
    BigDecimal e_Eccentricity;
    BigDecimal cuc;
    BigDecimal sqrt_a_sqrt_m;/* Square root of the semimajor axis (rootA)*/

    //BROADCAST ORBIT - 3
    BigDecimal toeTimeOfEphemeris_Sec_of_GAL_Week;
    BigDecimal cic;
    BigDecimal omega0_radians;/* Longitude of ascending node of orbit plane at beginning of week */
    BigDecimal cis;

    //BROADCAST ORBIT - 4
    BigDecimal i0_radians;
    BigDecimal crc;
    BigDecimal omega_radians;/* Argument of perigee */
    BigDecimal omegaDot;/* Rate of right ascension */

    //BROADCAST ORBIT - 5
    BigDecimal idot;
    BigDecimal dataSources;
    BigDecimal galWeekNumber;
    BigDecimal spare;

    //BROADCAST ORBIT - 6
    BigDecimal sisaSignalInSpaceAccuracy_meters;
    BigDecimal svHealth;
    BigDecimal bgd_E5a_E1_seconds;
    BigDecimal bgd_E5b_E1_seconds;

    //BROADCAST ORBIT - 7
    BigDecimal transmissionTimeOfMessage;
    BigDecimal BO7_spare1;//BO7 = BROADCAST ORBIT 7 DON'T THINK THESE ARE USED
    BigDecimal BO7_spare2;//BO7 = BROADCAST ORBIT 7 DON'T THINK THESE ARE USED
    BigDecimal BO7_spare3;//BO7 = BROADCAST ORBIT 7 DON'T THINK THESE ARE USED



    //Other GNSS DATA
    private double pseudorange;

    public GalileoSatelliteData(String data){//Data is single data entry from RINEX file.
        updateEmphemeridesParameters(data);
    }


    public void updateEmphemeridesParameters(String data){
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
        //Log.i("Project", "theOtherData: "+theOtherData);
        ArrayList<BigDecimal> dataParts= new ArrayList<>();



        while(theOtherData.length() > 4){
            int checkPos = 4;

            boolean skippedFirstPlusOrMinus = false;
            String checkChar = theOtherData.substring(checkPos, checkPos+1);
            while(true){
                if(checkChar.equals("-") ||
                        checkChar.equals("+") ||
                        checkChar.equals(" ") ||
                        skippedFirstPlusOrMinus){
                    checkPos+=3;
                    break;
                }else if(checkChar.equals("-") ||
                        checkChar.equals("+")){
                    skippedFirstPlusOrMinus = true;
                }
                checkPos++;
                if(theOtherData.length() > checkPos+1){
                    checkChar = theOtherData.substring(checkPos, checkPos+1);
                }else{
                    break;
                }

            }

            String doubleNumberCleanup = theOtherData.substring(0, checkPos).replaceAll(" ", "");
            doubleNumberCleanup = doubleNumberCleanup.replace("e", "E");
            dataParts.add(new BigDecimal(doubleNumberCleanup));
            theOtherData = theOtherData.substring(checkPos);
        }


        //Log.i("Project", "DATA PARTS--------------------------------------------------------------------");
        for(BigDecimal b : dataParts){
            //Log.i("Project", "PART: ["+b.toString()+"] \n ");
        }


        //Log.i("Project", "dataParts length: "+Integer.toString(dataParts.size()));
        //Log.i("Project", "svEpochSvClkPart:"+svEpochSvClkPart);
        //SV / EPOCH / SV CLK
        satelliteSystem = svEpochSvClkPart.substring(0, 3);
        svid = satelliteSystem.substring(1);
        epochTimeOfClockGALYear = Integer.parseInt(svEpochSvClkPart.substring(4, 8));
        month = Integer.parseInt(svEpochSvClkPart.substring(9, 11));
        day = Integer.parseInt(svEpochSvClkPart.substring(12, 14));
        hour = Integer.parseInt(svEpochSvClkPart.substring(15, 17));
        minute = Integer.parseInt(svEpochSvClkPart.substring(18, 20));
        second = Integer.parseInt(svEpochSvClkPart.substring(21, 23));
        SVClockBiasInSeconds_af0 = dataParts.get(0);
        SVClockDriftSec_af1 = dataParts.get(1);
        SVClockDriftRateSec_af2 = dataParts.get(2);

        //BROADCAST ORBIT - 1
        IODnavIssueOfDataOfNavBatch = dataParts.get(3);
        crs = dataParts.get(4);
        deltaN = dataParts.get(5);
        m0_radians = dataParts.get(6);

        //BROADCAST ORBIT - 2
        cuc = dataParts.get(7);
        e_Eccentricity = dataParts.get(8);
        cus = dataParts.get(9);
        sqrt_a_sqrt_m = dataParts.get(10);

        //BROADCAST ORBIT - 3
        toeTimeOfEphemeris_Sec_of_GAL_Week = dataParts.get(11);
        cic = dataParts.get(12);
        omega0_radians = dataParts.get(13);
        cis = dataParts.get(14);

        //BROADCAST ORBIT - 4
        i0_radians = dataParts.get(15);
        crc = dataParts.get(16);
        omega_radians = dataParts.get(17);
        omegaDot = dataParts.get(18);

        //BROADCAST ORBIT - 5
        idot = dataParts.get(19);
        dataSources = dataParts.get(20);
        galWeekNumber = dataParts.get(21);
        spare = dataParts.get(22);

        //BROADCAST ORBIT - 6
        sisaSignalInSpaceAccuracy_meters = dataParts.get(23);
        svHealth = dataParts.get(24);
        bgd_E5a_E1_seconds = dataParts.get(25);
        bgd_E5b_E1_seconds = dataParts.get(26);

        //BROADCAST ORBIT - 7
        transmissionTimeOfMessage = dataParts.get(27);


        calculatePosition();
    }


    public static ArrayList<Integer> getAllIndexMatches(String haystack, String needle){
        ArrayList<Integer> matches = new ArrayList<Integer>();
        int index = haystack.indexOf(needle);
        while (index >=0){
            matches.add(index);
            index = haystack.indexOf(needle, index+needle.length())   ;
        }
        return matches;
    }


    private void calculatePosition(){
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

        //Calculate T_k

        //BigDecimal t
        //BigDecimal T_k = new BigDecimal();

    }


    private BigDecimal getSatelliteClockCorrection(){
        //https://www.navcen.uscg.gov/pubs/gps/sigspec/gpssps1.pdf  -  2.5.5.2
        BigDecimal t = new BigDecimal("0.0");

        //double satelliteClockError =

        return t;
    }


    public String getSatelliteSystem(){
        return satelliteSystem;
    }

    public String getSvid(){
        return svid;
    }

    public double getPseudorange(){
        return pseudorange;
    }

    public void setPseudorange(double setPseudorange){
        this.pseudorange = setPseudorange;
    }

}
