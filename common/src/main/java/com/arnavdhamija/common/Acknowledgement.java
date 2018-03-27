package com.arnavdhamija.common;

/**
 * Created by abhishek on 27/3/18.
 */

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Acknowledgement {

    static final String TAG = "ACK-JSON";

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("ack_time")
    @Expose
    private Long ackTime;
    @SerializedName("items")
    @Expose
    private List<AckItem> items = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAckTime() {
        return ackTime;
    }

    public void setAckTime(Long ackTime) {
        this.ackTime = ackTime;
    }

    public List<AckItem> getItems() {
        return items;
    }

    public List<String> getAckedFilenames() {
        List<String> filenames = new ArrayList<>();
        for (AckItem item : items) {
            filenames.add(item.getFilename());
        }
        return getAckedFilenames();
    }

    public void setItems(List<AckItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        Log.d(TAG, "JSON string " + gson.toJson(this));
        return gson.toJson(this);
    }

    public static Acknowledgement fromString(String jsonEncodedAcknowledgement) {
        Gson gson = new Gson();
        return gson.fromJson(jsonEncodedAcknowledgement, Acknowledgement.class);
    }

}

