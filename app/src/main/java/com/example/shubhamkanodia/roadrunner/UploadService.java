package com.example.shubhamkanodia.roadrunner;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UploadService extends Service {

    ParseObject recordRow;
    ZipOutputStream out;
    File curFile;
    String curName;
    StringBuilder dataString;

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

        Bundle dataSent = intent.getExtras();
        dataSent.getLong("startTime");


        ParseQuery<ParseObject> query = ParseQuery.getQuery("GameScore");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, com.parse.ParseException e) {

                recordRow = list.get(0);


                record.add("start_time", startTime);
                record.add("end_time", System.currentTimeMillis());
                record.add("isSynced", false);
                record.add("isSyncing", false);
                record.add("start_lat", initLat);
                record.add("start_long", initLong);
                record.add("end_lat", curLat);
                record.add("end_long", curLong);
                record.add("upload_progress", 0);





            }

        });



    }


    public String getTransport(int i) {
        switch (i) {
            case 0:
                return "car";
            case 1:
                return "bus";
            case 2:
                return "bike";
            case 3:
                return "cycle";

        }
        return null;
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
