package com.abbyberkers.gardener;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
        String isButton = getIntent().getStringExtra("isButton");
        //assign textview to the instance variable also used in choicePass
        mainTextView = (TextView) findViewById(R.id.messageText);


        //if action (snooze) button is pressed on notification
        if (isButton.equals("action")) {
            //then display the snooze dialog
            showChoiceDialog(findViewById(R.id.snoozeButton));
        }

        if (TextUtils.isEmpty(reminderMessage)) { //if the message is empty, show default text
            //funny note: R.string.app_name is equal to getTimeByID
            mainTextView.setText(getResources().getString(
                    R.string.snoozed_text));
        } else {
            //set textview with message and time
            mainTextView.setText(String.format(getResources().getString(
                            R.string.dontforget),
                    reminderMessage, millisToText(getTimeByID())));
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

        //give the message to the fragment to show the new times in the list
        Bundle bundle = new Bundle();
        snoozeChoiceFragment.setArguments(bundle);
        snoozeChoiceFragment.show(fm, "Snooze me for...");
    }

    public void choicePass(long choice) {
        /**this is the extra snooze time passed by SnoozeChoiceFragment
         * very similar to addAlarm from ShowAlarm activity
         * saves alarm into database (with the text shown) and sets alarm
         */

        //get current time instead of time from database,
        // because snoozing should be from current time
        // and add choice of delay to it
        choice += System.currentTimeMillis();

        Cursor rs = mydb.getData(idToUpdate);
        rs.moveToFirst();
        boolean checkRepeat = rs.getInt(
                rs.getColumnIndex(DBHelper.ALARMS_COLUMN_REPEAT)) != 0; //get date
        long interval = rs.getLong(rs.getColumnIndex(DBHelper.ALARMS_COLUMN_INTERVAL));
        if (!rs.isClosed()) {
            rs.close();
        }

        //if update succeeded
        if (mydb.updateAlarm(idToUpdate, reminderMessage,
                choice, interval, checkRepeat)) {
            Toast.makeText(getApplicationContext(), "Alarm is snoozed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "not Updated", Toast.LENGTH_SHORT).show();
        }

        //set alarm using alarmmanager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //intent to DisplayNotification
        Intent intent = new Intent(this, DisplayNotification.class);
        //add the message and id
        intent.putExtra("id", idToUpdate);
        intent.putExtra("message", reminderMessage);
        intent.putExtra("snoozeMessage", R.string.snooze_message);

        /** set the flags so the mainactivity isn't started when the notification is
         * triggered use new task to put the displaynotif in a new task, and multiple task
         * so it doesn't interfere with the main task. This way, the backstack of that
         * main task isn't disturbed(?) and when you hit the back button when in
         * reminder activity you go back to your last page(?)
         */
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        PendingIntent displayIntent = PendingIntent.getBroadcast(
                this, idToUpdate, //assign a unique id, using the database id
                intent, PendingIntent.FLAG_CANCEL_CURRENT); //instead of FLAG_UPDATE_CURRENT

        if (checkRepeat) {
            //finally, set repeating alarm
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    choice, interval, displayIntent);
        } else {
            //finally, set alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    choice, displayIntent);
        }

        //update textview
        this.mainTextView.setText(String.format(getResources().getString(
                        R.string.dontforget),
                reminderMessage, millisToText(getTimeByID())));
    }

    public String millisToText(long m) {
        Date date = new Date(m);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            //on back key go to main
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        return super.onKeyDown(keycode, event);
    }

}
