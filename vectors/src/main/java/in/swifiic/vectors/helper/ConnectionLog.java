/***************************************************************************
 *   Copyright (C) 2018 by The SWiFiIC Project <apps4rural@gmail.com>      *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA            *
 ***************************************************************************/

/***************************************************************************
 *   Code for Campus Experiments: April 2018                               *
 *   Authors: Abhishek Thakur, Arnav Dhamija, Tejashwar Reddy G            *
 ***************************************************************************/

package in.swifiic.vectors.helper;

import android.util.Log;

import com.google.gson.Gson;

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
