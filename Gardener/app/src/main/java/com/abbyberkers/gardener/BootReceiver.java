package com.abbyberkers.gardener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BootReceiver extends BroadcastReceiver {
    /**
     * When boot is received, reset all alarms
     */
    @Override
    public void onReceive(Context context, Intent i) {
        //make an object of the DBHelper class so we can use its methods
        DBHelper myDB = new DBHelper(context);
        SQLiteDatabase db = myDB.getReadableDatabase();
        Cursor res = db.rawQuery("select * from alarmstable order by date", null);
        res.moveToFirst();

        while(!res.isAfterLast()) {
            //get everything out on this line
            int id = res.getInt(res.getColumnIndex(DBHelper.ALARMS_COLUMN_ID));
            String message = res.getString(res.getColumnIndex(DBHelper.ALARMS_COLUMN_MESSAGE));
            long time = res.getLong(res.getColumnIndex(DBHelper.ALARMS_COLUMN_DATE));
            long interval = res.getLong(res.getColumnIndex(DBHelper.ALARMS_COLUMN_INTERVAL));
            boolean repeat = res.getInt(res.getColumnIndex(DBHelper.ALARMS_COLUMN_REPEAT)) != 0;

            //only set alarm when in the future, except for repeating alarms
            if (time > System.currentTimeMillis() || repeat) {
                //if repeating, change time to set alarm to, to the next
                //time instance in the future to avoid the alarm popping up immediately
                if (repeat) {
                    while (time < System.currentTimeMillis()) {
                        time += interval;
                    }
                }

                //set alarm using alarmmanager
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                //intent to DisplayNotification
                Intent intent = new Intent(context, DisplayNotification.class);
                //add the message and id
                intent.putExtra("id", id);
                intent.putExtra("message", message);
                intent.putExtra("snoozeMessage", R.string.snooze_message);
                //set flags
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                PendingIntent displayIntent = PendingIntent.getBroadcast(
                        //assign a unique id, using the database id
                        context, id,
                        //instead of FLAG_UPDATE_CURRENT:
                        intent, PendingIntent.FLAG_CANCEL_CURRENT);

                if (repeat) {
                    //finally, set repeating alarm
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                            time, interval, displayIntent);
                } else {
                    //finally, set alarm
                    alarmManager.set(AlarmManager.RTC_WAKEUP,
                            time, displayIntent);
                }
            }

            //move to next item
            res.moveToNext();
        }
        res.close();
    }
}
