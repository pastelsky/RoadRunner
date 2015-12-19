package com.example.shubhamkanodia.roadrunner.Services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Activities.GPSPermissionDialog;
import com.example.shubhamkanodia.roadrunner.Activities.MainActivity;
import com.example.shubhamkanodia.roadrunner.Activities.PostActivity;
import com.example.shubhamkanodia.roadrunner.Activities.RunnerWidget;
import com.example.shubhamkanodia.roadrunner.Events.ServiceStopEvent;
import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.example.shubhamkanodia.roadrunner.Helpers.XYZProcessor;
import com.example.shubhamkanodia.roadrunner.R;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by shubhamkanodia on 05/09/15.
 */


public class DataLoggerService extends Service implements SensorEventListener {


    private static final double THRESHOLD_ACCELERATION = 13;
    private static final int SLIDING_WINDOW_CAPACITY = 5;
    private static final int notif_id = 1;
    private static final int MINIMUM_RECORD_TIME = 5000;
    public static boolean wasStartedSuccessfully = false;

    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            destroyService();
        }
    };


    double curLat, curLong;
    double initLat = 0, initLong = 0;
    long lastUpdate = 0;

    LocationManager mlocManager;
    LocationListener locationListener;
    SensorManager senSensorManager;
    Sensor senAccelerometer;


    Vibrator vibrator;

    ArrayList<Double> slidingWindow;

    long startTime;
    boolean requireMovement;
    Realm realm;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initService();

        if (wasStartedSuccessfully)
            destroyService();


        if (!Helper.isGPSEnabled(this)) {
            Intent m = new Intent(this, GPSPermissionDialog.class);
            m.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivity(m);
            stopSelf();
        } else {
            wasStartedSuccessfully = true;

        }

        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, new Intent("myFilter"), PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_run));
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Recording data")
                .setTicker("Recording vibrations. Don't forget to finish recording when done.")
                .setContentText(" Don't forget to finish recording when done.")
                .setSmallIcon(R.drawable.ic_run_notif)
                .setLargeIcon(bm)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.ic_stop,
                        "Stop Recording", contentIntent).build();

        startForeground(notif_id, notification);

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
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mlocManager.addGpsStatusListener(new android.location.GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {

                if (event == GpsStatus.GPS_EVENT_STOPPED) {
                    destroyService();
                }
            }
        });

        Log.e("MAX_ACCL_RANGE", "RANGE: " + senAccelerometer.getMaximumRange() + "\nResolution: " + senAccelerometer.getResolution() + "\nDelays: " + senAccelerometer.getMinDelay() + " - " + senAccelerometer.getMinDelay());

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
        updateWidgetStatus(false);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        mlocManager.removeUpdates(locationListener);
        senSensorManager.unregisterListener(this);

        if (wasStartedSuccessfully) {

            long totTime = (System.currentTimeMillis() - startTime) / 1000;

            Intent m = new Intent(this, MainActivity.class);
            m.setAction(Intent.ACTION_MAIN);
            m.addCategory(Intent.CATEGORY_LAUNCHER);
            m.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);


            if (totTime < MINIMUM_RECORD_TIME) {
                Toast.makeText(this, "Data set too small. Try recording for a longer time!", Toast.LENGTH_SHORT).show();
                startActivity(m);

            } else if (requireMovement && (curLat == 0 || curLong == 0)) {
                Toast.makeText(this, "Did not detect any geographical movement. Move it!", Toast.LENGTH_SHORT).show();
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
            }

            wasStartedSuccessfully = false;

        }

    }

    public void updateWidgetStatus(boolean isRunning) {

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, RunnerWidget.class);
        int widgetIds[] = awm.getAppWidgetIds(widgetComponent);

        for (int i = 0; i < widgetIds.length; i++) {

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.runner_widget);

            views.setTextViewText(R.id.tvRecord, isRunning ? "Stop recording" : "Record journey");
            views.setImageViewResource(R.id.ivRecordIcon, isRunning ? R.drawable.ic_record : R.drawable.ic_record_inactive);

            awm.updateAppWidget(widgetIds[i], views);

        }
    }

    public void initService() {
        realm = Realm.getInstance(this);
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        registerReceiver(stopServiceReceiver, new IntentFilter("myFilter"));


        startTime = System.currentTimeMillis();
        slidingWindow = new ArrayList<Double>();

        updateWidgetStatus(true);

    }

    public void destroyService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            XYZProcessor processor = new XYZProcessor(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 80) {
                lastUpdate = curTime;

                if (slidingWindow.size() >= SLIDING_WINDOW_CAPACITY) {
                    Log.e("STDEV: ", "STDEV: " + Helper.stdev(slidingWindow));
                    slidingWindow.clear();
                }
                slidingWindow.add(processor.normalizedValue);


                if (processor.normalizedValue > THRESHOLD_ACCELERATION) {
                    vibrator.vibrate(50);

                }

            }
        }

    }

}