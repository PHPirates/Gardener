package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class DisplayNotification extends BroadcastReceiver {
    /**
     * This activity is to display a notification only. It is called when
     * the system alarm goes off.
     */

    //don't forget to update the manifest!!
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            //get notification ID passed by MainActivity
            int id = intent.getExtras().getInt("id");

            //initialize strings (message is also used for notification text)

            String message = intent.getStringExtra("message");

            //pIntent to launch activity if user selects notification
            /** making a new Intent has to be done with (this, Reminder.class) instead
             * of ("com.garden.Reminder")... why? (otherwise the reminder text is not shown)
             */
            //(2)... and from here to receiver ...
            Intent reminderIntent = new Intent(context, AlarmReceiver.class);
            reminderIntent.putExtra("id", id); //alarm id
            //pass on strings again in the intent, message in both intents (we always need message)
            reminderIntent.putExtra("message", message);
            reminderIntent.putExtra("isButton", "main");
            //give intent unique id
            PendingIntent reminderPIntent = PendingIntent
                    .getActivity(context, id, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //intent for snooze button (action button)
            Intent actionIntent = new Intent(context, AlarmReceiver.class);
            actionIntent.putExtra("isButton", "action"); //("STRING_I_NEED", strName)
            //don't forget to pass the id also to the second intent
            actionIntent.putExtra("id", id);
            //just put the message here too because we need it anyway
            actionIntent.putExtra("message", message);
            PendingIntent actionPIntent = PendingIntent.getActivity(context,
                    (int) System.currentTimeMillis(), actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //define an action for the snooze action button
            NotificationCompat.Action action = new NotificationCompat.Action.Builder
                    (R.drawable.pixel, "Snooze me", actionPIntent)
                    .build();

            //create notification
            Notification notif = new NotificationCompat.Builder(context) //build the notification
                    .setContentTitle(context.getString(R.string.app_name)) //required
                    .setContentText(message) //required
                    .setSmallIcon(R.mipmap.ic_klaver) //required
                    .setContentIntent(reminderPIntent)
                            //associate pendingIntent with a gesture of NotificationCompat.Builder: click
                    .addAction(action)
                    .setAutoCancel(true) //to be dismissed in the Reminder activity
                    .setPriority(Notification.PRIORITY_MAX) //to show the action buttons by default
                    .build();

            NotificationManager nm = (NotificationManager) context
                    .getSystemService(android.content.Context.NOTIFICATION_SERVICE);

            nm.notify(id, notif); //(int id, Notification notification);

        } catch (Exception e) {
            Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
