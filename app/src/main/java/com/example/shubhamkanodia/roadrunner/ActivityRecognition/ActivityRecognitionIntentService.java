package com.example.shubhamkanodia.roadrunner.ActivityRecognition;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Activities.MainActivity;
import com.example.shubhamkanodia.roadrunner.Activities.SettingsActivity;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Services.DataLoggerService;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by shubhamkanodia on 22/12/15.
 */

public class ActivityRecognitionIntentService extends IntentService {


    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {


        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {

            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);


            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            Log.e("ACTIVITY RECOGNITION", "RECOGNISED ACITIVITY : " + getNameFromType(mostProbableActivity.getType()) + "with confidence: " + mostProbableActivity.getConfidence());


            if (mostProbableActivity.getType() == DetectedActivity.IN_VEHICLE) {
                showRecordNotification(mostProbableActivity.getConfidence());
            }

        }
    }

    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }


    private void showRecordNotification(float confidence) {
        Toast.makeText(this, "Driving detected with confidence : " + confidence, Toast.LENGTH_SHORT).show();

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent toMainFragment = PendingIntent.getActivity(this, 0, new Intent(getApplicationContext(), MainActivity.class), 0);
        PendingIntent recordIntent = PendingIntent.getService(this, 0, new Intent(this, DataLoggerService.class), 0);
        PendingIntent settingsIntent = PendingIntent.getActivity(this, 0, new Intent(this, SettingsActivity.class), 0);


        Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_run));

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Record your current journey?")
                .setTicker("You seem to have started a new trip. How about recording this journey?")
                .setLargeIcon(bm)
                .setContentIntent(toMainFragment)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.ic_record_action,
                        "Record", recordIntent)
                .addAction(R.drawable.ic_settings_action,
                        "Settings", recordIntent).build();

        nManager.notify(5, notification);
    }

}
