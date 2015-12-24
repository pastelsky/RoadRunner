package com.example.shubhamkanodia.roadrunner.Models;

import com.example.shubhamkanodia.roadrunner.Helpers.Haversine;
import com.example.shubhamkanodia.roadrunner.Helpers.Helper;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private String startTime;
    private String endTime;

    private String volunteerIdentity;
    private String volunteerEmail;


    private boolean isSynced;
    private RealmList<RoadIrregularity> roadIrregularityRealmList;
    private double distance;
    private String startAddress;
    private String endAddress;

    public Journey() {
    }

    public Journey(double startLat, double endLat, double startLong, double endLong, String startTime, String endTime, String volunteerEmail) {
        this.startLat = startLat;
        this.endLat = endLat;
        this.startLong = startLong;
        this.endLong = endLong;
        this.startTime = startTime;
        this.endTime = endTime;
        this.volunteerEmail = volunteerEmail;
        this.isSynced = false;
        this.roadIrregularityRealmList = new RealmList<>();
        this.distance = Math.round(Haversine.haversine(startLat, startLong, endLat, endLong) * 100.0) / 100.0;
    }

    public static ParseObject convertToParseObject(Journey j) {
        Date startDate = null, endDate = null;
        ParseObject p = new ParseObject("Journeys");

        p.put("startLat", j.getStartLat());
        p.put("startLong", j.getStartLong());

        p.put("endLat", j.getEndLat());
        p.put("endLong", j.getEndLong());

//        p.put("startTime", j.getStartTime());
//        p.put("endTime", j.getEndTime());

        p.put("volunteerEmail", j.getVolunteerEmail());
        p.put("volunteerIdentity", ParseInstallation.getCurrentInstallation());

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        try {
            startDate = formatter.parse(j.getStartTime());
            endDate = formatter.parse(j.getEndTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        p.put("startTime", startDate);
        p.put("endTime", endDate);


        return p;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getStartAddress() {
        return this.startAddress;
    }

    public void setEndAddress(String startAddress) {
        this.endAddress = startAddress;
    }

    public String getEndAddress() {
        return this.endAddress;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double d) {
        this.distance = distance;
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

    public String getStartTime() {

        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String endTime) {
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
