package com.example.navigationtesting;

import android.util.Log;

import com.example.navigationtesting.SatelliteMVC.Satellite;

import java.util.ArrayList;

public class RinexReader {


    public static final ArrayList<Satellite> parse(String rinexData){
        ArrayList<Satellite> satellites = new ArrayList<>();
        String[] lines = rinexData.split("\\n");


        /*Example data from Galileo:
        *
        * E05 2019 04 02 07 40 00 8.120288839560e-05-6.579625733140e-12 0.000000000000e+00
            7.800000000000e+01-9.843750000000e+00 3.053698627380e-09-5.395120863750e-01
            -3.948807716370e-07 1.295380061490e-04 1.359544694420e-05 5.440626609800e+03
            2.004000000000e+05-3.725290298460e-09-2.421556169020e-01-1.117587089540e-08
            9.534028560040e-01 4.209375000000e+01-1.653159749880e+00-5.468799226010e-09
            8.893227581480e-11 2.580000000000e+02 2.047000000000e+03 0.000000000000e+00
            3.120000000000e+00 0.000000000000e+00-1.396983861920e-09 0.000000000000e+00
            2.011300000000e+05

           the formats parts is explained here: ftp://igs.org/pub/data/format/rinex303.pdf
           in Appendix 25
        * */
        String rinexDataBuffer = "";
        boolean endOfHeader = false;
        int lineNumber = 0;
        for(String l : lines){
            if(l.indexOf("END OF HEADER") != -1){
                endOfHeader = true;
            }else if(endOfHeader){
                if(l.substring(0, 1).equals("E")){//Finish already started buffer and start a new one
                    if(rinexDataBuffer != ""){
                        insertSatelliteData(satellites, createnewSatelliteData(rinexDataBuffer));
                        //Log.i("Project", "rinexDataBuffer: "+rinexDataBuffer);
                    }
                    rinexDataBuffer = l+"\n";
                }else{
                    rinexDataBuffer += l+"\n";
                }
            }else if(!endOfHeader){
                //Versioncheck
                if(lineNumber == 0){
                    if(l.indexOf("3.03") == -1){
                        Log.i("Project", "Rinex verion is wrong, must be 3.03");
                        return null;
                    }
                }
            }
            lineNumber++;
            //Log.i("Project", "lineNumner: "+lineNumber);
        }

        //use last used buffer
        insertSatelliteData(satellites, createnewSatelliteData(rinexDataBuffer));



        return satellites;
    }


    public static void testFunc(ArrayList<Satellite> galileoSatellites, Satellite newSatelliteData){
        Log.i("Project", "HEJ!");
    }


    public static void insertSatelliteData(ArrayList<Satellite> galileoSatellites, Satellite newSatelliteData){
        if(newSatelliteData.getNoradId() == -1){
            /*If norad id is -1 then no associated norad was found ffor the svid which makes this
            * Satellite useless as the api that handles coordinate prediction needs a norad id.*/
            return;//do not insert satellite
        }
        boolean satelliteWasUpdated = false;
        for(int i=0; i<galileoSatellites.size(); i++){
            if(galileoSatellites.get(i).getSvid() == newSatelliteData.getSvid()){
                galileoSatellites.set(i, newSatelliteData);
                satelliteWasUpdated = true;
                break;
            }
        }

        if(!satelliteWasUpdated){
            //satellite system has not been added, add it
            galileoSatellites.add(newSatelliteData);
        }


    }

    private static final Satellite createnewSatelliteData(String rinexData){
        Satellite sat = new Satellite(rinexData);
        //Log.i("Project", "Created new satellite from:" + rinexData);
        return sat;
    }



}
