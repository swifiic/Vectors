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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import in.swifiic.vectors.Constants;

public class Acknowledgement {

    static final String TAG = "ACK-JSON";
    @SerializedName("ack_time")
    @Expose
    private Long ack_time;
    @SerializedName("items")
    @Expose
    private List<AckItem> items = null;
    private ArrayList<Pair<Long, String>> traversal;

    public Long getAckTime() {
        return ack_time;
    }

    public void setAckTime(Long ackTime) {
        this.ack_time = ackTime;
    }

    public List<AckItem> getItems() {
        return items;
    }

    public void setItems(List<AckItem> items) {
        this.items = items;
    }

    public Acknowledgement() {
        traversal = new ArrayList<>();
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

    public void addTraversedNode(String deviceName) {
        for (Pair pair : traversal) {
            String _deviceName = (String)pair.second;
            if (_deviceName.compareTo(deviceName) == 0) {
                Log.d(TAG, "Device already traversed!");
                return;
            }
        }
        Long epochTimeSeconds = System.currentTimeMillis()/1000;
        Pair<Long, String> pair = new Pair<>(epochTimeSeconds, deviceName);
        traversal.add(pair);
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
        byte[] buf = new byte[32768];
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
            return fromString(outputString);
        } catch(java.io.UnsupportedEncodingException ex) {
            Log.e(TAG, "Encoding format exception");
        } catch (java.util.zip.DataFormatException ex) {
            Log.e(TAG, "Data format exception");
        }
        return null;
    }
}

