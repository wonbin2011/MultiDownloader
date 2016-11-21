package com.wonbin.multidownloader.dao;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wonbin on 11/18/16.
 */

public class DownloadDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "database.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "info";
    private static final String DOWNLOAD_INFO_TABLE_CREAT_SQL = "CREATE TABLE "
            + TABLE_NAME + "("
            + "thid integer, "
            + "done integer, "
            + "path VARCHAR(1024), "
            + "PRIMARY KEY(path,thid)"
            + ");";

    public DownloadDBHelper(Context context) {
        this(context,DB_NAME,null,DB_VERSION);
    }

    public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DOWNLOAD_INFO_TABLE_CREAT_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
