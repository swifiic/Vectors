package in.swifiic.common;

import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nic on 23/2/18.
 */

// filename structure video0_1_64_0_10_100

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
        Log.d(TAG, "JSON string " + gson.toJson(this));
        return gson.toJson(this);
    }

    public VideoData() {
        traversal = new ArrayList<>();
    }
}
