package com.example.shubhamkanodia.roadrunner.Models;

import com.parse.ParseObject;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by shubhamkanodia on 20/12/15.
 */
public class RoadIrregularity extends RealmObject {

    public static final double THRESHOLD_VIBRATION = 2.3f;
    public static final double THRESHOLD_LOW = 3.3f;
    public static final double THRESHOLD_MEDIUM = 4.5f;
    public static final double THRESHOLD_HIGH = 6f;
    public static final double THRESHOLD_VERY_HIGH = 7.5f;

    //Constants
    public static final int LEVEL_LOW = 1;
    public static final int LEVEL_MEDIUM = 2;
    public static final int LEVEL_HIGH = 3;
    public static final int LEVEL_VERY_HIGH = 4;
    public static final int LEVEL_EXTREME = 5;

    private double latitude;
    private double longitude;

    private Date timeRecorded;
    private int intensity;

    public RoadIrregularity() {
    }

    ;

    public RoadIrregularity(int intensity, double latitude, double longitude, Date timeRecorded) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeRecorded = timeRecorded;
        this.intensity = intensity;
    }

    public static int getIntensityLevel(double normalizedValue) {

        if (normalizedValue <= THRESHOLD_LOW)
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

    public static ParseObject convertToParseObject(RoadIrregularity r) {

        ParseObject p = new ParseObject("RoadIrregularities");

        p.add("latitude", r.getLatitude());
        p.add("longitude", r.getLongitude());
        p.add("timeRecorded", r.getTimeRecorded());
        p.add("intensity", r.getIntensity());

        return p;
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
