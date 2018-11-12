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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    VectorsService mService;
    boolean mBound = false;

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;

    final String TAG = "VectorsUI";
    private int lineCounter = 0;

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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            String[] requiredPermissionsArray = listPermissionsNeeded.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, requiredPermissionsArray, 0);
        }
    }

    boolean checkPermissions() {
        // User Email isn't essential, so it's okay if the user doesn't want to share it
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
                        Log.d(TAG, "We didn't get permissions");
                        break;
                    }
                }
            }
        }
    }

    private void enableVectors() {
        mEditor.putBoolean(Constants.STATUS_ENABLE_BG_SERVICE, true);
        mEditor.apply();
        mService.refreshServiceState();
    }

    private void disableVectors() {
        mEditor.putBoolean(Constants.STATUS_ENABLE_BG_SERVICE, false);
        mEditor.apply();
        mService.refreshServiceState();
    }

    private boolean getVectorsStatus() {
        return mSharedPreferences.getBoolean(Constants.STATUS_ENABLE_BG_SERVICE, true);
    }

    private void setUIText() {
        final Button toggleVectorsButton = findViewById(R.id.toggleVectors);
        if (mBound) {
            if (getVectorsStatus()) {
                toggleVectorsButton.setText(R.string.stop_button_text);
            } else {
                toggleVectorsButton.setText(R.string.start_button_text);
            }
            TextView deviceIdView = findViewById(R.id.deviceIdView);
            deviceIdView.setText("Device ID: " + mService.getDeviceId());
            TextView availableFilesView = findViewById(R.id.availableFilesView);
            availableFilesView.setText("Available Files Count: " + mService.getFileListSize());
        } else {
            customLogger("Not bound!");
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startApp() {
        Account[] accounts = AccountManager.get(VectorsApp.getContext()).getAccounts();
        String possibleEmail = null;
        if (accounts.length > 0) {
            for (Account acc : accounts) {
                Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
                if (emailPattern.matcher(acc.name).matches()) {
                    possibleEmail = acc.name;
                    break;
                }
            }
            if (BuildConfig.DEBUG) {
                customLogger(possibleEmail);
            }
            mEditor.putString(Constants.USER_EMAIL_ID, possibleEmail);
            mEditor.apply();
        } else {
            customLogger("Email denied");
        }
        Intent intent = new Intent(this, VectorsService.class);

        if (isMyServiceRunning(VectorsService.class)) {
            customLogger("Service already running");
        } else {
            customLogger("Starting service");
            startService(intent);
        }
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        final Button toggleVectorsButton = findViewById(R.id.toggleVectors);
        toggleVectorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getVectorsStatus()) {
                    if (BuildConfig.DEBUG) {
                    } else {
                        Toast.makeText(getApplicationContext(), "Thank you for participating! For best results, please keep your Bluetooth on.", Toast.LENGTH_LONG).show();
                    }
                    enableVectors();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("")
                            .setMessage("Are you sure you want to disable Vectors?")
                            .setIcon(android.R.drawable.stat_sys_warning)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    customLogger("Disabling Vectors");
                                    disableVectors();
                                    setUIText();
                                }})
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    customLogger("No action taken.");
                                }
                            }).show();
                    customLogger("exited");
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

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(VectorsApp.getContext());
        customLogger("On Create");

        mEditor = mSharedPreferences.edit();
        lastConnectStr = mSharedPreferences.getString("LastConnects", "EMPTY");
        if (!checkPermissions()) {
            getPermissions();
        } else {
            startApp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Unbinding service conn");
        unbindService(mConnection);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(VectorsApp.getContext());
        customLogger("On Create");

        mEditor.putString("LastConnects", lastConnectStr);
        mEditor.apply();

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            VectorsService.LocalBinder binder = (VectorsService.LocalBinder) service;
            mService = binder.getService();
//            customLogger("Service conn!");
            mBound = true;
            setUIText();
            mService.refreshServiceState();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            customLogger("Service disconn!");
            mBound = false;
        }
    };


    private void customLogger(String msg) {
        TextView logView = findViewById(R.id.logView);

        if (lineCounter > Constants.LOG_TEXT_VIEW_LINES) {
            logView.setText("");
            lineCounter = 0;
        }

        String timeStamp = new SimpleDateFormat("HH.mm.ss.SS").format(new Date());
        logView.append(timeStamp+' '+msg+"\n");
        lineCounter++;
    }

    /*** Get the updates from Service to the UI ***/
    String lastConnectStr;
    private class BGServiceUIUpdateReceiver extends BroadcastReceiver
    {
        private BGServiceUIUpdateReceiver() {}         // Prevents instantiation
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.CONNECTION_STATUS)){
                TextView textView = (TextView) findViewById(R.id.connectionStatusView);
                String timeStamp = new SimpleDateFormat("kk.mm.ss").format(new Date());
                String toLog = intent.getStringExtra(Constants.CONNECTION_STATUS) + " at " + timeStamp;
                if(toLog.contains(Constants.CONN_UP_LOG_STR)){
                    toLog = toLog + " ## " + lastConnectStr;
                    String oldLogs[] = toLog.split(" ## ", 4);
                    String newLogStr = oldLogs[0];
                    for (int i = 1; i < oldLogs.length; i++) {
                            newLogStr = newLogStr + " ## " + oldLogs[i];
                    }
                    toLog = newLogStr;
                    lastConnectStr = toLog;
                } else {
                    toLog = toLog + " ## " + lastConnectStr;
                }

                textView.setText(toLog);
            }
            if(intent.hasExtra(Constants.LOG_STATUS)){
                TextView logView = findViewById(R.id.logView);
                customLogger(intent.getStringExtra(Constants.LOG_STATUS));
            }
        }
    }
}
