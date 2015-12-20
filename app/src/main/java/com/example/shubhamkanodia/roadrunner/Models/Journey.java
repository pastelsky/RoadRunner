package com.example.shubhamkanodia.roadrunner.Models;

import com.parse.ParseObject;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by shubhamkanodia on 20/12/15.
 */
public class Journey extends RealmObject {


    double startLat;
    double endLat;

    double startLong;
    double endLong;

    Date startTime;
    Date endTime;

    String volunteerIdentity;
    String volunteerEmail;

    public Journey(double startLat, double endLat, double startLong, double endLong, Date startTime, Date endTime, String volunteerIdentity, String volunteerEmail) {
        this.startLat = startLat;
        this.endLat = endLat;
        this.startLong = startLong;
        this.endLong = endLong;
        this.startTime = startTime;
        this.endTime = endTime;
        this.volunteerIdentity = volunteerIdentity;
        this.volunteerEmail = volunteerEmail;
    }

    public static ParseObject convertToParseObject(Journey j) {

        ParseObject p = new ParseObject("Journey");

        p.add("startLat", j.getStartLat());
        p.add("startLong", j.getStartLong());

        p.add("endLat", j.getEndLat());
        p.add("endLong", j.getEndLong());

        p.add("startTime", j.getStartTime());
        p.add("endTime", j.getEndTime());

        p.add("volunteerEmail", j.volunteerEmail);
        p.add("volunteerIdentity", j.volunteerIdentity);

        return p;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getStartLong() {
        return startLong;
    }

    public void setStartLong(double startLong) {
        this.startLong = startLong;
    }

    public double getEndLong() {
        return endLong;
    }

    public void setEndLong(double endLong) {
        this.endLong = endLong;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getVolunteerIdentity() {
        return volunteerIdentity;
    }

    public void setVolunteerIdentity(String volunteerIdentity) {
        this.volunteerIdentity = volunteerIdentity;
    }

    public String getVolunteerEmail() {
        return volunteerEmail;
    }

    public void setVolunteerEmail(String volunteerEmail) {
        this.volunteerEmail = volunteerEmail;
    }


}
