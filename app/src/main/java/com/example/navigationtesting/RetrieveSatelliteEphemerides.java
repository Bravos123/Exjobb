package com.example.navigationtesting;

import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.net.ftp.FTPClient;


public class RetrieveSatelliteEphemerides {
    //Url templete of where the satellite ephemerides should be downloaded
    final String host = "igs.bkg.bund.de";//"ftp://igs.bkg.bund.de";
    static final String IGS_GALILEO_RINEX = "/IGS/BRDC/${yyyy}/${ddd}/BRDC00WRD_R_${yyyy}${ddd}0000_01D_EN.rnx.gz";


    public RetrieveSatelliteEphemerides(){
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
    //ftp://igs.bkg.bund.de/IGS/BRDC/2019/091/BRDC00WRD_R_20190910000_01D_EN.rnx.gz
    //ftp://igs.bkg.bund.de/IGS/BRDC/2019/091/BRDC00WRD_R_20190910000_01D_EN.rnx.gz


    class downloadHandler extends AsyncTask<Void, Void, Void> {
        String filePath;

        public downloadHandler(String url){
            filePath = url;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("Project", "doInBackground");

            try {

                FTPClient ftpClient = new FTPClient();
                ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
                ftpClient.connect(host, 21);
                int responseCode = ftpClient.getReplyCode();
                if(!FTPReply.isPositiveCompletion(responseCode)){
                    Log.i("Project", "Negative completion"+Integer.toString(responseCode));
                    ftpClient.disconnect();
                }
                ftpClient.login("Username", "");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                /*Log.i("Project", Integer.toString(ftpClient.getReplyCode()));


                boolean loginResult = ftpClient.login("", "");
                Log.i("Login result: ", Boolean.toString(loginResult));


                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ftpClient.retrieveFile(filePath, byteStream);

                for(byte b : byteStream.toByteArray()){
                    Log.i("Project", Byte.toString(b));
                }*/


                /*Log.i("Project", "Send request: "+requestUrl);
                URL url = new URL(requestUrl);
                URLConnection connection = url.openConnection();
                InputStream fileStream = connection.getInputStream();
                //Reader decoder = new InputStreamReader(gzipStream, "UTF-8");

                BufferedReader br = null;
                if(connection.getHeaderField("Content-Encoding") != null && connection.getHeaderField("Content-Encoding").equals("gzip")){
                    br = new BufferedReader(new InputStreamReader(new GZIPInputStream(fileStream)));
                }else{
                    br = new BufferedReader(new InputStreamReader(fileStream));
                }


                String response = "";
                String line;
                while((line = br.readLine()) != null){
                    response += line+"\n";
                }

                Log.i("Project", response);*/

                ftpClient.logout();
                ftpClient.disconnect();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }
}
