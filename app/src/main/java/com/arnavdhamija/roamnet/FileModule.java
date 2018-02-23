package com.arnavdhamija.roamnet;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;

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

    FileModule(Context context) {
        dataDirectory = new File(Environment.getExternalStorageDirectory()+"/RoamnetData");
        if (!dataDirectory.exists()) {
            if (dataDirectory.mkdir());
        }
        Log.d(TAG, "Created dir");
        mContext = context;
        mDatabaseModule = new DatabaseModule(mContext, null, null, 1);
        buildFileLedger();
    }

    void buildFileLedger() {
        String fileName;
        String tokens[];
        File[] files = dataDirectory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                fileName = files[i].getName();
                tokens = fileName.split("_");
                VideoData videoData = new VideoData();
                videoData.setFileName(fileName);
                videoData.setSequenceNumber(Integer.parseInt(tokens[1]));
                videoData.setResolution(Integer.parseInt(tokens[2]));
                videoData.setFrameRate(Integer.parseInt(tokens[3]));
                videoData.setTickets(128);
                videoData.logVideoData();
                mDatabaseModule.addVideoData(videoData);
                Log.d(TAG, fileName);
            }
        }
    }

    int getFilesCount() {
        File[] files = dataDirectory.listFiles();
        if (files != null) {
            return files.length;
        }
        return 0;
    }
}
