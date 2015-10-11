package com.example.shubhamkanodia.roadrunner;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by shubhamkanodia on 07/09/15.
 */

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "7mKOLxU48Xnx1MoBET8StzmzRyJoxIEjxo4Omnrp", "1ApEXOD1V22d8lPAd8KDvKNa9KDRbhzmbztWk9JQ");

//        Parse.initialize(this, "wQ0QGvJnHy1UdN3Q7qVmNgJnmIGavG1YTWGgw8RX", "5SbSrgLlvmjQ0zP1DbHVBHNvz0nmlKsSqU2sn41y");
    }
}