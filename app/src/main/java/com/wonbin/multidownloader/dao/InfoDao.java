package com.wonbin.multidownloader.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.wonbin.multidownloader.javabean.Info;
/**
 * Created by wonbin on 11/18/16.
 */

public class InfoDao {

    private static final String TABLE_NAME = "info";

    private DownloadDBHelper dbHelper;
    private SQLiteDatabase database;

    public InfoDao(Context context) {
        dbHelper = new DownloadDBHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void insert(Info info) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = info2ContentValues(info);
        database.insert(TABLE_NAME, null, contentValues);
    }
    public void delete(String path,int thid) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(TABLE_NAME,"path=? AND thid=?",new String[] {path,String.valueOf(thid)});
    }
    public void update(Info info) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("done",info.getDone());
        database.updateWithOnConflict(TABLE_NAME,values,"path=? AND thid=?",new String[]{info.getPath()
                ,String.valueOf(info.getThid())},SQLiteDatabase.CONFLICT_NONE);
    }
    public Info query(String path,int thid){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(TABLE_NAME,new String[] {"path","thid","done"},"path=? AND thid=?",new String[]{path,
                String.valueOf(thid)},null,null,null);
        Info info = null;
        if(cursor.moveToNext()) {
            info = new Info(cursor.getInt(0),cursor.getInt(1),cursor.getString(2));
        }
        cursor.close();
        return info;
    }
    public int queryDownload(String path) {
          Cursor cursor = queryAll(path,dbHelper.getWritableDatabase());
        if(cursor.moveToNext()) {
            int result = cursor.getInt(0);
            return result;
        } else {
            return 0;
        }
    }
    public Cursor queryAll(String path,SQLiteDatabase database) {
        Cursor cursor = database.query(TABLE_NAME,new String[]{"SUM(done)"},"path=?",new String[]{path},
                null,null,null);
        return cursor;
    }
    public void deleteAll(String path,int len){
        Cursor cursor = queryAll(path,database);
        if(cursor.moveToNext()) {
            int result = cursor.getInt(0);
            if(result == len) {
                database.delete(TABLE_NAME,"path=?",new String[]{path});
            }
        }

    }
    public List<String> queryUndone(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(true,TABLE_NAME,new String[]{"path"},null,null,null,null,null,null);
        List<String> pathList = new ArrayList<>();

        while (cursor.moveToNext()) {
            pathList.add(cursor.getString(0));
        }
        cursor.close();

        return  pathList;
    }

    private ContentValues info2ContentValues(Info info) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("thid",info.getThid());
        contentValues.put("done",info.getDone());
        contentValues.put("path",info.getPath());
        return contentValues;
    }
}
