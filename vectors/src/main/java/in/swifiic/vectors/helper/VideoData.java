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
import android.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VideoData {
    private final static String TAG = "VideoData";
    private String fileName;
    private int sequenceNumber;
    private int tickets;
    private int temporalLayer;
    private int maxTemporalLayer;
    private int svcLayer;
    private int maxSvcLayer;
    private long creationTime; // unix time
    private int ttl; // in seconds
    private String sourceNode;
    private String destinationNode;
    private ArrayList<Pair<Long, String>> traversal;

    public void addTraversedNode(String deviceName) {
        for (Pair pair : traversal) {
            String _deviceName = (String)pair.second;
            if (_deviceName.compareTo(deviceName) == 0) {
                return;
            }
        }
        Long epochTimeSeconds = System.currentTimeMillis()/1000;
        Pair<Long, String> pair = new Pair<>(epochTimeSeconds, deviceName);
        traversal.add(pair);
    }

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

    public int getTemporalLayer() {
        return temporalLayer;
    }

    public void setTemporalLayer(int temporalLayer) {
        this.temporalLayer = temporalLayer;
    }

    public int getMaxTemporalLayer() {
        return maxTemporalLayer;
    }

    public void setMaxTemporalLayer(int maxTemporalLayer) {
        this.maxTemporalLayer = maxTemporalLayer;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSvcLayer() {
        return svcLayer;
    }

    public void setSvcLayer(int svcLayer) {
        this.svcLayer = svcLayer;
    }

    public int getMaxSvcLayer() {
        return maxSvcLayer;
    }

    public void setMaxSvcLayer(int maxSvcLayer) {
        this.maxSvcLayer = maxSvcLayer;
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

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(String destinationNode) {
        this.destinationNode = destinationNode;
    }

    public static VideoData fromString(String jsonEncodedVideoData) {
        Gson gson = new Gson();
        return gson.fromJson(jsonEncodedVideoData, VideoData.class);
    }

    public static void sortListCopyCount(List<VideoData> videoDataList) {
        Collections.sort(videoDataList, new Comparator<VideoData>() {
            @Override
            public int compare(VideoData o1, VideoData o2) {
                if (o1.getTickets() > o2.getTickets()) { // test if this is descending order
                    return -1;
                } else {
                    if(o1.getTickets() == o2.getTickets())
                        return 0;
                    else
                        return 1;
                }
            }
        });
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public VideoData() {
        traversal = new ArrayList<>();
    }
}
