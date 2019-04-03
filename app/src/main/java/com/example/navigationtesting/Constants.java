package com.example.navigationtesting;

import java.util.concurrent.TimeUnit;

public class Constants {
    public static final long LOCATION_RATE_NETWORK_MS = TimeUnit.SECONDS.toMillis(60L);

    public static final double NUMBER_NANO_SECONDS_100_MILLI = 100000000.0;
    public static final double NUMBER_NANO_SECONDS_DAY = 86400000000000.0;
    public static final double NUMBER_NANO_SECONDS_WEEK = 604800000000000.0;

    public static final double NUMBER_NANO_SECONDS_14 = 14000000000.0;
    public static final double NUMBER_NANO_SECONDS_THREE_HOURS = 10800000000000.0;

    public static final int MAXTOWUNCNS = 50;//Max TOW Uncertanity

    public static final double LIGHT_SPEED_VACUUM_METERS_PER_SECOND = 299792458.0;
}
