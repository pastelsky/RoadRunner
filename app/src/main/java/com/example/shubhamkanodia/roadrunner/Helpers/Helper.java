package com.example.shubhamkanodia.roadrunner.Helpers;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.shubhamkanodia.roadrunner.Activities.MainActivity;
import com.example.shubhamkanodia.roadrunner.Fragments.MainFragment;
import com.example.shubhamkanodia.roadrunner.Services.UploadService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by shubhamkanodia on 05/09/15.
 */
public class Helper {

    static Context c;

    public static void setContext(Context co){
        c = co;
    }

    public static boolean isOnline(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public static boolean isOnlineOnWifi(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifi.isConnected();
    }

    public static String getAddress(Context ctx, double latitude, double longitude) {


        Geocoder geocoder = new Geocoder(ctx, Locale.ENGLISH);

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null ) {

                Address fetchedAddress = addresses.get(0);

                if(fetchedAddress.getSubLocality() != null )
                    return fetchedAddress.getSubLocality().toString();
                else if(fetchedAddress.getLocality() != null )
                    return fetchedAddress.getLocality();
                else
                    return "Offline Data";


            } else
                return " ";


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return " ";
        }
    }

    public static void uploadData(Context co, long startTime){
        Intent serviceIntent = new Intent(co, UploadService.class);
        serviceIntent.putExtra("start_time", startTime);
        c.startService(serviceIntent);
    }

}
