package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper {

    /**
     * Class to connect to the database. This class contains all the basic database functions
     * like add, delete, edit accessible by methods.
     */
    public static final String DATABASE_NAME = "Alarms.db";
    public static final String ALARMS_COLUMN_ID = "id";
    public static final String ALARMS_COLUMN_MESSAGE = "message";
    public static final String ALARMS_COLUMN_DATE = "date";
    public static final String ALARMS_COLUMN_INTERVAL = "interval";
    public static final String ALARMS_COLUMN_REPEAT = "repeat";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table alarmstable" + "(id integer primary key, " +
                        "message text, date integer, interval integer, repeat bit)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS alarmstable");
        onCreate(db);
    }

    public boolean insertAlarm(String message, long date, long interval, boolean repeat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("date", date);
        contentValues.put("interval", interval);
        contentValues.put("repeat", repeat);
        db.insert("alarmstable", null, contentValues);
        return true;
    }

    /**
     * select row by id
     * @param id id to search for
     * @return cursor pointed at that row
     */
    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from alarmstable where id=" + id + "", null);
    }

    public boolean updateAlarm(int id, String message, long date, long interval, boolean repeat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("date", date);
        if (interval != 0) {
            contentValues.put("interval", interval);
        }
        contentValues.put("repeat", repeat);
        db.update("alarmstable", contentValues, "id = ? ",
                new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteAlarm(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("alarmstable", "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<String> getAllAlarms() {
        ArrayList<String> arrayList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from alarmstable order by date", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            String listItem = res.getString(
                    res.getColumnIndex(ALARMS_COLUMN_MESSAGE));
            if ((listItem != null) && listItem.length() > 25) { //listItem can be empty?
                String cutString = listItem.substring(0, 25);
                listItem = cutString + "...";
            }
            arrayList.add(listItem);
            res.moveToNext();
        }

        res.close();
        return arrayList;
    }

    public ArrayList<Integer> getAllAlarmIDs() {
        /**
         * Return all ID's in an array, so in MainActivity we can use both this array and the
         * message array in order to give the listview the right id
         */
        ArrayList<Integer> arrayList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from alarmstable order by date", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            int listItem = res.getInt(
                    res.getColumnIndex(ALARMS_COLUMN_ID));
            arrayList.add(listItem);
            res.moveToNext();
        }

        res.close();
        return arrayList;
    }

    public ArrayList<String> getAllAlarmTimes() {
        ArrayList<String> arrayList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from alarmstable order by date", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            Long listItem = res.getLong(
                    res.getColumnIndex(ALARMS_COLUMN_DATE));
            arrayList.add(millisToText(listItem));
            res.moveToNext();
        }

        res.close();
        return arrayList;
    }

//    public ArrayList<Long> getAllIntervals(){
//        ArrayList<Long> arrayList = new ArrayList<>();
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res = db.rawQuery("select * from alarmstable order by date", null);
//        res.moveToFirst();
//
//        while (!res.isAfterLast()){
//            Long listItem = res.getLong(
//                    res.getColumnIndex(ALARMS_COLUMN_INTERVAL));
//            arrayList.add(listItem);
//            res.moveToNext();
//        }
//
//        res.close();
//        return arrayList;
//    }

//    public ArrayList<Boolean> getAllRepeat(){
//        ArrayList<Boolean> arrayList = new ArrayList<>();
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res = db.rawQuery("select * from alarmstable order by date", null);
//        res.moveToFirst();
//
//        while (!res.isAfterLast()){
//            Boolean listItem = res.getInt(res.getColumnIndex(ALARMS_COLUMN_REPEAT)) != 0;
//            arrayList.add(listItem);
//            res.moveToNext();
//        }
//
//        res.close();
//        return arrayList;
//    }

    public ArrayList<Long> getAllAlarmTimesInMillis() {
        ArrayList<Long> arrayList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from alarmstable order by date", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            Long listItem = res.getLong(
                    res.getColumnIndex(ALARMS_COLUMN_DATE));
            arrayList.add(listItem);
            res.moveToNext();
        }

        res.close();
        return arrayList;
    }


    public String millisToText(long m) {
        Date date = new Date(m);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
    }
}
