package com.abbyberkers.gardener;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlarmReceiver extends AppCompatActivity {
    /**
     * this is the activity that is shown after you click the notification
     */
    int idToUpdate = 0;
    DBHelper mydb = new DBHelper(this); //to connect to database
    //the textview that shows the string the notification passed
    TextView mainTextView;
    String reminderMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_receiver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /**this activity gets both strings from both intents, so that's why you need
         * to use different intents for the notification and action buttons,
         * so that when you click e.g. the action button the other string is empty.
         */

        //get id from notification
        idToUpdate = getIntent().getExtras().getInt("id");
        //Log.e("Gardener","oncreate"+Integer.toString(idToUpdate));
        //get message
        reminderMessage = getIntent().getStringExtra("message");
        //assign textview to the instance variable also used in choicePass
        mainTextView = (TextView) findViewById(R.id.messageText);

        //now the same for the action button from the notification
        // TextView snoozeTextView = (TextView) findViewById(R.id.snoozeMessageText);
        //(3).... and then get String and setText!
        //String snoozeText = getIntent().getStringExtra("snoozeMessage"); //getString("STRING_I_NEED");

        if (TextUtils.isEmpty(reminderMessage)) {
            //if the reminder string is empty it means the snooze button was pressed
            //display choice dialog
            showChoiceDialog(findViewById(R.id.snoozeButton));
            mainTextView.setText(getResources().getString(
                    R.string.snoozed_text)); //funny note: R.string.app_name is equal to getTimeByID
        } else {
            mainTextView.setText(String.format(getResources().getString(
                    R.string.dontforget), reminderMessage, millisToText(getTimeByID())));
        }

        //This will remove the notification if the action button is pressed
        // instead of the notification, but only when the activity is displayed
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(getIntent().getExtras().getInt("id"));

    }

    public long getTimeByID() {
        /**
         * gets the time corresponding to the alarm currently shown
         */
        List<Long> timesList = mydb.getAllAlarmTimesInMillis();
        ArrayList<Integer> arrayListID = mydb.getAllAlarmIDs();
        int timeIndex = arrayListID.indexOf(idToUpdate);
        return timesList.get(timeIndex);
    }


    public void showChoiceDialog(View view) {
        FragmentManager fm = getFragmentManager();
        SnoozeChoiceFragment snoozeChoiceFragment = new SnoozeChoiceFragment();
        snoozeChoiceFragment.show(fm, "Snooze me for...");
    }

    public void choicePass(long choice) {
        /**this is the extra snooze time passed by SnoozeChoiceFragment
         * very similar to addAlarm from ShowAlarm activity
         * saves alarm into database (with the text shown) and sets alarm
         */

        //get time from database and add choice of delay to it
        choice += getTimeByID();

        //if update succeeded
        if (mydb.updateAlarm(idToUpdate, reminderMessage,
                choice)) {
            Toast.makeText(getApplicationContext(), "Alarm is snoozed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "not Updated", Toast.LENGTH_SHORT).show();
        }


        //set alarm using alarmmanager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //intent to DisplayNotification
        //Intent intent = new Intent("com.abbyberkers.DisplayNotification");
        Intent intent = new Intent(this, DisplayNotification.class);
        //add the message and id
        intent.putExtra("id", idToUpdate);
        intent.putExtra("message", reminderMessage);
        intent.putExtra("snoozeMessage", R.string.snooze_message);
        //intent.setAction("foo"); //dummy action?

        /** set the flags so the mainactivity isn't started when the notification is triggered
         * use new task to put the displaynotif in a new task, and multiple task so it doesn't
         * interfere with the main task. This way, the backstack of that main task isn't desturbed(?)
         * and when you hit the back button when in reminder activity you go back to your last page(?)
         */
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        PendingIntent displayIntent = PendingIntent.getBroadcast(
                this, idToUpdate, //assign a unique id, using the database id
                intent, PendingIntent.FLAG_ONE_SHOT); //instead of FLAG_UPDATE_CURRENT TODO what why


        cancelAlarmIfExists(this, idToUpdate, intent);

        //finally, set alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                choice, displayIntent);

        //update textview
        if (reminderMessage==null) { //if coming via snooze action button, reminderMessage was empty
            reminderMessage = getIntent().getStringExtra("alarmMessage"); //the message passed with snooze intent
        }
        this.mainTextView.setText(String.format(getResources().getString(
                    R.string.dontforget), reminderMessage, millisToText(getTimeByID())));


//            //after alarm added, to back to main
//            Intent mainIntent = new Intent(getApplicationContext(),
//                    MainActivity.class);
//            startActivity(mainIntent);

    }

    public void cancelAlarmIfExists(Context mContext, int id, Intent intent) {
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, 0);
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String millisToText(long m) {
        Date date = new Date(m);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            //on back key go to main
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keycode, event);
    }

}
