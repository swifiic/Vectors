package in.swifiic.common;

/**
 * Created by abhishek on 27/3/18.
 */

import android.text.util.Linkify;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
        return filenames;
    }

    public boolean containsFilename(String filename) {
        for (String s : getAckedFilenames()) {
            if (s.compareTo(filename) == 0) {
                return true;
            }
        }
        return false;
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

    public static byte[] getCompressedAcknowledgement(Acknowledgement ack) {
        byte[] input = ack.toString().getBytes();
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (!compresser.finished()) {
            int byteCount = compresser.deflate(buf);
            baos.write(buf, 0, byteCount);
        }
        compresser.end();

        byte[] compressedBytes = baos.toByteArray();
        Log.d(TAG, "Compressed Length = " + compressedBytes.length + " Original " + input.length);
        return compressedBytes;
    }

    public static Acknowledgement getDecompressedAck(byte[] compressedAckBytes) {
        int compressedDataLength = compressedAckBytes.length;
        try {
            Inflater decompresser = new Inflater();
            decompresser.setInput(compressedAckBytes, 0, compressedDataLength);
            byte[] result = new byte[Constants.ACK_BUFFER_SIZE];
            int resultLength = decompresser.inflate(result);
            decompresser.end();
            String outputString = new String(result, 0, resultLength, "UTF-8");
//            Log.d(TAG, "O/p string " + outputString);
            return fromString(outputString);
        } catch(java.io.UnsupportedEncodingException ex) {
            // handle
        } catch (java.util.zip.DataFormatException ex) {
            // handle
        }
        return null;
    }
}

