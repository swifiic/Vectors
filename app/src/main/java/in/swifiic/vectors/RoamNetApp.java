package in.swifiic.vectors;

import android.app.Application;
import android.content.Context;

/**
 * Created by abhishek on 20/3/18.
 */


public class RoamNetApp extends Application {
    private static RoamNetApp instance;

    public static RoamNetApp getInstance() {
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