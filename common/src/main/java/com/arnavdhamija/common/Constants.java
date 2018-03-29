package com.arnavdhamija.common;

/**
 * Created by abhishek on 19/3/18.
 */

public final class Constants {
    // Roamnet Constants
    public static final String BROADCAST_ACTION =
            "swifiic.roamnet.android.BROADCAST";
    public static final String LOG_STATUS =
            "swifiic.roamnet.android.LOG_STATUS";
    public static final String CONNECTION_STATUS =
            "swifiic.roamnet.android.CONNECTION_STATUS";
    public static final String STATUS_ENABLE_BG_SERVICE =
            "connectionStatus";
//    public static final String ACK_FILENAME =
//            "ack";
    public static final String APP_KEY =
            "RoamnetSharedPrefs";
    public static final String DEVICE_ID =
            "DEVICE_ID";
    public static final String ANDROID_BOOT_COMPLETION =
            "android.intent.action.BOOT_COMPLETED";
    public static final int MIN_CONNECTION_GAP_TIME = 60;
    // Bridge Constants
    public static final String BURST_COUNT = "BURST_COUNT";
    public static final String FLDR =  "/RoamnetData";
    public static final String ACK_PREFIX = "ack_";
    public static final String BASE_NAME =  "video_00";
    public static final String ACK_FILENAME = ACK_PREFIX + BASE_NAME;

    public static final int[]  fileSizeArrayL0 = {5, 3, 3, 4, 3, 3, 4};
    public static final int[]  fileSizeArrayL1 = {21, 13, 13, 24, 28, 25, 28};

    // indexing for temporal starts from 1
    public static final int[] CopyCountL0 = {32, 32, 16, 16, 8, 8, 0, 0};
    public static final int[] CopyCountL1 = {6, 6, 6, 6, 6, 6, 0, 0};
    public static final int[] CopyCountL3 = {4, 4, 4, 4, 4, 4, 0, 0};
    public static final int[] CopyCountL4 = {3, 3, 3, 3, 3, 3, 0, 0};
}
