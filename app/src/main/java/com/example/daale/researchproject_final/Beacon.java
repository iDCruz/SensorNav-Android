package com.example.daale.researchproject_final;

import android.util.Log;

/**
 * Created by daale on 4/25/2016.
 */
public class Beacon {
    private String uuid;
    private int min;
    private int major;
    private String location;
    private int approxDistance;
    private double averageRSSI;
    private int prevDistance;
    private int lastRSSI;

    public Beacon(String uuid, int major, int min, String location){
        this.uuid = uuid;
        this.major = major;
        this.min = min;
        this.location = location;
        this.averageRSSI = 0;
        this.approxDistance = 0;
        this.prevDistance = -1;
    }

    public int calculateDistance(int rssi){
        averageRSSI = .25*rssi + .75*averageRSSI;
        double ratio = averageRSSI/-53;

        //the following block - nonlinear regression
        /*if(ratio <= 1){
            approxDistance = (int)Math.round(Math.pow(ratio, 10));
        }
        else {
            //-53 is the average rssi at 1 m
            approxDistance = (int) Math.round((Math.pow(ratio, 7.4543985) * 1.5865492 - 0.4678046));
        }*/

        //the following block - best fit curve (3rd order)
        approxDistance = (int) Math.round((-0.00071916 * Math.pow(averageRSSI,3)) - (0.09745645 * Math.pow(averageRSSI, 2)) - (4.41875938 * averageRSSI) - 66.72374563);
        if(approxDistance < 0){
            approxDistance = 0;
        }
        return approxDistance;
    }

    public void setLastRSSI(int rssi){
        lastRSSI = rssi;
    }

    public int getLastRSSI(){
        return lastRSSI;
    }

    public void setPrevDistance(int d){
        prevDistance = d;
    }

    public double getAverageRSSI(){
        return averageRSSI;
    }

    public int getPrevDistance(){
        return prevDistance;
    }

    public int getApproxDistance(){
        return approxDistance;
    }

    public String getLocation(){
        return location;
    }

    public int getMajor(){
        return major;
    }

    public int getMin(){
        return min;
    }
}
