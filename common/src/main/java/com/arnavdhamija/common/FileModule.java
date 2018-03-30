package com.arnavdhamija.common;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by nic on 23/2/18.
 */

public class FileModule {
    private File dataDirectory;
    private File fileLedger;
    private File destinationStrategy;
    private final String TAG = "FileModule";
    private final int ticketCount = 128;
    private DatabaseModule mDatabaseModule;
    private Context mContext;
    boolean useDb = false;

    public FileModule(Context context) {
        dataDirectory = new File(Environment.getExternalStorageDirectory()+"/RoamnetData");
        if (!dataDirectory.exists()) {
            if (dataDirectory.mkdir());
            Log.d(TAG, "Created dir");
        }
        mContext = context;
//        mDatabaseModule = new DatabaseModule(mContext, null, null, 1);
//        buildFileLedger();
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

    public String getFileName(String filename) {
        return getFileName(Uri.fromFile(new File(dataDirectory, filename)));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private int getRequiredTickets(int svcLayer) {
        switch (svcLayer) {
            case 0:
                return 32;
            case 1:
                return 16;
            case 2:
                return 12;
            case 3:
                return 10;
            case 4:
                return 8;
            case 5:
                return 7;
            case 6:
                return 6;
            default:
                return -1;
        }
    }

// filename structure video0_1_0_10_100
    public void buildFileLedger() {
        String fileName;
        String tokens[];
        File[] files = dataDirectory.listFiles();
        Log.d(TAG, "FILELIST" + getFileList());
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                fileName = files[i].getName();
//                test if this ignores which already have a JSON
                if (!fileName.contains(".json") && fileName.startsWith(Constants.VIDEO_PREFIX) && Arrays.asList(files).indexOf(fileName + ".json") == -1) {
                    tokens = fileName.split("_");
                    VideoData videoData = new VideoData();
                    if (tokens.length == 5) {
                        videoData.setFileName(fileName);
                        videoData.setSequenceNumber(Integer.parseInt(tokens[1]));
                        videoData.setTemporalLayer(Integer.parseInt(tokens[2]));
                        videoData.setMaxTemporalLayer(Integer.parseInt(tokens[3]));
                        videoData.setCreationTime(Integer.parseInt(tokens[4]));
                        videoData.setTickets(getRequiredTickets(videoData.getTemporalLayer()));
                    } else {
                        Log.d(TAG, "Invalid file");
                    }
                    if (useDb) {
//                        mDatabaseModule.addVideoData(videoData);
                    } else {
                        File file = new File(dataDirectory, videoData.getFileName() + ".json");
                        if (!file.exists()) {
                            writeToJSONFile(videoData);
                        }
                    }
                    Log.d(TAG, fileName);
                }
            }
        }
    }

    public void writeToJSONFile(VideoData videoData) {
        if (videoData != null) {
            String data = videoData.toString();
            try {
                FileWriter writer = new FileWriter(new File(dataDirectory, videoData.getFileName() + ".json"), false);
                writer.write(data);
                writer.close();
                Log.d(TAG, "File written" + videoData.getFileName());
            } catch (IOException e) {
                Log.d(TAG, "File write failed");
            }
        } else {
            Log.d(TAG, "null???");
        }
    }

    public void writeAckToJSONFile(Acknowledgement destinationAck) {
        String data = destinationAck.toString();
        try {
            FileWriter writer = new FileWriter( new File(dataDirectory, Constants.ACK_FILENAME + ".json"), false);
            writer.write(data);
            writer.close();
            Log.d(TAG, "File written");
        } catch (IOException e) {
            Log.d(TAG, "File write failed");
        }
    }

    public String getFileList() {
        File[] files = dataDirectory.listFiles();
        String fileName;
        StringBuilder csvFileList = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                fileName = files[i].getName();
                if (!fileName.contains(".json")) {
                    if (i > 0) {
                        csvFileList.append(",");
                    }
                    csvFileList.append(fileName);
                }
            }
            return csvFileList.toString();
        } else {
            return "";
        }
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
