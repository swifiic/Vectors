package com.arnavdhamija.roamnet;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;

/**
 * Created by nic on 23/2/18.
 */

// filename structure video0_1_64_0_10_100

public class VideoData {
    private final String TAG = "VideoData";
    private String fileName;
    private int sequenceNumber;
    private int tickets;
    private int svcLayer;
    private int maxLayer;
    private long creationTime; // unix time
    private int ttl; // in seconds

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public int getSvcLayer() {
        return svcLayer;
    }

    public void setSvcLayer(int svcLayer) {
        this.svcLayer = svcLayer;
    }

    public int getMaxLayer() {
        return maxLayer;
    }

    public void setMaxLayer(int maxLayer) {
        this.maxLayer = maxLayer;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTickets() {
        return tickets;
    }

    public void setTickets(int tickets) {
        this.tickets = tickets;
    }

    public void logVideoData() {
        Log.d(TAG, this.toString());
    }

    public void fromString(String jsonEncodedVideoData) {
        Gson gson = new Gson();
        VideoData res = gson.fromJson(jsonEncodedVideoData, VideoData.class);
        setFileName(res.getFileName());
        setSequenceNumber(res.getSequenceNumber());
        setTickets(res.getTickets());
        setSvcLayer(res.getSvcLayer());
        setMaxLayer(res.getMaxLayer());
        setCreationTime(res.getCreationTime());
        setTtl(res.getTtl());
        Log.d(TAG, "Done encoding");
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        Log.d(TAG, "JSON string " + gson.toJson(this));
        return gson.toJson(this);
    }

    VideoData(){}
}
