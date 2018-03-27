package in.swifiic.shmbridge;

import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by nic on 21/3/18.
 */

public class DestinationAck {
    class AckEntry {}
    private ArrayList<Long> timestampRcvd;
    private long timestampAcked;
    private final static String TAG = "DestAck";

    private ArrayList<String> ackedFiles;

    DestinationAck(long timeAcked, ArrayList<Long> _timeRcvd, ArrayList<String> _ackedFiles) {
        timestampAcked = timeAcked;
        timestampRcvd = _timeRcvd;
        ackedFiles = _ackedFiles;
    }

    public long getAckTimestamp() {
        return timestampAcked;
    }

    public ArrayList<Long> getTimestampRcvd() {
        return timestampRcvd;
    }

    public ArrayList<String> getAckedFiles() {
        return ackedFiles;
    }

    public static DestinationAck fromString(String jsonEncodedDestAck) {
        Gson gson = new Gson();
        return gson.fromJson(jsonEncodedDestAck, DestinationAck.class);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        Log.d(TAG, "JSON string " + gson.toJson(this));
        return gson.toJson(this);
    }
}
