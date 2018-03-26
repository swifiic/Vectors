package com.arnavdhamija.roamnet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // BOOT_COMPLETED” start Service
        if (intent.getAction().equals(Constants.ANDROID_BOOT_COMPLETION)) {
            //Service
            Intent serviceIntent = new Intent(context, MainBGService.class);
            context.startService(serviceIntent);
        }
    }
}
