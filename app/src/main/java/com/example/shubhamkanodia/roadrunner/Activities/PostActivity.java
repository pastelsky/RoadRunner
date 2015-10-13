package com.example.shubhamkanodia.roadrunner.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.example.shubhamkanodia.roadrunner.Helpers.Haversine;
import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.example.shubhamkanodia.roadrunner.R;
import com.example.shubhamkanodia.roadrunner.Models.RecordRow;
import com.example.shubhamkanodia.roadrunner.Services.UploadService;
import com.parse.ParseObject;

import net.soulwolf.widget.materialradio.MaterialRadioGroup;

import de.greenrobot.event.EventBus;
import io.realm.Realm;

public class PostActivity extends AppCompatActivity {

    RangeBar rangebar;
    MaterialRadioGroup rgTransport;
    TextView tvMinutes;
    TextView tvDist;

    Button bSave, bDiscard;

    Realm realm;
    Bundle extras;

    private ParseObject recording;

    long startTime;
    long endTime;
    double startLat;
    double endLat;
    double startLong;
    double endLong;


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

        realm = Realm.getInstance(this);

        extras = getIntent().getExtras();
        startTime = (long) extras.get("start_time");
        endTime = (long) extras.get("end_time");
        startLat = (double) extras.get("start_lat");
        endLat = (double) extras.get("end_lat");
        startLong = (double) extras.get("start_long");
        endLong = (double) extras.get("end_long");




        final long totalMinutes = (endTime - startTime) / 1000 / 60;
        final double totalDist = Haversine.haversine(startLat, startLong, endLat, endLong);
        rangebar.setTickStart(0);
        rangebar.setTickEnd(Math.max(2, totalMinutes));
        tvMinutes.setText(totalMinutes + "");
        tvDist.setText((Math.round(totalDist * 100.0) / 100.0) + "");


        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int i, int i1, String s, String s1) {
                tvMinutes.setText(i1 + "");
                endTime = startTime + i1 * 1000 * 60;

                if (i1 < Math.max(2, totalMinutes)) {
                    tvDist.setText("<" + (Math.round(totalDist * 100.0) / 100.0));

                } else {
                    tvDist.setText((Math.round(totalDist * 100.0) / 100.0) + "");

                }
            }
        });

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveTrip();

                if(Helper.isOnlineOnWifi(PostActivity.this)) {
                    Intent serviceIntent = new Intent(getApplicationContext(), UploadService.class);
                    serviceIntent.putExtra("start_time", startTime);
                    serviceIntent.putExtra("end_time", endTime);

                    startService(serviceIntent);
                    Toast.makeText(PostActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(PostActivity.this, "No Wifi Internet. Saved in MyTrips.", Toast.LENGTH_SHORT).show();
                }
                Intent m = new Intent(getApplicationContext(), MainActivity.class);
                PostActivity.this.startActivity(m);
            }
        });

        bDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
showWarning();
            }
        });

    }


    public void showWarning(){

        final AlertDialog.Builder dialog = new AlertDialog.Builder(PostActivity.this);
        dialog.setMessage("Recorded data will be lost");
        dialog.setTitle("Are you sure?");
        dialog.setPositiveButton("Discard Anyway", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                finish();

            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    public void saveTrip() {

        realm.beginTransaction();

        RecordRow r = realm.createObject(RecordRow.class);

        r.setEnd_lat(endLat);
        r.setEnd_long(endLong);
        r.setEnd_time(endTime);
        r.setIs_synced(false);
        r.setIs_syncing(false);
        r.setStart_lat(startLat);
        r.setStart_long(startLong);
        r.setT_mode(getTransport());
        r.setStart_time(startTime);
        r.setFrom_address("Unknown A");
        r.setTo_address("Unkown B");

        realm.commitTransaction();

    }

    public String getTransport() {

        View radioButton = rgTransport.findViewById(rgTransport.getCheckedRadioButtonId());
        int radioId = rgTransport.indexOfChild(radioButton);

        switch (radioId) {
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

    @Override
    public void onBackPressed() {
        showWarning();

    }
}
