package com.example.shubhamkanodia.roadrunner.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.example.shubhamkanodia.roadrunner.R;

public class GPSPermissionDialog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        dialog.setMessage("We need your GPS data to be recorded too!");
        dialog.setIcon(R.drawable.ic_location);
        dialog.setTitle("Please enable Location");
        dialog.setPositiveButton("Turn On GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                //get gps
            }
        });

        dialog.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });

        dialog.show();
    }

}

