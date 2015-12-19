package com.example.shubhamkanodia.roadrunner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shubhamkanodia.roadrunner.Helpers.Helper;

public class WifiConnectedReceiver extends BroadcastReceiver {
    public WifiConnectedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Helper.isOnlineOnWifi(context)){


        }

    }
}
