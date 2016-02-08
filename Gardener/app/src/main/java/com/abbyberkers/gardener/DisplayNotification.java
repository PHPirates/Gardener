package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

public class DisplayNotification extends BroadcastReceiver {
    /**
     * This activity is to display a notification only. It is called when
     * the system alarm goes off.
     *
     */

    //don't forget to update the manifest!!
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            //get notification ID passed by MainActivity
            int id = intent.getExtras().getInt("id");
            //----Oh of course, have to pass on the strings again.....-----
            //initialize strings (reminder is also used for notification text)
            String message = intent.getStringExtra("message");
            String snoozeMessage = intent.getStringExtra("snoozeMessage");


            //pIntent to launch activity if user selects notification
            /** making a new Intent has to be done with (this, Reminder.class) instead
             * of ("com.garden.Reminder")... why? (otherwise the reminder text is not shown)
             */
            Intent reminderIntent = new Intent(context, AlarmReceiver.class); //(2)... and from here to receiver ...
            reminderIntent.putExtra("id", id);
            //pass on strings again in the intent
            reminderIntent.putExtra("message", message);
            //intent.putExtra("notifyAction",snoozeMessage); //---> in different intent


            PendingIntent reminderPIntent = PendingIntent
                    .getActivity(context, id, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT); //give intent unique id

            //intent for snooze button (action button)
            Intent actionIntent = new Intent(context, AlarmReceiver.class);
            actionIntent.putExtra("snoozeMessage", snoozeMessage); //("STRING_I_NEED", strName)
            //don't forget to pass the id also to the second intent
            actionIntent.putExtra("id", id);
            actionIntent.putExtra("alarmMessage",message); //just put the message here too because we need it anyway
            PendingIntent actionPIntent = PendingIntent.getActivity(context,
                    (int) System.currentTimeMillis(), actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            //create notification
            Notification notif = new Notification.Builder(context) //build the notification
                    .setContentTitle(context.getString(R.string.app_name)) //required
                    .setContentText(message) //required
                    .setSmallIcon(R.mipmap.ic_klaver) //required
                    .setContentIntent(reminderPIntent)
                            //associate pendingIntent with a gesture of NotificationCompat.Builder: click
                    .addAction(R.drawable.pixel, "Snooze me", actionPIntent)
                            //should be addAction(NotificationCompat.Action action)
                    .setAutoCancel(true) //to be dismissed in the Reminder activity
                    .setPriority(Notification.PRIORITY_MAX) //to show the action buttons by default
                            // .setVibrate(new long[] {200, 600, 200, 600})
                    .build();

            NotificationManager nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

            nm.notify(id, notif); //(int id, Notification notification);

            //finish(); //because this doesn't have a GUI we don't need it anymore
        } catch (Exception e) {
            Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
