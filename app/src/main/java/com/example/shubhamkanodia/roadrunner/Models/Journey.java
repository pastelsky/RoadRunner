package com.example.shubhamkanodia.roadrunner.Models;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmList;
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

    private String volunteerIdentity = "kaw kaw kaw";
    private String volunteerEmail = "shubhambobo@noob.com";


    private boolean isSynced;
    private RealmList<RoadIrregularity> roadIrregularityRealmList;

    public Journey() {
    }

    ;

    public Journey(double startLat, double endLat, double startLong, double endLong, Date startTime, Date endTime, String volunteerEmail) {
        this.startLat = startLat;
        this.endLat = endLat;
        this.startLong = startLong;
        this.endLong = endLong;
        this.startTime = startTime;
        this.endTime = endTime;
        this.volunteerIdentity = volunteerIdentity;
        this.volunteerEmail = volunteerEmail;
        this.isSynced = false;
        this.roadIrregularityRealmList = new RealmList<>();
    }

    public static ParseObject convertToParseObject(Journey j) {

        ParseObject p = new ParseObject("Journies");


//        ParseGeoPoint start = new ParseGeoPoint(j.getStartLat(), j.getStartLong());
//        ParseGeoPoint end = new ParseGeoPoint(j.getEndLat(), j.getEndLong());
//
//        p.put("startGeoPoint", start);
//        p.put("endGeoPoint", end);

        p.put("startLat", j.getStartLat());
        p.put("startLong", j.getStartLong());

        p.put("endLat", j.getEndLat());
        p.put("endLong", j.getEndLong());

        p.put("startTime", j.getStartTime());
        p.put("endTime", j.getEndTime());

        p.put("volunteerEmail", j.volunteerEmail);
        p.put("volunteerIdentity", j.volunteerIdentity);


          //p.add();
//        p.addAll("irregularity", j.getroadIrregularityRealmList());

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

    public void setroadIrregularityRealmList(RealmList<RoadIrregularity> r) {
        this.roadIrregularityRealmList.addAll(r);
    }

    public RealmList<RoadIrregularity> getroadIrregularityRealmList() {
        return this.roadIrregularityRealmList;
    }
}
