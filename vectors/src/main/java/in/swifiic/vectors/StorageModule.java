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

package in.swifiic.vectors;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import in.swifiic.vectors.helper.Acknowledgement;
import in.swifiic.vectors.helper.ConnectionLog;
import in.swifiic.vectors.helper.VideoData;

/**
 * Class for managing all File IO on the Internal Storage of the device
 * Maintains a directory of received payloads in VectorsData/ and corresponding logs in VectorsLogs/
 */

public class StorageModule {
    private File dataDirectory;
    private File logDirectory;
    private final String TAG = "StorageModule";
    private Context mContext;

    public StorageModule(Context context) {
        dataDirectory = new File(Environment.getExternalStorageDirectory()+ Constants.FLDR);
        logDirectory = new File(Environment.getExternalStorageDirectory()+Constants.FOLDER_LOG);
        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }
        if (!logDirectory.exists()) {
            logDirectory.mkdir();
        }
        mContext = context;
    }

    public static String convertFileListToCSV(List<String> files) {
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
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false), Constants.FILE_BUFFER_SIZE);
            writer.write(data);
            writer.close();
        } catch (Exception e) {
        }
    }

    public void writeLogBuffer(StringBuilder logBuffer) {
        if (logBuffer != null) {
            String data = logBuffer.toString();
            Long currTime = System.currentTimeMillis()/1000;
            writeFile(new File(logDirectory,
                    Constants.LOGGER_FILENAME + "_" + Long.toString(currTime)), data);
        }
    }

    public void writeConnectionLog(ConnectionLog connectionLog) {
        if (connectionLog != null) {
            String data = connectionLog.toString();
            writeFile(new File(logDirectory, Constants.CONNECTION_LOG_FILENAME + "_" +
                                connectionLog.getConnectionStartedTime()+".json"), data);
        }
    }

    public void writeToJSONFile(VideoData videoData) {
        if (videoData != null) {
            String data = videoData.toString();
            writeFile(new File(dataDirectory, videoData.getFileName() + ".json"), data);
        }
    }

    public void writeAckToJSONFile(Acknowledgement destinationAck) {
        if (destinationAck != null) {
            String data = destinationAck.toString();
            writeFile(new File(dataDirectory, Constants.ACK_FILENAME + ".json"), data);
        }
    }

    public String getFileListToSend() {
        /* First query the list of files to get video data
           Automatically deletes the expired content
         */
        File[] filesPreTTLCheck = dataDirectory.listFiles();
        for (int i = 0; i < filesPreTTLCheck.length; i++) {
            String fileName = filesPreTTLCheck[i].getName();
            if (!fileName.contains(".json")) {
                VideoData vd = getVideoDataFromFile(fileName);
            }
        }

        File[] files = dataDirectory.listFiles();
        StringBuilder csvFileList = new StringBuilder();
        int fileCount =0;
        if (files != null) {
            ArrayList<String> jsonList = new ArrayList<String>();
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (!fileName.contains(".json")) {
                    if (csvFileList.length() > 0) {
                        csvFileList.append(",");
                    }
                    csvFileList.append(fileName);
                    fileCount++;
                } else {
                    jsonList.add(fileName);
                }
            }
            String retVal = csvFileList.toString();
            // Additional logic to remove orphaned json files
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
            if(fileCount > Constants.MAX_FILES_TO_LIST){
                String[] elements=retVal.split(",");
                int incCountMax = 1 + (2 *fileCount) / Constants.MAX_FILES_TO_LIST ;
                Random rn = new Random();
                int truncatedListCount=0;
                StringBuilder csvFileList2 = new StringBuilder();
                csvFileList2.append(elements[0]);
                for(int i =1; i < elements.length; ){
                    csvFileList2.append(",");
                    csvFileList2.append(elements[i]);
                    int randomInc = rn.nextInt(incCountMax);
                    i=i+ randomInc + 1;
                    truncatedListCount++;
                }
                retVal = csvFileList2.toString();
                Log.w(TAG, "getFileList reduced from " + fileCount  + " to " + truncatedListCount);
            }
            return retVal;
        }
        return "";
    }

    public String getFileListComplete() {
        /* No optimization - just return all the files
         */

        File[] files = dataDirectory.listFiles();
        StringBuilder csvFileList = new StringBuilder();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (!fileName.contains(".json")) {
                    if (csvFileList.length() > 0) {
                        csvFileList.append(",");
                    }
                    csvFileList.append(fileName);
                }
            }
            String retVal = csvFileList.toString();

            return retVal;
        }
        return "";
    }

    public boolean deleteFile(String filename) {
        boolean jsonDeleted = true;
        boolean fileDeleted = true;
        if (filename.startsWith(Constants.PAYLOAD_PREFIX)) {
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
            VideoData vd = VideoData.fromString(videoDataJSON);

            // Delete payloads in which the TTL has expired
            if (vd.getCreationTime() + vd.getTtl() < System.currentTimeMillis() / 1000) {
                deleteFile(vd.getFileName());
                return null;
            }
            return vd;
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
        } catch (Exception e) {
            Log.d(TAG, "Ack not found" + e.getMessage());
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
