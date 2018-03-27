package in.swifiic.shmbridge;

import android.app.Application;
import android.content.Context;

/**
 * Created by abhishek on 27/3/18.
 */

public class BridgeApp extends Application {
    private static BridgeApp instance;

    public static BridgeApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}