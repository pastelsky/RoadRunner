package com.example.shubhamkanodia.roadrunner.Services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Events.UploadChangeEvent;
import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.example.shubhamkanodia.roadrunner.Models.RecordRow;
import com.example.shubhamkanodia.roadrunner.Models.SensorRecorder;
import com.example.shubhamkanodia.roadrunner.R;
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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class UploadService extends Service {

    ZipOutputStream out;
    File curFile;
    String curName;
    StringBuilder dataString;

    Realm realm;
    ParseObject recording;

    long recordingStartTime;


    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    int totalCount;
    int completedCount;
    int successCount = 0;


    public UploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        realm = Realm.getInstance(this);


        if (intent.getBooleanExtra("upload_bulk", false))
            uploadAll();
        else {


            recordingStartTime = intent.getLongExtra("start_time", 0);
            Log.e("Time: ", intent.getExtras() + "");


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

            totalCount = 1;
            completedCount = 0;
            saveData((RecordRow) rq.findFirst());
        }

        return START_STICKY;

    }

    public void uploadAll() {

        RealmQuery<RecordRow> query = realm.where(RecordRow.class);
        List<RecordRow> filtered = new ArrayList<RecordRow>();
        RealmResults<RecordRow> results = query.findAll();



        for (int i = 0; i < results.size(); i++) {

            RecordRow r = results.get(i);
            if (!r.is_synced() && !r.is_syncing()) {
                filtered.add(r);
                realm.beginTransaction();
                r.setIs_syncing(true);
                r.setTo_address(Helper.getAddress(this, r.getStart_lat(), r.getStart_long()));
                r.setFrom_address(Helper.getAddress(this, r.getEnd_lat(), r.getEnd_long()));
                realm.commitTransaction();
            }
        }

        EventBus.getDefault().post(new UploadChangeEvent("started"));


        if (filtered.size() == 0) {
            Toast.makeText(this, "Hurray! All files are aready synced!", Toast.LENGTH_SHORT).show();
            return;
        }
        totalCount = filtered.size();
        completedCount = 0;


        mNotifyManager =
                (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Uploading " + totalCount + " files...")
                .setContentText("Done 0/" + totalCount)
                .setSmallIcon(R.drawable.ic_upload)
                .setProgress(0, totalCount, false);

        startForeground(2,
                mBuilder.build());

        for (int i = 0; i < filtered.size(); i++) {
            saveData(filtered.get(i));
            Log.e("ITERATING" , i + "");
        }
    }


    public void saveData(RecordRow toUpload) {

        mBuilder.setContentTitle("Writing data to file...");
        mNotifyManager.notify(2, mBuilder.build());

        prepareDataFile(toUpload);

        realm.beginTransaction();
        toUpload.setIs_syncing(true);
        toUpload.setTo_address(Helper.getAddress(this, toUpload.getStart_lat(), toUpload.getStart_long()));
        toUpload.setFrom_address(Helper.getAddress(this, toUpload.getEnd_lat(), toUpload.getEnd_long()));
        realm.commitTransaction();

        EventBus.getDefault().post(new UploadChangeEvent("started upload"));

        mBuilder.setContentTitle("Uploading data...");
        mNotifyManager.notify(2, mBuilder.build());

        saveFileToParse(toUpload);

        try {
            out.closeEntry();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveFileToParse(final RecordRow toUpload) {

        final ParseFile dataFile = new ParseFile(String.valueOf(dataString).getBytes(), "text/csv");
        dataFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException error) {


                if (error == null) {

                    recording = new ParseObject("Recordings");
                    recording.put("start_time", toUpload.getStart_time());
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
                            completedCount ++;

                            if (completedCount == totalCount) {
                                mNotifyManager.cancel(2);
                                stopSelf();
                            } else {
                                mBuilder.setContentText("Processing: " + completedCount + "/" + totalCount)
                                        .setProgress(totalCount, completedCount, false);
                                mNotifyManager.notify(2, mBuilder.build());
                            }

                            if (e == null) {
                                Toast.makeText(UploadService.this, "Posted!", Toast.LENGTH_SHORT).show();
                                realm.beginTransaction();
                                toUpload.setIs_synced(true);
                                toUpload.setIs_syncing(false);
                                realm.commitTransaction();
                                successCount++;
                                EventBus.getDefault().post(new UploadChangeEvent("success"));


                                Log.e("SAVED", completedCount + ":" +  successCount);

                            } else

                            {
                                realm.beginTransaction();
                                toUpload.setIs_synced(false);
                                toUpload.setIs_syncing(false);
                                realm.commitTransaction();
                                Toast.makeText(UploadService.this, "Could not upload data: " + e.getMessage() + +e.getCode(), Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new UploadChangeEvent("failure"));


                            }
                        }

                    });
                } else

                {
                    realm.beginTransaction();
                    toUpload.setIs_synced(false);
                    toUpload.setIs_syncing(false);
                    realm.commitTransaction();

                    EventBus.getDefault().post(new UploadChangeEvent("Complete failure"));


                    Toast.makeText(UploadService.this, "Cannot Post!" + error.getMessage() + error.getCode(), Toast.LENGTH_SHORT).show();
                    mNotifyManager.cancel(2);
                    stopSelf();


                }
            }

        });
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

    public void prepareDataFile(RecordRow toUpload) {

        createFileStream();
        dataString = new StringBuilder();

        RealmQuery<SensorRecorder> query = realm.where(SensorRecorder.class);
        query.between("currentTime", toUpload.getStart_time(), toUpload.getEnd_time());

        RealmResults<SensorRecorder> results = query.findAll();
        for (SensorRecorder sr : results) {

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
        } catch (IOException e) {
            e.printStackTrace();
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
