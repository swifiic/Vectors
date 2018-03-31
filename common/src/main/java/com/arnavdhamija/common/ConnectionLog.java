package com.arnavdhamija.common;

import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;

import java.time.Clock;
import java.util.ArrayList;

public class ConnectionLog {
    private final static String TAG = "ConnectionLog";
    private String source;
    private String destination;
    private long connectionStartedTime;
    private long connectionTerminatedTime;
    ArrayList<String> filesSent;
    ArrayList<String> filesReceived;

    public ConnectionLog(String source, String destination) {
        this.source = source;
        this.destination = destination;
        filesSent = new ArrayList<>();
        filesReceived = new ArrayList<>();
        connectionStartedTime = System.currentTimeMillis()/1000;
    }

    public String getConnectionStartedTime() {
        return Long.toString(connectionStartedTime);
    }

    public void addSentFile(String filename) {
        filesSent.add(filename);
    }

    public void addReceivedFile(String filename) {
        filesReceived.add(filename);
    }

    public void connectionTerminated() {
        connectionTerminatedTime = System.currentTimeMillis()/1000;
    }

    public static ConnectionLog fromString(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ConnectionLog.class);
    }

    public String toString() {
        Gson gson = new Gson();
        Log.d(TAG, "JSON string " + gson.toJson(this));
        return gson.toJson(this);
    }
}
