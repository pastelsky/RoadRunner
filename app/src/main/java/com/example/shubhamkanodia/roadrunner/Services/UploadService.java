package com.example.shubhamkanodia.roadrunner.Services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Events.UploadChangeEvent;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Models.RecordRow;
import com.example.shubhamkanodia.roadrunner.Models.SensorRecoder;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class UploadService extends Service {

    ParseObject recordRow;
    ZipOutputStream out;
    File curFile;
    String curName;
    StringBuilder dataString;

    Realm realm;
    ParseObject recording;

    long recordingStartTime;
    RecordRow toUpload;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;


    public UploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        recordingStartTime = intent.getLongExtra("start_time", 0);

        realm = Realm.getInstance(this);


        mNotifyManager =
                (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Uploading data...")
                .setContentText("Preparing data to upload...")
                .setSmallIcon(R.drawable.ic_upload)
                .setProgress(0, 0, true);

        startForeground(2,
                mBuilder.build());


        RealmQuery<RecordRow> rq = realm.where(RecordRow.class);
        rq.equalTo("start_time", recordingStartTime);

        toUpload = (RecordRow) rq.findFirst();

        saveData();

        return START_STICKY;

    }

    public void saveData() {

        mBuilder.setContentText("Writing data to file...");
        mNotifyManager.notify(2, mBuilder.build());
        realm.beginTransaction();
        toUpload.setIs_syncing(true);
        realm.commitTransaction();

        EventBus.getDefault().post(new UploadChangeEvent("started"));



        createFileStream();
        dataString = new StringBuilder();

        RealmQuery<SensorRecoder> query = realm.where(SensorRecoder.class);
        query.between("currentTime", toUpload.getStart_time(), toUpload.getEnd_time());

        RealmResults<SensorRecoder> results = query.findAll();


        for ( SensorRecoder sr: results ) {

            String writeData = sr.getCurrentTime() + ", " +
                    sr.getxData() + ", " +
                    sr.getyData() + ", " +
                    sr.getzData() + ", " +
                    sr.getSpeed() + ", " +
                    sr.getLatitude() + ", " +
                    sr.getLongitude() + "\n";

            dataString.append(writeData);

            try {
                out.write(writeData.getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            InputStream is = new FileInputStream(curFile);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }


            buffer.flush();

            mBuilder.setContentText("Uploading data...");
            mNotifyManager.notify(2, mBuilder.build());

            final ParseFile dataFile = new ParseFile(String.valueOf(dataString).getBytes(), "text/csv");
            dataFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException error) {

                    if (error == null) {

                        recording = new ParseObject("Recordings");
                        recording.put("start_time", recordingStartTime);
                        recording.put("end_time", toUpload.getEnd_time());
                        recording.put("start_lat", toUpload.getStart_lat());
                        recording.put("start_long", toUpload.getStart_long());
                        recording.put("end_long", toUpload.getEnd_long());
                        recording.put("end_lat", toUpload.getEnd_lat());
                        recording.put("data_file", dataFile);
                        recording.put("t_mode", toUpload.getT_mode());
                        recording.saveEventually(new SaveCallback() {
                            @Override
                            public void done(com.parse.ParseException e) {
                                if (e == null) {
                                    Toast.makeText(UploadService.this, "Posted!", Toast.LENGTH_SHORT).show();
                                    realm.beginTransaction();
                                    toUpload.setIs_synced(true);
                                    realm.commitTransaction();
                                } else

                                {
                                    Toast.makeText(UploadService.this, "Could not upload data: " + e.getMessage() + +e.getCode(), Toast.LENGTH_SHORT).show();
                                }
                            }

                    });
                }

                else

                {
                    Toast.makeText(UploadService.this, "Cannot Post!" + error.getMessage() + error.getCode(), Toast.LENGTH_SHORT).show();
                }

                EventBus.getDefault().

                post(new UploadChangeEvent("stopped")

                );
                realm.beginTransaction();
                toUpload.setIs_syncing(false);
                realm.commitTransaction();

                mNotifyManager.cancel(2);

                stopSelf();


            }

        });


            out.closeEntry();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void createFileStream() {
        try {
            File newFolder = new File(Environment.getExternalStorageDirectory(), "RoadRunner");
            if (!newFolder.exists()) {
                newFolder.mkdir();
            }
            try {
                curName = System.currentTimeMillis() + "";
                curFile = new File(newFolder, curName + ".zip");
//                curFile.createNewFile();

                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(curFile);
                    out = new ZipOutputStream(new
                            BufferedOutputStream(fos));

                    ZipEntry entry = new ZipEntry(curName + ".txt");
                    out.putNextEntry(entry);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (Exception ex) {
                System.out.println("ex: " + ex);
            }
        } catch (Exception e) {
            System.out.println("e: " + e);
        }
    }

    public void zipFile(File file) {

        byte[] buffer = new byte[1024];

        try {

            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(System.currentTimeMillis() + ".zip");
            out.putNextEntry(e);


            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry ze = new ZipEntry(System.currentTimeMillis() + "txt.zip");
            zos.putNextEntry(ze);
            FileInputStream in = new FileInputStream(file.getAbsolutePath() + ".zip");

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            in.close();
            zos.closeEntry();

            zos.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
