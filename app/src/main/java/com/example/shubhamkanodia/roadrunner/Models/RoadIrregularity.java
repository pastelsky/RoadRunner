package com.example.shubhamkanodia.roadrunner.Models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by shubhamkanodia on 20/12/15.
 */
public class RoadIrregularity extends RealmObject {

    public static final int THRESHOLD_VIBRATION = 8;
    public static final int THRESHOLD_LOW = 10;
    public static final int THRESHOLD_MEDIUM = 13;
    public static final int THRESHOLD_HIGH = 16;
    public static final int THRESHOLD_VERY_HIGH = 19;
    //Constants
    private static final int LEVEL_VIBRATION = 0;
    private static final int LEVEL_LOW = 1;
    private static final int LEVEL_MEDIUM = 2;
    private static final int LEVEL_HIGH = 3;
    private static final int LEVEL_VERY_HIGH = 4;
    private static final int LEVEL_EXTREME = 5;
    double latitude;
    double longitude;

    Date timeRecorded;
    int intensity;

    public RoadIrregularity(int intensity, double latitude, double longitude, Date timeRecorded) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeRecorded = timeRecorded;
        this.intensity = intensity;
    }

    public static int getIntensityLevel(double normalizedValue) {

        if (normalizedValue <= THRESHOLD_VIBRATION)
            return LEVEL_VIBRATION;

        else if (normalizedValue <= THRESHOLD_LOW)
            return LEVEL_LOW;

        else if (normalizedValue <= THRESHOLD_MEDIUM)
            return LEVEL_MEDIUM;

        else if (normalizedValue <= THRESHOLD_HIGH)
            return LEVEL_HIGH;

        else if (normalizedValue <= THRESHOLD_VERY_HIGH)
            return LEVEL_VERY_HIGH;
        else
            return LEVEL_EXTREME;

    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public Date getTimeRecorded() {
        return timeRecorded;
    }

    public void setTimeRecorded(Date timeRecorded) {
        this.timeRecorded = timeRecorded;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
