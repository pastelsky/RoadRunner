package com.example.shubhamkanodia.roadrunner.Models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shubhamkanodia on 04/09/15.
 */
public class SensorRecorder extends RealmObject {

    private long currentTime;

    private float xData;
    private float yData;
    private float zData;

    private float speed;

    private double latitude;
    private double longitude;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public float getxData() {
        return xData;
    }

    public void setxData(float xData) {
        this.xData = xData;
    }

    public float getyData() {
        return yData;
    }

    public void setyData(float yData) {
        this.yData = yData;
    }

    public float getzData() {
        return zData;
    }

    public void setzData(float zData) {
        this.zData = zData;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
