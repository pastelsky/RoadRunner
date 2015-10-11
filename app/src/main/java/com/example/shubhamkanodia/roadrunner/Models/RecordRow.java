package com.example.shubhamkanodia.roadrunner.Models;

import io.realm.RealmObject;

/**
 * Created by shubhamkanodia on 11/10/15.
 */
public class RecordRow extends RealmObject {

    private long start_time;
    private long end_time;

    private String t_mode;

    private double distance;
    private boolean is_synced;
    private boolean is_syncing;

    private double end_lat;

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public String getT_mode() {
        return t_mode;
    }

    public void setT_mode(String t_mode) {
        this.t_mode = t_mode;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean is_synced() {
        return is_synced;
    }

    public void setIs_synced(boolean is_synced) {
        this.is_synced = is_synced;
    }

    public boolean is_syncing() {
        return is_syncing;
    }

    public void setIs_syncing(boolean is_syncing) {
        this.is_syncing = is_syncing;
    }

    public double getEnd_lat() {
        return end_lat;
    }

    public void setEnd_lat(double end_lat) {
        this.end_lat = end_lat;
    }

    public double getEnd_long() {
        return end_long;
    }

    public void setEnd_long(double end_long) {
        this.end_long = end_long;
    }

    public double getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(double start_lat) {
        this.start_lat = start_lat;
    }

    public double getStart_long() {
        return start_long;
    }

    public void setStart_long(double start_long) {
        this.start_long = start_long;
    }

    private double end_long;

    private double start_lat;
    private double start_long;

}