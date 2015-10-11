package com.example.shubhamkanodia.roadrunner.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    long startLat;
    long endLat;
    long startLong;
    long endLong;


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

        extras = getIntent().getExtras();
        realm = Realm.getInstance(this);

        startTime = extras.getLong("start_time");
        endTime = extras.getLong("end_time");
        startLat = extras.getLong("start_lat");
        endLat = extras.getLong("end_lat");
        startLong = extras.getLong("start_long");
        endLong = extras.getLong("end_long");

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
                    startService(serviceIntent);
                }
                else{
                    Toast.makeText(PostActivity.this, "Saved. Upload from MY TRIPS!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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

        Toast.makeText(this, "LATLONG:" + startLat + startLong + " : " + endLat + endLong, Toast.LENGTH_LONG).show();

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
}
