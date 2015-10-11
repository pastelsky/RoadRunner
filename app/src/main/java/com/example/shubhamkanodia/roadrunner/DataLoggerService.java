package com.example.shubhamkanodia.roadrunner;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.realm.Realm;

/**
 * Created by shubhamkanodia on 05/09/15.
 */


public class DataLoggerService extends Service implements SensorEventListener {
    private static final String DEBUG_TAG = "AccelLoggerService";

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private final static int MINIMUM_RECORD_TIME = 5;
    private long startTime;

    double curLat, curLong;
    double initLat = 0, initLong = 0;

    long lastUpdate = 0;
    private float last_x, last_y, last_z;

    private static final int notif_id=1;
    private Realm realm;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        realm = Realm.getInstance(this);
        startTime = System.currentTimeMillis();

        Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_run));

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Recording data")
                .setTicker("Recording your movements. Don't forget to finish recording when done.")
                .setContentText("RoadRunner is running. You should too.")
                .setSubText(" Don't forget to finish recording when done.")
                .setSmallIcon(R.drawable.ic_run_notif)
                .setLargeIcon(bm)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.ic_stop,
                        "Stop Recording", pendingIntent).build();

        startForeground(notif_id,
                notification);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(initLat == 0 || initLong == 0){
                    initLat =location.getLatitude();
                    initLong = location.getLongitude();
                }

                curLat = location.getLatitude();
                curLong = location.getLongitude();

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        });

        mlocManager.addGpsStatusListener(new android.location.GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {

                if(event == GpsStatus.GPS_EVENT_STOPPED){
                    stopSelf();

                }
            }
        });


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);


        //createFileStream

        return START_STICKY;
    }
//
//    recording.put("end_time", new Date());
//    recording.put("end_Lat", curLat);
//    recording.put("end_Long", curLong);
//    recording.put("isSyncing", false);
//    recording.put("isSynced", false);
//    recording.put("uploadProgress", 0);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

@Override
public void onDestroy(){

    long totTime = ( System.currentTimeMillis() - startTime ) / 1000;
    if(totTime < MINIMUM_RECORD_TIME)
        Toast.makeText(this, "Data set too small. Try recording for a longer time!", Toast.LENGTH_SHORT).show();

    else {

        saveTrip();
        Intent resultIntent = new Intent(this, PostActivity.class);

        resultIntent.putExtra("start_time", startTime);
        resultIntent.putExtra("end_time", System.currentTimeMillis());
        resultIntent.putExtra("start_lat", initLat);
        resultIntent.putExtra("end_lat", curLat);
        resultIntent.putExtra("start_long", initLong);
        resultIntent.putExtra("end_long", curLong);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(resultIntent);
    }


}

    public void saveTrip(){
        ParseObject record = new ParseObject("RecordingList");
        record.add("start_time", startTime);
        record.add("end_time", System.currentTimeMillis());
        record.add("isSynced", false);
        record.add("isSyncing", false);
        record.add("start_lat", initLat);
        record.add("start_long", initLong);
        record.add("end_lat", curLat);
        record.add("end_long", curLong);
        record.add("upload_progress", 0);
        try {
            record.pin();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        record.pinInBackground();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 80 && curLat > 0) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;


                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;




                realm.beginTransaction();

                SensorRecoder sr = realm.createObject(SensorRecoder.class);
                sr.setxData(x);
                sr.setyData(y);
                sr.setzData(z);
                sr.setCurrentTime(System.currentTimeMillis());
                sr.setSpeed(speed);
                sr.setLatitude(curLat);
                sr.setLongitude(curLong);

                realm.commitTransaction();

                last_x = x;
                last_y = y;
                last_z = z;

            }
        }

    }



}