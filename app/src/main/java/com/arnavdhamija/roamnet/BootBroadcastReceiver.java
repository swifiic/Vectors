package com.arnavdhamija.roamnet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.arnavdhamija.common.Constants;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // BOOT_COMPLETED” start Service
        if (intent.getAction().equals(Constants.ANDROID_BOOT_COMPLETION)) {
            //Service
            Log.d("bCast", "RoamnetBGService started");
            Intent serviceIntent = new Intent(context, MainBGService.class);
            context.startService(serviceIntent);
        }
    }
}
