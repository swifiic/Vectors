package com.arnavdhamija.common;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by nic on 23/2/18.
 */

public class FileModule {
    private File dataDirectory;
    private File logDirectory;
    private final String TAG = "FileModule";
    private Context mContext;

    public FileModule(Context context) {
        dataDirectory = new File(Environment.getExternalStorageDirectory()+Constants.FLDR);
        logDirectory = new File(Environment.getExternalStorageDirectory()+Constants.FOLDER_LOG);
        if (!dataDirectory.exists()) {
            if (dataDirectory.mkdir()) {
                Log.d(TAG, "Created dir");
            }
        }
        if (!logDirectory.exists()) {
            if (logDirectory.mkdir()) {
                Log.d(TAG, "Created log dir");
            }
        }
        mContext = context;
    }

    public static String convertListToCSV(List<String> files) {
        String fileName;
        StringBuilder csvFileList = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            fileName = files.get(i);
            if (!fileName.contains(".json")) {
                if (i > 0) {
                    csvFileList.append(",");
                }
                csvFileList.append(fileName);
            }
        }
        return csvFileList.toString();
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    public ParcelFileDescriptor getPfd(String filename) {
        Uri uri = Uri.fromFile(new File(dataDirectory, filename));
        Log.d(TAG, "Chosen file URI: " + uri);
        try {
            ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(uri, "r");
            return pfd;
        } catch (FileNotFoundException fne) {
            Log.d(TAG, "File not found for PFD");
        }
        return null;
    }

    private void writeFile(File file, String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false), 1024*16);
            writer.write(data);
            writer.close();
            Log.d(TAG, "generic file written" + file.getName());
        } catch (Exception e) {
        }
    }

    public void writeLogBuffer(StringBuilder logBuffer) {
        if (logBuffer != null) {
            String data = logBuffer.toString();
            Long currTime = System.currentTimeMillis()/1000;
            writeFile(new File(logDirectory,
                    Constants.LOGGER_FILENAME + "_" + Long.toString(currTime)), data);
        } else {
            Log.d(TAG, "Null logger File");
        }
    }

    public void writeConnectionLog(ConnectionLog connectionLog) {
        if (connectionLog != null) {
            String data = connectionLog.toString();
            writeFile(new File(logDirectory, Constants.CONNECTION_LOG_FILENAME + "_" +
                                connectionLog.getConnectionStartedTime()+".json"), data);
        } else {
            Log.d(TAG, "Null Log");
        }
    }

    public void writeToJSONFile(VideoData videoData) {
        if (videoData != null) {
            String data = videoData.toString();
            writeFile(new File(dataDirectory, videoData.getFileName() + ".json"), data);
        } else {
            Log.d(TAG, "null???");
        }
    }

    public void writeAckToJSONFile(Acknowledgement destinationAck) {
        if (destinationAck != null) {
            String data = destinationAck.toString();
            writeFile(new File(dataDirectory, Constants.ACK_FILENAME + ".json"), data);
        } else {
            Log.d(TAG, "Null");
        }
    }

    public String getQuickFileList() {
        File[] files = dataDirectory.listFiles();
        String fileName;
        StringBuilder csvFileList = new StringBuilder();
        if (files != null) {
            ArrayList<String> jsonList = new ArrayList<String>();
            for (int i = 0; i < files.length; i++) {
                fileName = files[i].getName();
                if (!fileName.contains(".json")) {
                    if (i > 0) {
                        csvFileList.append(",");
                    }
                    csvFileList.append(fileName);
                } else {
                    jsonList.add(fileName);
                }
            }
            String retVal = csvFileList.toString();
            if (!jsonList.isEmpty()) {
                for (int i = 0; i < jsonList.size(); i++) {
                    String fName = jsonList.get(i);
                    if (fName.contains(Constants.ACK_FILENAME))
                        continue;
                    String name = fName.substring(0, fName.indexOf(".json"));
                    if (retVal.contains(name))
                        continue;
                    File file = new File(dataDirectory, fName);
                    file.delete();
                    Log.w(TAG, "Deleted unmatched JSON file with name " + fName);
                }
            }
            return retVal;
        }
        return null;
    }

    public boolean deleteFile(String filename) {
        boolean jsonDeleted = true;
        boolean fileDeleted = true;
        if (filename.startsWith(Constants.VIDEO_PREFIX)) {
            File jsonFile = new File(dataDirectory, filename + ".json");
            jsonDeleted = jsonFile.delete();

        }
        File file = new File(dataDirectory, filename);
        fileDeleted = file.delete();
        return fileDeleted && jsonDeleted;
    }

    public VideoData getVideoDataFromFile(String videoFileName) {
        String jsonFilename = videoFileName + ".json";
        try {
            String videoDataJSON = new Scanner(new File(dataDirectory, jsonFilename)).useDelimiter("\\Z").next();
            return VideoData.fromString(videoDataJSON);
        } catch (IOException e) {
            deleteFile(videoFileName);
            Log.e(TAG, "File not found");
        }
        return null;
    }

    public Acknowledgement getAckFromFile() {
        String jsonFilename = Constants.ACK_FILENAME + ".json";
        try {
            String ackJSON = new Scanner(new File(dataDirectory, jsonFilename)).useDelimiter("\\Z").next();
            return Acknowledgement.fromString(ackJSON);
        } catch (IOException e) {
            Log.d(TAG, "Ack not found");
        }
        return null;
    }

    public int getFilesCount() {
        File[] files = dataDirectory.listFiles();
        if (files != null) {
            return files.length;
        }
        return 0;
    }
}
