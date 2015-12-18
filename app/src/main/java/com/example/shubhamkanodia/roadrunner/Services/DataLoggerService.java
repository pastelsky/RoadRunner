package com.example.shubhamkanodia.roadrunner.Services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Activities.MainActivity;
import com.example.shubhamkanodia.roadrunner.Activities.PostActivity;
import com.example.shubhamkanodia.roadrunner.Events.ServiceStopEvent;
import com.example.shubhamkanodia.roadrunner.Models.SensorRecorder;
import com.example.shubhamkanodia.roadrunner.R;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by shubhamkanodia on 05/09/15.
 */


public class DataLoggerService extends Service implements SensorEventListener {
    private static final String DEBUG_TAG = "AccelLoggerService";
    private static final double THRESHOLD_ACCELERATION = 11;
    private static final int SLIDING_WINDOW_CAPACITY = 5;
    private static final int notif_id = 1;
    private static int MINIMUM_RECORD_TIME;
    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };
    double curLat, curLong;
    double initLat = 0, initLong = 0;
    long lastUpdate = 0;
    LocationManager mlocManager;
    LocationListener locationListener;
    Vibrator v;
    ArrayList<Double> slidingWindow;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long startTime;
    private boolean requireMovement;
    private float last_x, last_y, last_z;
    private Realm realm;
//
//    recording.put("end_time", new Date());
//    recording.put("end_Lat", curLat);
//    recording.put("end_Long", curLong);
//    recording.put("is_syncing", false);
//    recording.put("is_synced", false);
//    recording.put("uploadProgress", 0);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        realm = Realm.getInstance(this);
        startTime = System.currentTimeMillis();

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        MINIMUM_RECORD_TIME = Integer.parseInt(sharedPrefs.getString("min_record_time", "5"));
        requireMovement = sharedPrefs.getBoolean("require_movement", true);


        Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_run));
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        slidingWindow = new ArrayList<Double>();


        registerReceiver(stopServiceReceiver, new IntentFilter("myFilter"));
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent("myFilter"), PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Recording data")
                .setTicker("Recording your movements. Don't forget to finish recording when done.")
                .setContentText("RoadRunner is running. You should too.")
                .setSubText(" Don't forget to finish recording when done.")
                .setSmallIcon(R.drawable.ic_run_notif)
                .setLargeIcon(bm)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.ic_stop,
                        "Stop Recording", contentIntent).build();

        startForeground(notif_id,
                notification);

        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (initLat == 0 || initLong == 0) {
                    initLat = location.getLatitude();
                    initLong = location.getLongitude();
                }

                curLat = location.getLatitude();
                curLong = location.getLongitude();

//                Log.e("LOCATION:", curLat + " : " + curLong);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.e("LOCATION:", "Changed");

            }

            @Override
            public void onProviderEnabled(String s) {
                Log.e("LOCATION:", "Started");

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.e("LOCATION:", "Stopped");

            }
        };

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


        mlocManager.addGpsStatusListener(new android.location.GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {

                if (event == GpsStatus.GPS_EVENT_STOPPED) {
                    stopSelf();

                }
            }
        });


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.e("MAX_ACCL_RANGE", "RANGE: " + senAccelerometer.getMaximumRange() + "\nResolution: " + senAccelerometer.getResolution() + "\nDelays: " + senAccelerometer.getMinDelay() + " - " + senAccelerometer.getMinDelay());
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);


        //createFileStream

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onDestroy() {

        EventBus.getDefault().post(new ServiceStopEvent());


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        mlocManager.removeUpdates(locationListener);

        unregisterReceiver(stopServiceReceiver);

        long totTime = (System.currentTimeMillis() - startTime) / 1000;
        if (totTime < MINIMUM_RECORD_TIME) {
            Toast.makeText(this, "Data set too small. Try recording for a longer time!", Toast.LENGTH_SHORT).show();
            Intent m = new Intent(this, MainActivity.class);
            m.setAction(Intent.ACTION_MAIN);
            m.addCategory(Intent.CATEGORY_LAUNCHER);
            m.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivity(m);
        } else if (requireMovement && (curLat == 0 || curLong == 0)) {
            Toast.makeText(this, "Did not detect any geographical movement. Move it!", Toast.LENGTH_SHORT).show();
            Intent m = new Intent(this, MainActivity.class);
            m.setAction(Intent.ACTION_MAIN);
            m.addCategory(Intent.CATEGORY_LAUNCHER);
            m.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivity(m);
        } else {
            Intent resultIntent = new Intent(this, PostActivity.class);

            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);

            resultIntent.putExtra("start_time", startTime);
            resultIntent.putExtra("end_time", System.currentTimeMillis());
            resultIntent.putExtra("start_lat", initLat);
            resultIntent.putExtra("end_lat", curLat);
            resultIntent.putExtra("start_long", initLong);
            resultIntent.putExtra("end_long", curLong);

            startActivity(resultIntent);

            stopSelf();
        }


    }

    public double stdev() {
        double sum = 0.0;
        double mean = 0.0;
        double num = 0.0;
        double numi = 0.0;
        double deno = 0.0;
        for (double n : slidingWindow) {
            sum += n;
            mean = sum / slidingWindow.size() - 1;

        }

        for (double n : slidingWindow) {
            numi = Math.pow((n - mean), 2);
            num += numi;
            deno = slidingWindow.size() - 1;
        }


        double stdevResult = Math.sqrt(num / deno);
        return stdevResult;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 80) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;


                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                realm.beginTransaction();

                SensorRecorder sr = realm.createObject(SensorRecorder.class);
                sr.setxData(x);
                sr.setyData(y);
                sr.setzData(z);
                sr.setCurrentTime(System.currentTimeMillis());
                sr.setSpeed(speed);
                sr.setLatitude(curLat);
                sr.setLongitude(curLong);

                realm.commitTransaction();
                double normalizedAcceleration = Math.pow(x * x + y * y + z * z, 0.5);

                if (slidingWindow.size() >= SLIDING_WINDOW_CAPACITY) {
                    Log.e("STDEV: ", "STDEV: " + stdev());
                    slidingWindow.clear();
                }
                slidingWindow.add(normalizedAcceleration);


                if (normalizedAcceleration > THRESHOLD_ACCELERATION) {
                    v.vibrate(50);
                    Log.e("NORMAL", "X:" + x + "\nY:" + y + "\nZ:" + z + "\n N:" + normalizedAcceleration + "\nwindow" + slidingWindow + "\nDev: " + stdev());

                }

                last_x = x;
                last_y = y;
                last_z = z;

            }
        }

    }


}