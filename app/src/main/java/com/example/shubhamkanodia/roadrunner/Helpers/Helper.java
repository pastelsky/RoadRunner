package com.example.shubhamkanodia.roadrunner.Helpers;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.shubhamkanodia.roadrunner.Services.UploadService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shubhamkanodia on 05/09/15.
 */
public class Helper {

    static Context c;

    public static void setContext(Context co) {
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
        NetworkInfo network = cm.getActiveNetworkInfo();

        return network != null && network.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isGPSEnabled(Context ctx) {
        LocationManager mlocManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            return false;
        }

        return gps_enabled;

    }

    public static String getAddress(Context ctx, double latitude, double longitude) {


        Geocoder geocoder = new Geocoder(ctx, Locale.ENGLISH);

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null) {

                Address fetchedAddress = addresses.get(0);

                if (fetchedAddress.getSubLocality() != null)
                    return fetchedAddress.getSubLocality().toString();
                else if (fetchedAddress.getLocality() != null)
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

    public static void uploadData(Context co, long startTime) {
        Intent serviceIntent = new Intent(co, UploadService.class);
        serviceIntent.putExtra("start_time", startTime);
        c.startService(serviceIntent);
    }

    public static double stdev(ArrayList<Double> slidingWindow) {
        double sum = 0.0;
        double mean = 0.0;
        double num = 0.0;
        double numi = 0.0;
        double deno = 0.0;


        for (double n : slidingWindow) {
            sum += n;
            mean = sum / slidingWindow.size() - 1;

        }

        for (double n : slidingWindow) {
            numi = Math.pow((n - mean), 2);
            num += numi;
            deno = slidingWindow.size() - 1;
        }


        double stdevResult = Math.sqrt(num / deno);
        return stdevResult;
    }


}
