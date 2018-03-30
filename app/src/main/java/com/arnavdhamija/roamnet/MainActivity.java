//2do: test bgservice
//remove notifs
//nodes traversed
//del on ACK

package com.arnavdhamija.roamnet;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arnavdhamija.common.Constants;
import com.arnavdhamija.common.VideoData;
import com.google.android.gms.nearby.connection.Payload;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MainBGService mService;
    boolean mBound = false;

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    final String TAG = "RoamnetUI";

    private final SimpleArrayMap<Long, NotificationCompat.Builder> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, NotificationCompat.Builder> outgoingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> incomingPayloadReferences = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();
    private List<VideoData> incomingTransfersMetadata = new ArrayList<>();
    private SimpleArrayMap<Long, VideoData> outgoingTransfersMetadata = new SimpleArrayMap<>();

    void getPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            String[] requiredPermissionsArray = listPermissionsNeeded.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, requiredPermissionsArray, 0);
        }
    }

    boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
        case 0:
            if (grantResults.length > 0) {
                for (int i : grantResults) {
                    if (i == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Perm granted");
                        startApp();
                        return;
                    } else {
                        Log.d(TAG, "We didn't get perms :(");
                        break;
                    }
                }
            }
        }
    }

    private void enableRoamnet() {
        mEditor.putBoolean(Constants.STATUS_ENABLE_BG_SERVICE, true);
        mEditor.apply();
        mService.setBackgroundService();
    }

    private void disableRoamnet() {
        mEditor.putBoolean(Constants.STATUS_ENABLE_BG_SERVICE, false);
        mEditor.apply();
        mService.setBackgroundService();
    }

    private boolean getRoamnetStatus() {
        return mSharedPreferences.getBoolean(Constants.STATUS_ENABLE_BG_SERVICE, false);
    }

    private void setUIText() {
        final Button toggleRoamnetButton = findViewById(R.id.toggleRoamnet);
        if (mBound) {
            if (getRoamnetStatus()) {
                toggleRoamnetButton.setText("Stop Roamnet");
            } else {
                toggleRoamnetButton.setText("Start Roamnet");
            }
            customLogger("Service launched at: " + mService.getStartTime());
            TextView deviceIdView = findViewById(R.id.deviceIdView);
            deviceIdView.setText("Device ID: " + mService.getDeviceId());
            TextView availableFilesView = findViewById(R.id.availableFilesView);
            availableFilesView.setText("Available Files Count: " + mService.getFileListSize());
        } else {
            customLogger("Not bound!");
        }
    }

    private void startApp() {
        Intent intent = new Intent(this, MainBGService.class);
        startService(intent);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        final Button toggleRoamnetButton = findViewById(R.id.toggleRoamnet);
        toggleRoamnetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getRoamnetStatus()) {
                    enableRoamnet();
                } else {
                    disableRoamnet();
                }
                setUIText();
            }
        });
        Button logClearButton = findViewById(R.id.logClearButton);
        logClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView logView = findViewById(R.id.logView);
                setUIText();
                logView.setText("");
            }
        });
        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);
        BGServiceUIUpdateReceiver bgServiceUIUpdateReceiver =  new BGServiceUIUpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                bgServiceUIUpdateReceiver,
                statusIntentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(RoamNetApp.getContext());
        customLogger("OncReate");
        mEditor = mSharedPreferences.edit();
        if (!checkPermissions()) {
            getPermissions();
        } else {
            startApp();
        }
    }

    private void rebindBGService() {
        Intent intent = new Intent(this, MainBGService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

//    @Override
//    protected void onStart() {
//        customLogger("StartTime");
//        super.onStart();
//        rebindBGService();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unbindService(mConnection);
//        customLogger("Stop!");
//        mBound = false;
//    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MainBGService.LocalBinder binder = (MainBGService.LocalBinder) service;
            mService = binder.getService();
//            customLogger("Service conn!");
            mBound = true;
            customLogger("Started w/bound: " + mService.getStartTime());
            setUIText();
            mService.setBackgroundService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            customLogger("Service disconn!");
            mBound = false;
        }
    };


    private void customLogger(String msg) {
        Log.d(TAG, msg);
        TextView logView = findViewById(R.id.logView);
        String timeStamp = new SimpleDateFormat("kk.mm.ss.SS").format(new Date());
        logView.append(timeStamp+' '+msg+"\n");
    }


    private NotificationCompat.Builder buildNotification(Payload payload, boolean isIncoming) {
        long size = payload.asFile().getSize();
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this).setContentTitle((isIncoming ? "Receiving..." : "Sending...") + size).setSmallIcon(R.drawable.common_full_open_on_phone);
        boolean indeterminate = false;
        if (size == -1) {
            // This is a stream payload, so we don't know the size ahead of time.
            size = 100;
            indeterminate = true;
        }
        notification.setProgress((int)size, 0, indeterminate);
        return notification;
    }


    /*** Get the updates from Service to the UI ***/
    private class BGServiceUIUpdateReceiver extends BroadcastReceiver
    {
        private BGServiceUIUpdateReceiver() {}         // Prevents instantiation
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.CONNECTION_STATUS)){
                TextView textView = (TextView) findViewById(R.id.connectionStatusView);
                textView.setText(intent.getStringExtra(Constants.CONNECTION_STATUS));
            }
            if(intent.hasExtra(Constants.LOG_STATUS)){
                TextView logView = findViewById(R.id.logView);
                logView.append(intent.getStringExtra(Constants.LOG_STATUS));
            }
        }
    }
}

//    @Override
//    protected void onStart() {
//        super.onStart();
////        rebindBGService();
//     }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        setButtonText();
//        // populate content from the Service
//        if(mBound){
//            customLogger("Started at: " + mService.getStartTime());
//            TextView deviceIdView = findViewById(R.id.deviceIdView);
//            deviceIdView.setText("Device ID: " + mService.getDeviceId());
//
//            TextView availableFilesView = findViewById(R.id.availableFilesView);
//            availableFilesView.setText("Available Files Count: " + mService.getFileListSize());
//
//
//        } else {
//            Toast.makeText(getApplicationContext(), "Resume Without Bound", Toast.LENGTH_LONG).show();
////            rebindBGService();
//        }
//    }


