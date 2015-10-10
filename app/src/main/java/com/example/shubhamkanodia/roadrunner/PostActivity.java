package com.example.shubhamkanodia.roadrunner;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import net.soulwolf.widget.materialradio.MaterialRadioButton;
import net.soulwolf.widget.materialradio.MaterialRadioGroup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class PostActivity extends AppCompatActivity {

    RangeBar rangebar;
    MaterialRadioGroup rgTransport;
    TextView tvMinutes;
    TextView tvDist;

    Button bSave, bDiscard;

    Realm realm;
    private FileOutputStream fstream;
    ZipOutputStream out;
    File curFile;
    String curName;
    Bundle extras;
    StringBuilder dataString;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    private ParseObject recording;


    long startTime;
    long endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        rangebar = (RangeBar) findViewById(R.id.rangebar);
        tvMinutes = (TextView) findViewById(R.id.tvMinutes);
        tvDist = (TextView) findViewById(R.id.tvDist);

        rgTransport = (MaterialRadioGroup) findViewById(R.id.rgTransport);
        bSave = (Button) findViewById(R.id.bSave);
        bDiscard = (Button) findViewById(R.id.bDiscard);

        dataString = new StringBuilder();


        extras = getIntent().getExtras();
        realm = Realm.getInstance(this);

        startTime = extras.getLong("start_time");
        endTime = extras.getLong("end_time");

        final long totalMinutes = (endTime - startTime) / 1000 / 60;
        final double totalDist = Haversine.haversine(extras.getDouble("start_lat"), extras.getDouble("start_long"), extras.getDouble("end_lat"), extras.getDouble("end_long"));
        rangebar.setTickStart(0);
        rangebar.setTickEnd(Math.max(2, totalMinutes));
        tvMinutes.setText(totalMinutes + "");
        tvDist.setText( (Math.round(totalDist * 100.0) / 100.0) + "");


        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int i, int i1, String s, String s1) {
                tvMinutes.setText(i1 + "");
                endTime = startTime + i1 * 1000 * 60;

                if(i1 < Math.max(2, totalMinutes))
                {
                    tvDist.setText( "<" + (Math.round(totalDist * 100.0) / 100.0));

                }
                else{
                    tvDist.setText( (Math.round(totalDist * 100.0) / 100.0) + "");

                }
            }
        });

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        bDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        Log.e("YEAHHH", "okay3");

    }

    public void saveData() {

        createFileStream();

        RealmQuery<SensorRecoder> query = realm.where(SensorRecoder.class);
        query.between("currentTime", startTime, endTime);

        RealmResults<SensorRecoder> results = query.findAll();

        Log.e("YEAHHH", results.size() + "");

        for (int i = 0; i < results.size(); i++) {

            SensorRecoder sr = results.get(i);

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

            mNotifyManager =
                    (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle("Uploading data...")
                    .setContentText("Upload in progress")
                    .setSmallIcon(R.drawable.ic_run_notif)
                    .setProgress(0, 0, false);
            mNotifyManager.notify(2, mBuilder.build());

            final ProgressDialog pd = new ProgressDialog(PostActivity.this);
            pd.setMessage("Uploading data...");
            pd.setIndeterminate(true);
            pd.show();

            final ParseFile dataFile = new ParseFile(String.valueOf(dataString).getBytes(), "text/csv");
            dataFile.saveInBackground(new SaveCallback() {
                                          @Override
                                          public void done(ParseException error) {
                                              pd.hide();

                                              if (error == null) {

                                                  mNotifyManager.cancel(2);
                                                  //gps check
                                                  recording = new ParseObject("Recordings");
                                                  startTime = System.currentTimeMillis();
                                                  recording.put("start_time", startTime);
                                                  recording.put("end_time", endTime);
                                                  recording.put("start_lat", extras.getDouble("start_lat"));
                                                  recording.put("start_long", extras.getDouble("start_long"));
                                                  recording.put("end_long", extras.getDouble("end_long"));
                                                  recording.put("end_lat", extras.getDouble("end_lat"));
                                                  recording.put("data_file", dataFile);

                                                  View radioButton = rgTransport.findViewById(rgTransport.getCheckedRadioButtonId());
                                                  int radioId = rgTransport.indexOfChild(radioButton);

                                                  recording.put("t_mode", getTransport(radioId));
                                                  recording.saveEventually(new SaveCallback() {
                                                      @Override
                                                      public void done(ParseException e) {
                                                          if (e == null)
                                                              Toast.makeText(PostActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                                                          else {
                                                              Toast.makeText(PostActivity.this, "Cannot Post!" + e.getMessage() + +e.getCode(), Toast.LENGTH_SHORT).show();
                                                          }
                                                      }
                                                  });
                                              } else

                                              {
                                                  Toast.makeText(PostActivity.this, "Cannot Post!" + error.getMessage() + error.getCode(), Toast.LENGTH_SHORT).show();

                                              }
                                          }

                                      },
                    new ProgressCallback() {
                        @Override
                        public void done(Integer integer) {
                            Log.e("PROGRESS is :", integer + "");

                            mBuilder.setProgress(100, integer, false);
                            mNotifyManager.notify(2, mBuilder.build());


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
