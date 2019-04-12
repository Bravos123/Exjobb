package com.example.navigationtesting.SatelliteMVC;

import com.example.navigationtesting.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SatelliteTime {
    //Adapted from https://github.com/TheGalfins/GNSS_Compare/blob/master/GNSS_Compare/GoGpsExtracts/src/main/java/com/galfins/gogpsextracts/Time.java

    DateFormat df = new SimpleDateFormat("yyyy MM dd HH mm ss");//"yyyy MM dd HH mm ss.SSS"
    private long msec; //Time in milliseconds since January 1, 1970 (UNIX standard)



    public SatelliteTime(String dateStr){
        msec = dateStringToTime(dateStr);
    }

    public long getUnixTimeSecs(){
        return msec / 1000L;
    }

    private long dateStringToTime(String dateStr) {

        long dateTime = 0;

        Date dateObj = null;
        try {
            dateObj = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateTime = dateObj.getTime();

        return dateTime;
    }


    public int getGpsWeekSec(){
        // Shift from UNIX time (January 1, 1970 - msec)
        // to GPS time (January 6, 1980 - sec)
        long time = msec / Constants.MILLISEC_IN_SEC - Constants.UNIX_GPS_DAYS_DIFF * Constants.SEC_IN_DAY;
        return (int)(time%(Constants.DAYS_IN_WEEK * Constants.SEC_IN_DAY));
    }

}
