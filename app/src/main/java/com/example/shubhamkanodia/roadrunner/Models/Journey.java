package com.example.shubhamkanodia.roadrunner.Models;

import com.parse.ParseObject;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by shubhamkanodia on 20/12/15.
 */
public class Journey extends RealmObject {


    private double startLat;
    private double endLat;

    private double startLong;
    private double endLong;

    private Date startTime;
    private Date endTime;

    private String volunteerIdentity;
    private String volunteerEmail;


    private boolean isSynced;

    public Journey() {
    }

    ;

    public Journey(double startLat, double endLat, double startLong, double endLong, Date startTime, Date endTime, String volunteerIdentity, String volunteerEmail) {
        this.startLat = startLat;
        this.endLat = endLat;
        this.startLong = startLong;
        this.endLong = endLong;
        this.startTime = startTime;
        this.endTime = endTime;
        this.volunteerIdentity = volunteerIdentity;
        this.volunteerEmail = volunteerEmail;
        this.isSynced = false;

    }

    public static ParseObject convertToParseObject(Journey j) {

        ParseObject p = new ParseObject("Journies");

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

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
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
