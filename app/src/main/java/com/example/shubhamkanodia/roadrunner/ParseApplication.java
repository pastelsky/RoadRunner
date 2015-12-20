package com.example.shubhamkanodia.roadrunner;

import android.app.Application;

import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.facebook.stetho.Stetho;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.interceptors.ParseStethoInterceptor;

/**
 * Created by shubhamkanodia on 07/09/15.
 */

public class ParseApplication extends Application {

    public boolean isDataLoggerServiceRunning = false;


    @Override
    public void onCreate() {
        super.onCreate();


        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        Parse.addParseNetworkInterceptor(new ParseStethoInterceptor());
        Parse.enableLocalDatastore(this);
        Helper.setContext(this);
        Stetho.initializeWithDefaults(this);

        Parse.initialize(this, "70PhhXAuM7JFQsw5jZEiCZvUOU8n2OYsd1XaChFI", "GcfAsGuyuOvuad7QE9t4Da6P0IciHa5mRqTcr9NZ");
        ParseInstallation.getCurrentInstallation().saveInBackground();



//        Parse.initialize(this, "wQ0QGvJnHy1UdN3Q7qVmNgJnmIGavG1YTWGgw8RX", "5SbSrgLlvmjQ0zP1DbHVBHNvz0nmlKsSqU2sn41y");
    }
}