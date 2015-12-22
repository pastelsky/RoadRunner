package com.example.shubhamkanodia.roadrunner.Helpers;

/**
 * Created by shubhamkanodia on 20/12/15.
 */
public class Constants {

    public static final String PACKAGE_NAME = "com.example.shubhamkanodia.roadrunner"; // Change as appropriate
    public static final String STRING_ACTION = PACKAGE_NAME + ".STRING_ACTION";
    public static final String STRING_EXTRA = PACKAGE_NAME + ".STRING_EXTRA";

    //All time constants are in seconds. All distance constants in meters.

    //Recording constants

    public static final int SLIDING_WINDOW_CAPACITY = 5;
    public static final int RECORD_NOTIF_ID = 1;
    public static final int MINIMUM_RECORD_TIME = 5;
    public static final boolean REQUIRE_MOVEMENT = true;


    //Frequency - in ms
    public static final int LOCATION_FETCH_INTERVAL = 500;
    public static final int LOCATION_FETCH_INTERVAL_FASTEST = 500;
    public static final int SENSOR_UPDATE_INTERVAL = 80;



    //Sleep timers
    public static final int MOVEMENT_CHECK_INTERVAL = 10;
    public static final int MOVEMENT_CHECK_INITIAL_DELAY = 20;

    public static final int MINIMUM_REQD_DISPLACEMENT = 2;
    public static final int MINIMUM_RETRY_DISPLACEMENT = 1;


    public static final int RETRY_MULTIPLIER = 100;
    public static final int MAX_RETRY_TIME = 60;
    public static final boolean MOCK_ALLOWED = true;
}
