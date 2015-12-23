package com.example.shubhamkanodia.roadrunner.Services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.example.shubhamkanodia.roadrunner.Models.Journey;
import com.example.shubhamkanodia.roadrunner.Models.RoadIrregularity;
import com.example.shubhamkanodia.roadrunner.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import io.realm.Realm;
import io.realm.RealmResults;

public class UploadService extends Service {

    static int syncedCount;
    ZipOutputStream out;
    File curFile;
    String curName;
    StringBuilder dataString;
    Realm realm;
    ParseObject recording;
    long recordingStartTime;
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    int notif_id = 4;
    RealmResults<Journey> journeyRealmResults;


    public UploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Helper.isOnlineOnWifi(getApplicationContext())) {

            realm = Realm.getInstance(this);


            journeyRealmResults = realm.where(Journey.class)
                    .equalTo("isSynced", false)
                    .findAll();

            Log.e("journeylist size", "" + journeyRealmResults.size());

            final int size = journeyRealmResults.size();

            if (journeyRealmResults.size() > 0) {

                mNotifyManager =
                        (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setContentTitle("Uploading data..." + size + " Journies")
                        .setSmallIcon(R.drawable.ic_upload)
                        .setProgress(journeyRealmResults.size(), 0, false)
                        .setOngoing(true);

                startForeground(notif_id,
                        mBuilder.build());

                //Save each irregularity and journey to parse
                for (final Journey j : journeyRealmResults) {
                    ParseObject p = Journey.convertToParseObject(j);

                    List<ParseObject> irregularityList = new ArrayList<>();

                    for (RoadIrregularity r : j.getroadIrregularityRealmList()) {
                        ParseObject pr = RoadIrregularity.convertToParseObject(r);
                        pr.saveInBackground();
                        irregularityList.add(pr);
                    }
                    p.addAll("irregularityList", irregularityList);

                    p.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.e("Success ", "Added to parse");
                                realm.beginTransaction();
                                j.setSynced(true);
                                realm.commitTransaction();
                                syncedCount++;
                                mBuilder.setProgress(journeyRealmResults.size(), syncedCount, false);
                                mNotifyManager.notify(notif_id, mBuilder.build());

                            } else {
                                Log.e("Failure ", "Yeah");
                            }
                            if (syncedCount == size) {
                                mBuilder.setOngoing(false);
                                stopForeground(true);
                                Log.e("NOTIFICATION:", "CANCELLING");
                                syncedCount = 0;
                            }
                        }
                    });


                }
            }


            //For each journey, get all road irregularties bounded by start and end time
        }

        return START_STICKY;

    }
}
