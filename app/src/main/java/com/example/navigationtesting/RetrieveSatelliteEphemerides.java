package com.example.navigationtesting;

import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.util.Log;

import com.example.navigationtesting.SatelliteMVC.Satellite;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.net.ftp.FTPClient;


public class RetrieveSatelliteEphemerides {



    private boolean loading = false;
    /*
    * Retrieves the RINEX Data of satellites
    * */
    //Url templete of where the satellite ephemerides should be downloaded
    final String host = "igs.bkg.bund.de";//"ftp://igs.bkg.bund.de";
    /*
    * RINEX File name description:
    * file:///home/elias/H%C3%A4mtningar/rinex303%20(6).pdf
    * A 1 RINEX File name description
    * */
    static final String IGS_GALILEO_RINEX = "/IGS/BRDC/${yyyy}/${ddd}/BRDC00WRD_R_${yyyy}${ddd}0000_01D_EN.rnx.gz";

    private Callback callback;

    public RetrieveSatelliteEphemerides(Callback callback){
        this.callback = (Callback) callback;
    }

    public void retrieveEmepherides(){
        if(!loading){
            loading = true;
            String substitutedStirng = IGS_GALILEO_RINEX;

            int yearNumber = Calendar.getInstance().get(Calendar.YEAR);
            int dayNumber = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            String dayNumberString = Integer.toString(dayNumber);
            if(dayNumber < 100){
                dayNumberString = "0"+dayNumberString;
            }

            substitutedStirng = substitutedStirng.replaceAll("\\$\\{yyyy\\}", Integer.toString(yearNumber));
            substitutedStirng = substitutedStirng.replaceAll("\\$\\{ddd\\}", dayNumberString);

            Log.i("Project", "substitutedStirng: "+substitutedStirng);
            new downloadHandler(substitutedStirng).execute();
        }
    }


    class downloadHandler extends AsyncTask<Void, Void, Void> {
        String filePath;

        public downloadHandler(String url){
            filePath = url;
        }


        private void parseData(String data, String dateLastModified){
            ArrayList<Satellite> satellites = RinexReader.parse(data);
            if(satellites == null){
                Log.i("Project", "satellites are NLL");
            }else{
                callback.callBack(dateLastModified, satellites);
            }

            loading = false;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("Project", "filePath: "+host+filePath);

            try {

                FTPClient ftpClient = new FTPClient();
                ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
                ftpClient.connect(host, 21);
                int responseCode = ftpClient.getReplyCode();
                if(!FTPReply.isPositiveCompletion(responseCode)){
                    Log.i("Project", "Negative completion"+Integer.toString(responseCode));
                    ftpClient.disconnect();
                }
                ftpClient.login("Anonymous", "");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                String lastModifiedTime = ftpClient.getModificationTime(filePath);


                boolean loginResult = ftpClient.login("", "");
                Log.i("Login result: ", Boolean.toString(loginResult));


                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ftpClient.retrieveFile(filePath, byteStream);


                BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(byteStream.toByteArray()))));
                String response = "";
                String line;

                while((line = br.readLine()) != null){
                    response += line+"\n";
                }

                ftpClient.logout();
                ftpClient.disconnect();



                parseData(response, lastModifiedTime);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }
}
