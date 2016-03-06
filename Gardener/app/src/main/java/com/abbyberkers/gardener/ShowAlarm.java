package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ShowAlarm extends AppCompatActivity {

    /**
     * Class to add a new alarm or edit an existing alarm. Contains buttons to set time and date,
     * save the alarm (and send it to the phone) and a cancel (add) or delete (edit) button.
     */
    private DBHelper mydb;
    int idToUpdate = 0;
    EditText message;

    int year, month, day, hour, minute;
    //some instance variables, handy to pass things around
    FragmentManager fm = getSupportFragmentManager();

    String currentDateTimeString; //instance to pass to confirmfrag

    int Value; //id is global, set in oncreate

    long interval = 0; //interval is set in SnoozeChoiceFragment or in oncreate
    boolean repeat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set initial instance variables values for printing
        Calendar c;
        c = Calendar.getInstance(); //get current time
        this.year = c.get(Calendar.YEAR);
        this.month = c.get(Calendar.MONTH);
        this.day = c.get(Calendar.DAY_OF_MONTH);
        this.hour = c.get(Calendar.HOUR_OF_DAY);
        this.minute = c.get(Calendar.MINUTE);

        message = (EditText) findViewById(R.id.messageText);

        mydb = new DBHelper(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) { //if there's something in the bundle

            Value = extras.getInt("id"); //get the id to search

            if (Value > 0) {
                //then we want to change an alarm and not add one

                //we're in edit mode, so change instance variables to database time
                long time = getTimeDatabase();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time);
                this.year = cal.get(Calendar.YEAR);
                this.month = cal.get(Calendar.MONTH);
                this.day = cal.get(Calendar.DAY_OF_MONTH);
                this.hour = cal.get(Calendar.HOUR_OF_DAY);
                this.minute = cal.get(Calendar.MINUTE);

                /**
                 * get stuff from database
                 */
                Cursor rs = mydb.getData(Value);
                idToUpdate = Value;
                rs.moveToFirst();

                String messag = rs.getString(
                        rs.getColumnIndex(DBHelper.ALARMS_COLUMN_MESSAGE)); //get message from database
                long dataDate = rs.getLong(
                        rs.getColumnIndex(DBHelper.ALARMS_COLUMN_DATE)); //get date
                interval = rs.getLong(
                        rs.getColumnIndex(DBHelper.ALARMS_COLUMN_INTERVAL));
                repeat = rs.getInt(rs.getColumnIndex(DBHelper.ALARMS_COLUMN_REPEAT)) != 0;

                if (!rs.isClosed()) {
                    rs.close();
                } //close cursor

                message.setText(messag); //set database text to edittext
                //set cursor initially to the right
                message.setSelection(message.length());

                //convert long to string
                Date date = new Date(dataDate);
                //format the date to a string using the local date time formatting
                //change instance variable for the confirmFragment to use
                currentDateTimeString = DateFormat.getDateTimeInstance().format(date);

                /**
                 * Set text on buttons
                 */
                Button dateButton = (Button) findViewById(R.id.setDate);
                Button timeButton = (Button) findViewById(R.id.setTime);
                Button intervalButton = (Button) findViewById(R.id.intervalButton);

                if (repeat) {
                    //set database interval on right button if editing a repeating alarm
                    intervalButton.setText(intervalToText(interval));
                }

                Date dateOnly = new Date(dataDate);
                String dateString = DateFormat.getDateInstance().format(dateOnly);
                dateButton.setText(dateString);

                Date timeOnly = new Date(dataDate);
                String timeString = DateFormat.getTimeInstance().format(timeOnly);
                timeButton.setText(timeString);


                //if you're changing something, set a clicklistener for delete
                Button deleteButton = (Button) findViewById(R.id.cancelButton);
                deleteButton.setText(R.string.delete);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mydb.deleteAlarm(idToUpdate);
                        //cancel alarm as well
                        cancelAlarmIfExists();

                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(mainIntent);
                    }
                });

            } else {

                //if you're adding something, set a clicklistener that cancels
                Button cancelButton = (Button) findViewById(R.id.cancelButton);
                cancelButton.setText(R.string.cancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                });

                printTime(); // print time on buttons
            }
        }
    }

    public void showDateDialog(View view) {
        //date to be sent to fragment
        DateFragment dateFragment = new DateFragment();
        Bundle bundle = new Bundle(); //bundle to be sent

        Bundle extras = getIntent().getExtras();
        //get time from database to be default time if editing, current time if adding
        if (extras != null) { //if there's something in the bundle
            //int Value = extras.getInt("id"); //get the id to search

            if (Value > 0) {
                //editing, so get time from database
                long dataDate = getTimeDatabase();
                //convert long to ints
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(dataDate);
                bundle.putInt("year", c.get(Calendar.YEAR));
                bundle.putInt("month", c.get(Calendar.MONTH));
                bundle.putInt("day", c.get(Calendar.DAY_OF_MONTH));

            } else {
                //adding an alarm, current time is default
                bundle.putInt("year", year);
                bundle.putInt("month", month);
                bundle.putInt("day", day);
            }
        }


        dateFragment.setArguments(bundle);

        dateFragment.show(fm, "Dialog Fragment");
    }

    public void showTimeDialog(View view) {
        TimeFragment timeFragment = new TimeFragment();
        Bundle bundle = new Bundle();

        Bundle extras = getIntent().getExtras();
        //get time from database to be default time if editing, current time if adding
        if (extras != null) { //if there's something in the bundle
            //int Value = extras.getInt("id"); //get the id to search

            if (Value > 0) {
                //editing, so get time from database
                long dataDate = getTimeDatabase();
                //convert long to ints
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(dataDate);
                bundle.putInt("hour", c.get(Calendar.HOUR_OF_DAY));
                bundle.putInt("minute", c.get(Calendar.MINUTE));

            } else {
                //adding an alarm, current time is default
                bundle.putInt("hour", hour);
                bundle.putInt("minute", minute);
            }
        }

        timeFragment.setArguments(bundle);

        timeFragment.show(fm, "Dialog Fragment");


    }

    public long getTimeDatabase() {
        //Bundle extras = getIntent().getExtras();
        //int Value = extras.getInt("id"); //get the id to search
        Cursor rs = mydb.getData(Value);
        rs.moveToFirst();
        long dataDate = rs.getLong(
                rs.getColumnIndex(DBHelper.ALARMS_COLUMN_DATE)); //get date
        if (!rs.isClosed()) {
            rs.close();
        } //close cursor
        return dataDate;
    }

    public void datePass(int y, int m, int d) {
        //date passed by fragment
        this.year = y;
        this.month = m;
        this.day = d;
        //get the others from the database, otherwise the other button gets the current time/date
        //but only when changing something...
        if (Value > 0) {
            long dataDate = getTimeDatabase();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dataDate);
            this.hour = c.get(Calendar.HOUR_OF_DAY);
            this.minute = c.get(Calendar.MINUTE);
        }
        printTime(); //update button
    }

    public void timePass(int h, int m) {
        this.hour = h;
        this.minute = m;
        if (Value > 0) {
            long dataDate = getTimeDatabase();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dataDate);
            this.year = c.get(Calendar.YEAR);
            this.month = c.get(Calendar.MONTH);
            this.day = c.get(Calendar.DAY_OF_MONTH);
        }
        printTime();
    }

    public void intervalPass(long interval) {
        this.interval = interval;
        repeat = interval != 0; //if interval 0, it's not repeating, otherwise it is
        Button intervalButton = (Button) findViewById(R.id.intervalButton);
        intervalButton.setText(intervalToText(interval));
    }

    public String millisToText(long m) {
        Date date = new Date(m);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
    }

    public String intervalToText(long interval) {
        /**
         * uses that interval is always either days, hours or minutes
         * returns the text of the last of days, hours and minutes which is not zero.
         */

        if (interval ==0 ) { //if alarm is not repeating
            return "Not repeating";
        }


        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = interval / daysInMilli;
        interval = interval % daysInMilli;

        if (interval == 0) { //if there are elapsedDays
            String temp = "every ";
            if (elapsedDays != 1) { //if multiple days, add number of them
                temp += elapsedDays + " days";
                return temp;
            }
            temp += "day";
            return temp;
        }

        long elapsedHours = interval / hoursInMilli;
        interval = interval % hoursInMilli;

        if (interval == 0) {
            String temp = "every ";
            if (elapsedHours != 1) {
                temp += elapsedHours + " hours";
                return temp;
            }
            temp += "hour";
            return temp;
        }

        long elapsedMinutes = interval / minutesInMilli;
        interval = interval % minutesInMilli;

        if (interval == 0) {
            String temp = "every ";
            if (elapsedMinutes != 1) {
                temp += elapsedMinutes + " minutes";
                return temp;
            }
            temp += "minute";
            return temp;
        }


        return "no interval?";

    }

    public void printTime() {
        /**
         * Print time and date on corresponding buttons
         */
        Calendar c = Calendar.getInstance();

        //sets time for alarm
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        //show text on buttons instead of in a texview
        //make a date object using the time we just set to the calendar instance
        Date date = new Date(c.getTimeInMillis());
        //format the date to a string using the local date time formatting
        //change instance variable for the confirmFragment to use

        currentDateTimeString = DateFormat.getDateTimeInstance().format(date);

        Button dateButton = (Button) findViewById(R.id.setDate);
        Button timeButton = (Button) findViewById(R.id.setTime);

        Date dateOnly = new Date(c.getTimeInMillis());
        String dateString = DateFormat.getDateInstance().format(dateOnly);
        dateButton.setText(dateString);

        Date timeOnly = new Date(c.getTimeInMillis());
        String timeString = DateFormat.getTimeInstance().format(timeOnly);
        timeButton.setText(timeString);
    }

    public long timeToInt() {
        Calendar c = Calendar.getInstance();


        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        return c.getTimeInMillis();
    }

    public void addAlarm() {
        /**
         * when save button clicked
         *  also sets alarm
         */

        String message = this.message.getText().toString();
        message = message.trim();

        if (message.equals("")) {
            Toast.makeText(this, "Please fill in text field.", Toast.LENGTH_SHORT).show();
        } else {

            Bundle extras = getIntent().getExtras();
            long time = timeToInt(); //get chosen (or otherwise current) time in millis
            //Toast.makeText(getApplicationContext(),millisToText(time),Toast.LENGTH_SHORT).show();
            if (extras != null) {
                //int Value = extras.getInt("id"); //use global value set in onCreate
                if (Value > 0) {
                    //if update succeeded
                    if (mydb.updateAlarm(idToUpdate, message,
                            time, interval, repeat)) {
                        Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "not Updated", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mydb.insertAlarm(message,
                            time, interval, repeat)) {
                        Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "not done", Toast.LENGTH_SHORT).show();
                    }
                    List<Integer> listID = mydb.getAllAlarmIDs(); //id to pass via notification also has to be set!
                    //id is the last added, so the last element of ListID
                    idToUpdate = listID.get(listID.size() - 1);
                    //Log.e("G",Integer.toString(idToUpdate));
                }

                //set alarm using alarmmanager
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                //intent to DisplayNotification
                //Intent intent = new Intent("com.abbyberkers.DisplayNotification");
                Intent intent = new Intent(getApplicationContext(), DisplayNotification.class);
                //add the message and id
                intent.putExtra("id", idToUpdate);
                intent.putExtra("message", message);
                intent.putExtra("snoozeMessage", R.string.snooze_message);
                //intent.setAction("foo"); //dummy action?

                /** set the flags so the mainactivity isn't started when the notification is triggered
                 * use new task to put the displaynotif in a new task, and multiple task so it doesn't
                 * interfere with the main task. This way, the backstack of that main task isn't desturbed(?)
                 * and when you hit the back button when in reminder activity you go back to your last page(?)
                 */
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                PendingIntent displayIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), idToUpdate, //assign a unique id, using the database id
                        intent, PendingIntent.FLAG_CANCEL_CURRENT); //instead of FLAG_UPDATE_CURRENT

//                cancelAlarmIfExists();

//                //finally, set alarm
//                alarmManager.set(AlarmManager.RTC_WAKEUP,
//                        time, displayIntent);

                if (repeat) {
                    //finally, set repeating alarm
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                            time, interval, displayIntent);
                } else {
                    //finally, set alarm
                    alarmManager.set(AlarmManager.RTC_WAKEUP,
                            time, displayIntent);
                }


                //after alarm added, to back to main
                Intent mainIntent = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(mainIntent);
            }
        }
    }

    public void cancelAlarmIfExists() {
        /**
         * uses global idToUpdate
         */
        try {
            Intent intent = new Intent(getApplicationContext(), DisplayNotification.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(), idToUpdate, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            am.cancel(pendingIntent);

            //cancel notification if displayed, when cancel button clicked
            NotificationManager nm = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(idToUpdate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void intervalFragment(View view) {
        android.app.FragmentManager fm = getFragmentManager();
        IntervalChoiceFragment intervalChoiceFragment = new IntervalChoiceFragment();
        intervalChoiceFragment.show(fm, "Snooze me for...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_settings); //go back to main
        item.setTitle(R.string.main);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        addAlarm(); //also save, view passed = button
        return true;
    }

}
