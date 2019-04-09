package com.example.navigationtesting;

import java.util.ArrayList;

public class GalileoEphemerides {

    private SatelliteTime time;
    private long toc;
    //EPHEMERIS PARAMETERS
    //SV / EPOCH / SV CLK
    private String satelliteSystem;
    private int svid;
    private int epochTimeOfClockGALYear;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int secondsInTheDay;
    private double af0;
    private double af1;
    private double af2;

    //BROADCAST ORBIT - 1
    private double IODnavIssueOfDataOfNavBatch;
    private double crs;
    private double deltaN;
    private double m0_radians;/*Mean anomaly at reference epoch*/

    //BROADCAST ORBIT - 2
    private double cus;
    private double e_Eccentricity;
    private double cuc;
    private double sqrtSemiMajorAxisA;/* Square root of the semimajor axis (rootA)*/

    //BROADCAST ORBIT - 3
    private double toe;
    private double cic;
    private double omega0_radians;/*Longitude of ascending node at the beginning of the week*/
    private double cis;

    //BROADCAST ORBIT - 4
    private double i0_radians; /*Inclination at reference epoch*/
    private double crc;
    private double omega_radians;/* Argument of perigee */
    private double omegaDot;/* Rate of right ascension */

    //BROADCAST ORBIT - 5
    private double idot;/* Rate of inclination angle */
    private double dataSources;
    private double galWeekNumber;
    private double spare;

    //BROADCAST ORBIT - 6
    private double sisaSignalInSpaceAccuracy_meters;
    private double svHealth;
    private double bgd_E5a_E1_seconds;//Is Tgd
    private double bgd_E5b_E1_seconds;

    //BROADCAST ORBIT - 7
    private double transmissionTimeOfMessage;
    //private double BO7_spare1;//BO7 = BROADCAST ORBIT 7 DON'T THINK THESE ARE USED
    //private double BO7_spare2;//BO7 = BROADCAST ORBIT 7 DON'T THINK THESE ARE USED
    //private double BO7_spare3;//BO7 = BROADCAST ORBIT 7 DON'T THINK THESE ARE USED



    public GalileoEphemerides(String data){
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
        ArrayList<Double> dataParts= new ArrayList<>();



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
            dataParts.add(Double.parseDouble(doubleNumberCleanup));
            theOtherData = theOtherData.substring(checkPos);
        }


        //Log.i("Project", "DATA PARTS--------------------------------------------------------------------");
        for(double b : dataParts){
            //Log.i("Project", "PART: ["+b.toString()+"] \n ");
        }


        //Log.i("Project", "dataParts length: "+Integer.toString(dataParts.size()));
        //Log.i("Project", "svEpochSvClkPart:"+svEpochSvClkPart);
        //SV / EPOCH / SV CLK
        satelliteSystem = svEpochSvClkPart.substring(0, 3);
        svid = Integer.parseInt(satelliteSystem.substring(1, 3));
        epochTimeOfClockGALYear = Integer.parseInt(svEpochSvClkPart.substring(4, 8));
        month = Integer.parseInt(svEpochSvClkPart.substring(9, 11));
        day = Integer.parseInt(svEpochSvClkPart.substring(12, 14));
        hour = Integer.parseInt(svEpochSvClkPart.substring(15, 17));
        minute = Integer.parseInt(svEpochSvClkPart.substring(18, 20));
        secondsInTheDay = Integer.parseInt(svEpochSvClkPart.substring(21, 23));
        af0 = dataParts.get(0);
        af1 = dataParts.get(1);
        af2 = dataParts.get(2);

        //BROADCAST ORBIT - 1
        IODnavIssueOfDataOfNavBatch = dataParts.get(3);// -> xIODE=a(11)
        crs = dataParts.get(4);
        deltaN = dataParts.get(5);/*Mean motion difference*/
        m0_radians = dataParts.get(6);

        //BROADCAST ORBIT - 2
        cuc = dataParts.get(7);
        e_Eccentricity = dataParts.get(8);
        cus = dataParts.get(9);
        sqrtSemiMajorAxisA = dataParts.get(10);

        //BROADCAST ORBIT - 3
        toe = dataParts.get(11);
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


        time = new SatelliteTime(svEpochSvClkPart.substring(4, 23));
        toc = time.getGpsWeekSec();
    }


    public SatelliteTime getTime() {
        return time;
    }

    public long getToc() {
        return toc;
    }

    public String getSatelliteSystem() {
        return satelliteSystem;
    }

    public int getSvid() {
        return svid;
    }

    public int getEpochTimeOfClockGALYear() {
        return epochTimeOfClockGALYear;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecondsInTheDay() {
        return secondsInTheDay;
    }

    public double getAf0() {
        return af0;
    }

    public double getAf1() {
        return af1;
    }

    public double getAf2() {
        return af2;
    }

    public double getIODnavIssueOfDataOfNavBatch() {
        return IODnavIssueOfDataOfNavBatch;
    }

    public double getCrs() {
        return crs;
    }

    public double getDeltaN() {
        return deltaN;
    }

    public double getM0_radians() {
        return m0_radians;
    }

    public double getCus() {
        return cus;
    }

    public double getE_Eccentricity() {
        return e_Eccentricity;
    }

    public double getCuc() {
        return cuc;
    }

    public double getSqrtSemiMajorAxisA() {
        return sqrtSemiMajorAxisA;
    }

    public double getToe() {
        return toe;
    }

    public double getCic() {
        return cic;
    }

    public double getOmega0_radians() {
        return omega0_radians;
    }

    public double getCis() {
        return cis;
    }

    public double getI0_radians() {
        return i0_radians;
    }

    public double getCrc() {
        return crc;
    }

    public double getOmega_radians() {
        return omega_radians;
    }

    public double getOmegaDot() {
        return omegaDot;
    }

    public double getIdot() {
        return idot;
    }

    public double getDataSources() {
        return dataSources;
    }

    public double getGalWeekNumber() {
        return galWeekNumber;
    }

    public double getSpare() {
        return spare;
    }

    public double getSisaSignalInSpaceAccuracy_meters() {
        return sisaSignalInSpaceAccuracy_meters;
    }

    public double getSvHealth() {
        return svHealth;
    }

    public double getBgd_E5a_E1_seconds() {
        return bgd_E5a_E1_seconds;
    }

    public double getBgd_E5b_E1_seconds() {
        return bgd_E5b_E1_seconds;
    }

    public double getTransmissionTimeOfMessage() {
        return transmissionTimeOfMessage;
    }
}
