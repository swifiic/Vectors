package com.arnavdhamija.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nic on 23/2/18.
 */

public class DatabaseModule {// extends SQLiteOpenHelper {
//    private static final int DATABASE_VERSION = 1;
//    private static final String DATABASE_NAME = "roamnet.db";
//    public static final String TABLE_VIDEODATA = "videodata";
//
//    public static final String COLUMN_VIDEONAME = "video_name";
//    public static final String COLUMN_SEQUENCENUMBER = "sequence_number";
//    public static final String COLUMN_TICKETS = "tickets";
//    public static final String COLUMN_RESOLUTION = "resolution";
//    public static final String COLUMN_FRAMERATE = "framerate";
//
//    public DatabaseModule(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String query = "create table " + TABLE_VIDEODATA + " (" +
//                        COLUMN_VIDEONAME + " TEXT PRIMARY KEY " +
//                        COLUMN_SEQUENCENUMBER + " INTEGER NOT NULL " +
//                        COLUMN_TICKETS + " INTEGER NOT NULL " +
//                        COLUMN_RESOLUTION  + " INTEGER NOT NULL " +
//                        COLUMN_FRAMERATE + " INTEGER NOT NULL " +
//                        ")";
//        db.execSQL(query);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("drop table if exists " + TABLE_VIDEODATA);
//        onCreate(db);
//    }
//
//    public void addVideoData(VideoData videoData) {
//        deleteVideoData(videoData.getFileName());
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_VIDEONAME, videoData.getFileName());
//        values.put(COLUMN_SEQUENCENUMBER, videoData.getSequenceNumber());
//        values.put(COLUMN_TICKETS, videoData.getTickets());
//        values.put(COLUMN_RESOLUTION, videoData.getResolution());
//        values.put(COLUMN_FRAMERATE, videoData.getFrameRate());
//
//        SQLiteDatabase db = getWritableDatabase();
//        db.insert(DATABASE_NAME, null, values);
//        db.close();
//    }
//
//    public VideoData getVideoData(String fileName) {
//        VideoData videoData = new VideoData();
//        Cursor res = getReadableDatabase().rawQuery("select * from " + videoData + " where " + COLUMN_VIDEONAME + " = ?", new String[]{fileName});
//        if (res.moveToFirst()) {
//            videoData.setFileName(res.getString(res.getColumnIndex(COLUMN_VIDEONAME)));
//            videoData.setFrameRate(res.getInt(res.getColumnIndex(COLUMN_FRAMERATE)));
//            videoData.setResolution(res.getInt(res.getColumnIndex(COLUMN_RESOLUTION)));
//            videoData.setSequenceNumber(res.getInt(res.getColumnIndex(COLUMN_SEQUENCENUMBER)));
//            videoData.setTickets(res.getInt(res.getInt(res.getColumnIndex(COLUMN_TICKETS))));
//        }
//        res.close();
//        videoData.logVideoData();
//        return videoData;
//    }
//
//    public void deleteVideoData(String fileName) {
//        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("delete from " + TABLE_VIDEODATA + " where " + COLUMN_VIDEONAME + "=\"" + fileName + "\"");
//    }
//
//    public String databaseToString() {
//        String dbString = "";
//        SQLiteDatabase db = getWritableDatabase();
//        String query = "select * from " + TABLE_VIDEODATA + " where 1";
//        return dbString;
//    }
}
