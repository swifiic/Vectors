package com.arnavdhamija.roamnet;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by nic on 23/2/18.
 */

public class FileModule {
    private File dataDirectory;
    private File fileLedger;
    private File destinationStrategy;
    private final String TAG = "FileModule";

    FileModule() {
        dataDirectory = new File(Environment.getExternalStorageDirectory()+"/RoamnetData");
        if (!dataDirectory.exists()) {
            if (dataDirectory.mkdir());
        }
        Log.d(TAG, "Created dir");
        buildFileLedger();
    }

    void buildFileLedger() {
        File[] files = dataDirectory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, files[i].getName());
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
