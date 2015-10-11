package com.example.shubhamkanodia.roadrunner.Models;

import android.text.format.DateUtils;

import com.example.shubhamkanodia.roadrunner.Helpers.Haversine;

/**
 * Created by shubhamkanodia on 04/09/15.
 */
public class RecordingItem {

    private long start_time;
    private  long end_time;

    private double distance;
    private boolean isSynced;
    private boolean isSyncing;


    private double start_lat;
    private double start_long;

    public RecordingItem(RecordRow recordRow){

        this.start_time = recordRow.getStart_time();
        this.end_time = recordRow.getEnd_time();
        this.distance = Math.round(Haversine.haversine(start_lat, start_long, end_lat, end_long) * 100.0) / 100.0 ;
        this.isSynced = recordRow.is_synced();
        this.isSyncing = recordRow.is_syncing();
        this.start_lat = recordRow.getStart_lat();
        this.start_long = recordRow.getStart_long();
        this.end_lat = recordRow.getEnd_lat();
        this.end_long =recordRow.getEnd_long();
        this.upload_progress = 0;

    }

    public RecordingItem(long start_time, long end_time, boolean isSynced, boolean isSyncing, double start_lat, double start_long, double end_lat, double end_long, int upload_progress) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.distance = Math.round(Haversine.haversine(start_lat, start_long, end_lat, end_long) * 100.0) / 100.0 ;
        this.isSynced = isSynced;
        this.isSyncing = isSyncing;
        this.start_lat = start_lat;
        this.start_long = start_long;
        this.end_lat = end_lat;
        this.end_long = end_long;
        this.upload_progress = upload_progress;
    }

    private double end_lat;
    private double end_long;

    private int upload_progress;



    public String getEnd_time() {
        return DateUtils.getRelativeTimeSpanString(end_time).toString();
    }

    public double getDistance() {
        return Math.round(Haversine.haversine(start_lat, start_long, end_lat, end_long) * 100.0) / 100.0 ;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public boolean isSyncing() {
        return isSyncing;
    }

    public double getstart_lat() {
        return start_lat;
    }

    public double getstart_long() {
        return start_long;
    }

    public double getend_lat() {
        return end_lat;
    }

    public double getend_long() {
        return end_long;
    }

    public int getUpload_progress() {
        return upload_progress;
    }

    public String getStart_time() {
        return DateUtils.getRelativeTimeSpanString(start_time, System.currentTimeMillis(),DateUtils.MINUTE_IN_MILLIS).toString();
    }
}
