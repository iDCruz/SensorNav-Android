package com.example.daale.researchproject_final;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by daale on 4/25/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "ResearchProj.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists beacons " +
                "(id integer primary key, uuid text, major int, min int, location text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists beacons");
        onCreate(db);
    }

    public boolean insertBeacon(String uuid, int major, int min, String location){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newBeacon = new ContentValues();

        newBeacon.put("uuid", uuid);
        newBeacon.put("major", major);
        newBeacon.put("min", min);
        newBeacon.put("location", location);
        db.insert("beacons", null, newBeacon);
        return true;
    }

    public ArrayList<Beacon> getBeacons(){ //might just give beacon uuid, max, and min here.
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery("select * from beacons", null);
        result.moveToFirst();
        ArrayList<Beacon> beacons = new ArrayList<>();

        for (int i = 0; i < result.getCount(); ++i){
            String uuid = result.getString(result.getColumnIndex("uuid"));
            int major = Integer.parseInt(result.getString(result.getColumnIndexOrThrow("major")));
            int min = Integer.parseInt(result.getString(result.getColumnIndex("min")));
            String location = result.getString(result.getColumnIndex("location"));

            Beacon beacon = new Beacon(uuid, major, min, location);
            beacons.add(beacon);

            result.moveToNext();
        }

        result.close();

        return beacons;
    }
}
