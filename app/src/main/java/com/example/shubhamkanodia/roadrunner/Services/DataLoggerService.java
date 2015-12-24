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
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Activities.GPSPermissionDialog;
import com.example.shubhamkanodia.roadrunner.Activities.RunnerWidget;
import com.example.shubhamkanodia.roadrunner.Events.ServiceStopEvent;
import com.example.shubhamkanodia.roadrunner.Helpers.Constants;
import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.example.shubhamkanodia.roadrunner.Helpers.XYZProcessor;
import com.example.shubhamkanodia.roadrunner.Models.Journey;
import com.example.shubhamkanodia.roadrunner.Models.RoadIrregularity;
import com.example.shubhamkanodia.roadrunner.R;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by shubhamkanodia on 05/09/15.
 */


public class DataLoggerService extends Service implements SensorEventListener {

    public static boolean wasStartedSuccessfully = false;
    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            destroyService();
        }
    };
    String HARDCODED_EMAIL = "abc@example.com";
    Location initLocation;
    Location currentLocation;
    Location checkLocation;
    SoundPool ourSounds;

    int soundLow;
    int soundmedium;
    int soundHigh;
    int soundVeryHigh;
    int soundExtreme;


    boolean isGPSConnected = false;
    long lastUpdate = 0;

    LocationManager mlocManager;
    LocationListener locationListener;
    SensorManager senSensorManager;
    Sensor senAccelerometer;

    Vibrator vibrator;

    ArrayList<Double> slidingWindow;

    Date startTime;
    boolean requireMovement;
    Realm realm;
    RealmList<RoadIrregularity> roadIrregularityRealmList;

    Timer noMovementTimer;
    TextToSpeech mTts;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initService();
