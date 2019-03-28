package com.example.navigationtesting;

import android.location.OnNmeaMessageListener;
import android.util.Log;

public class NmeaMessagesListener implements OnNmeaMessageListener {

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        Log.d("Project", "Nmea message: "+message);
    }


}
