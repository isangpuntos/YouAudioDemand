package com.isang.puntos.youaudiodemand;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class HandlerDatabase extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "playListManager";
    private static final String TABLE_PLAYLIST_LIST = "listOfPlaylist";
    private static final String TABLE_PLAYLIST = "playlist";
    private static final String KEY_PLAYLIST = "playlist";
    private static final String KEY_ID = "id";
    private static final String KEY_MUSIC = "music";
    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_DURATION = "duration";
    private Context appContext = null;

    public HandlerDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        appContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYLIST_TABLE = "CREATE TABLE " + TABLE_PLAYLIST + "("
                + KEY_PLAYLIST + " TEXT,"
                + KEY_MUSIC + " TEXT,"
                + KEY_ID + "  TEXT,"
                + KEY_THUMBNAIL + " TEXT,"
                + KEY_DURATION + " TEXT"  + ")";

        String CREATE_PLAYLIST_LIST_TABLE = "CREATE TABLE " + TABLE_PLAYLIST_LIST + "("
                + KEY_PLAYLIST + " TEXT)";

        db.execSQL(CREATE_PLAYLIST_TABLE);
        db.execSQL(CREATE_PLAYLIST_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_LIST);
        onCreate(db);
    }

    public List<String> getListOfPlayList(boolean isPlaylist)
    {
        List<String> contactList = new ArrayList<String>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYLIST_LIST;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                contactList.add(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        for(int i = 0; i < contactList.size() && isPlaylist; i++)
        {
            contactList.set(i, contactList.get(i) + "ß" + getAudioCount(contactList.get(i)));
        }

        return contactList;
    }

    public List<HandlerSongInformation> getPlayList(String playListName)
    {
        List<HandlerSongInformation> playList = new ArrayList<HandlerSongInformation>();
        List<HandlerSongInformation> notExists = new ArrayList<HandlerSongInformation>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYLIST + " WHERE playlist = '" + playListName + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                HandlerSongInformation handlerSongInformation = new HandlerSongInformation();
                handlerSongInformation.songTitle = cursor.getString(1);
                handlerSongInformation.id = cursor.getString(2);
                handlerSongInformation.thumbNail = cursor.getString(3);
                handlerSongInformation.duration = cursor.getString(4);
                if(!(new File(appContext.getFilesDir().toString() + "/" + handlerSongInformation.songTitle + ".mp3").exists()))
                    notExists.add(handlerSongInformation);
                else
                    playList.add(handlerSongInformation);
            } while (cursor.moveToNext());
        }

        for(HandlerSongInformation handlerSongInformation: notExists)
        {
            db = this.getWritableDatabase();
            db.delete(TABLE_PLAYLIST, KEY_MUSIC + " = ?",
                    new String[] { handlerSongInformation.songTitle });
            db.close();
        }
        return playList;
    }

    public String getAudioCount(String playlist)
    {
        String countQuery = "SELECT  * FROM " + TABLE_PLAYLIST + " WHERE playlist = '" + playlist + "'";
        getPlayList(playlist);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return Integer.toString(cnt);
    }

    public void addPlayList(String playList)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST, playList);
        db.insert(TABLE_PLAYLIST_LIST, null, values);
        db.close();
    }

    public void addMusic(String playList, String music, String id, String thumbNail, String duration)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST, playList);
        values.put(KEY_MUSIC, music);
        values.put(KEY_ID, id);
        values.put(KEY_THUMBNAIL, thumbNail);
        values.put(KEY_DURATION, duration);
        db.insert(TABLE_PLAYLIST, null, values);
        db.close();
    }

    public void deletePlaylist(String playlist)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST_LIST, KEY_PLAYLIST + " = ?",
                new String[] { playlist });
        db.close();
        db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST, KEY_PLAYLIST + " = ?",
                new String[] { playlist });
        db.close();
    }
}