//        initSounds();
        roadIrregularityRealmList = new RealmList<>();

        if (wasStartedSuccessfully)
            destroyService();
        else {
            Toast.makeText(this, "Recording data in background. Stop recording from notification bar when done.", Toast.LENGTH_LONG).show();

        }


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

        startForeground(Constants.RECORD_NOTIF_ID, notification);

        startListeningForNoMovement();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!Constants.MOCK_ALLOWED && android.os.Build.VERSION.SDK_INT >= 18 && location.isFromMockProvider()) {
                    Toast.makeText(DataLoggerService.this, "You seem to be using mock locations. Cannot continue.", Toast.LENGTH_SHORT).show();
                    destroyService();
                }
                if (!isGPSConnected) {
                    checkLocation = location;
                    initLocation = location;
                    isGPSConnected = true;

                }
                currentLocation = location;
                Log.e("GPS", "MOVING:" + currentLocation.getLatitude() + ":" + currentLocation.getLongitude());

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

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);

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
//    private void initSounds() {
//
//        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .setUsage(AudioAttributes.USAGE_GAME)
//                .build();
//        ourSounds = new SoundPool.Builder()
//                .setMaxStreams(15)
//                .setAudioAttributes(audioAttributes)
//                .build();
//        soundLow = ourSounds.load(this, R.raw.low, 1);
//        soundmedium = ourSounds.load(this, R.raw.medium, 1);
//        soundHigh = ourSounds.load(this, R.raw.high, 1);
//        soundVeryHigh = ourSounds.load(this, R.raw.veryhigh, 1);
//        soundExtreme = ourSounds.load(this, R.raw.extreme, 1);
//    }


    private void startListeningForNoMovement() {

        noMovementTimer = new Timer();
        noMovementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double displacement = checkLocation.distanceTo(currentLocation);
                Log.e("TIMER", "Checking for movement in last 5 seconds = " + displacement + "\n Between points: "
                               + checkLocation + " and " + currentLocation);
                checkLocation = currentLocation;

                if (displacement < Constants.MINIMUM_REQD_DISPLACEMENT) {
                    Log.e("TIMER", "Entering IDLE STATE");


                    unregisterListeners();
                    startBackOff();
                }
            }
        }, Constants.MOVEMENT_CHECK_INITIAL_DELAY * 1000, Constants.MOVEMENT_CHECK_INTERVAL * 1000);


    }

    private void startBackOff() {

        Callable<Boolean> callable = new Callable<Boolean>() {
            public Boolean call() throws Exception {

                if (ContextCompat.checkSelfPermission(DataLoggerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(DataLoggerService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }

                Location location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                double newDisplacement = checkLocation.distanceTo(location);

                Log.e("RETRY", "Movement after backoff = " + newDisplacement + "\n Between points: "
                               + checkLocation + " and " + currentLocation);
                checkLocation = currentLocation;


                if (newDisplacement < Constants.MINIMUM_RETRY_DISPLACEMENT)
                    return null;
                else {
                    mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
                    senSensorManager.registerListener(DataLoggerService.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

                    startListeningForNoMovement();
                    return true;
                }

            }
        };


        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(Predicates.<Boolean>isNull())
                .withWaitStrategy(WaitStrategies.fibonacciWait(Constants.RETRY_MULTIPLIER, Constants.MAX_RETRY_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(15))
                .build();

        try {
            retryer.call(callable);
        } catch (RetryException e) {
            e.printStackTrace();
            Log.e("RETRY", "All attempts to retry failed.");
            destroyService();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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

        Toast.makeText(this, "STOPPING SERVICE", Toast.LENGTH_SHORT).show();

        EventBus.getDefault().post(new ServiceStopEvent());
        updateWidgetStatus(false);

        unregisterListeners();

        if (wasStartedSuccessfully) {

            Date endTime = new Date();

            long totalJourneyTime = (endTime.getTime() - startTime.getTime()) / 1000;

            if (totalJourneyTime < Constants.MINIMUM_RECORD_TIME) {
                Toast.makeText(this, "Data set too small. Try recording for a longer time!", Toast.LENGTH_SHORT).show();
            } else if (Constants.REQUIRE_MOVEMENT && initLocation.distanceTo(currentLocation) < Constants.MINIMUM_REQD_DISPLACEMENT) {
                Toast.makeText(this, "Did not detect any geographical movement.", Toast.LENGTH_SHORT).show();
            } else {

                Journey newJourney = new Journey(initLocation.getLatitude(), currentLocation.getLatitude(), initLocation.getLongitude(), currentLocation.getLongitude(),
                        startTime, endTime, HARDCODED_EMAIL);

                realm.beginTransaction();
                newJourney.setroadIrregularityRealmList(roadIrregularityRealmList);
                realm.copyToRealm(newJourney);
                realm.commitTransaction();

                if (Helper.isOnlineOnWifi(this)) {
                    startService(new Intent(this, UploadService.class));
                }
            }


            wasStartedSuccessfully = false;

        }

        unregisterReceiver(stopServiceReceiver);


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


    public void unregisterListeners() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }


        mlocManager.removeUpdates(locationListener);
        senSensorManager.unregisterListener(this);
        if (noMovementTimer != null)
            noMovementTimer.cancel();

    }

    public void initService() {

        realm = Realm.getInstance(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        registerReceiver(stopServiceReceiver, new IntentFilter("myFilter"));

        startTime = new Date();
        slidingWindow = new ArrayList<Double>();

        updateWidgetStatus(true);
        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                mTts.setLanguage(Locale.US);

            }
        });

        String myText1 = "Did you sleep well?";
        String myText2 = "I hope so, because it's time to wake up.";
        mTts.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);
        mTts.speak(myText2, TextToSpeech.QUEUE_ADD, null);

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

            if ((curTime - lastUpdate) > Constants.SENSOR_UPDATE_INTERVAL) {
                lastUpdate = curTime;

                if (slidingWindow.size() >= Constants.SLIDING_WINDOW_CAPACITY) {
                    Log.e("STDEV: ", "LEVEL:  " + Helper.stdev(slidingWindow));

                    double stddev = Helper.stdev(slidingWindow);

                    if (stddev > RoadIrregularity.THRESHOLD_VIBRATION && isGPSConnected) {

                        int intensity = RoadIrregularity.getIntensityLevel(stddev);
                        Log.e("STDEV: ", "LEVEL:  " + intensity);
                        Toast.makeText(this, "LEVEL: " + intensity, Toast.LENGTH_SHORT).show();

                        realm.beginTransaction();

                        RoadIrregularity roadIrregularity = new RoadIrregularity(intensity, currentLocation.getLatitude(), currentLocation.getLongitude(), new Date());
                        RoadIrregularity toSave = realm.copyToRealm(roadIrregularity);

                        realm.commitTransaction();
                        roadIrregularityRealmList.add(roadIrregularity);
                        playSound(intensity);


                    }

                    slidingWindow.clear();

                }
                slidingWindow.add(processor.normalizedValue);

            }
        }

    }

    private void playSound(int intensity) {
        int sounds[] = {soundLow, soundmedium, soundHigh, soundVeryHigh, soundExtreme};
        ourSounds.play(sounds[intensity-1], 0.9f, 0.9f, 1, 0, 1);
    }

}