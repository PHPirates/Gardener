package com.abbyberkers.gardener;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShowReminder extends AppCompatActivity {

    int idToUpdate = 0;
    EditText message;
    int year, month, day;
    //some instance variables, handy to pass things around
    FragmentManager fm = getSupportFragmentManager();
    String currentDateTimeString; //instance to pass to confirmfrag
    int Value; //id is global, set in oncreate
    long interval = 0; //interval is set in SnoozeChoiceFragment or in oncreate
    boolean repeat = false;
    /**
     * Class to add a new alarm or edit an existing alarm. Contains buttons to set time and date,
     * save the alarm (and send it to the phone) and a cancel (add) or delete (edit) button.
     */
    private DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set initial instance variables values for printing
        Calendar c;
        c = Calendar.getInstance(); //get current time
        this.year = c.get(Calendar.YEAR);
        this.month = c.get(Calendar.MONTH);
        this.day = c.get(Calendar.DAY_OF_MONTH);

        message = (EditText) findViewById(R.id.messageText);

        mydb = new DBHelper(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) { //if there's something in the bundle gotten from main activity

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

                /**
                 * get stuff from database
                 */
                Cursor rs = mydb.getData(Value);
                idToUpdate = Value;
                rs.moveToFirst();

                String messag = rs.getString(
                        rs.getColumnIndex(DBHelper.ALARMS_COLUMN_MESSAGE)); //get message from database
//                long time = rs.getLong(
//                        rs.getColumnIndex(DBHelper.ALARMS_COLUMN_DATE)); //get date
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
                Date date = new Date(time);
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

                Date dateOnly = new Date(time);
                String dateString = DateFormat.getDateInstance().format(dateOnly);
                dateButton.setText(dateString);

                Date timeOnly = new Date(time);
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

        //let the fragment know it's for a reminder
        bundle.putString("alarmType","Reminder");

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

    /**
     * @return time from database corresponding to id in Value
     */
    public long getTimeDatabase() {
        Cursor rs = mydb.getData(Value);
        rs.moveToFirst();
        long dataDate = rs.getLong(
                rs.getColumnIndex(DBHelper.ALARMS_COLUMN_DATE)); //get date
        if (!rs.isClosed()) {
            rs.close();
        } //close cursor
        return dataDate;
    }

    /**
     * On date passed by fragment
     *
     * @param y year
     * @param m month
     * @param d day
     */
    public void datePass(int y, int m, int d) {
        //update instance variables
        this.year = y;
        this.month = m;
        this.day = d;
        printTime(); //update button
    }

//    public String millisToText(long m) {
//        Date date = new Date(m);
//        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
//    }

    public String intervalToText(long interval) {
        /**
         * uses that interval is always either days, hours or minutes
         * returns the text of the last of days, hours and minutes which is not zero.
         */

        if (interval == 0) { //if alarm is not repeating
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
        c.set(Calendar.SECOND, 0);

        //show text on buttons instead of in a texview
        //make a date object using the time we just set to the calendar instance
        Date date = new Date(c.getTimeInMillis());
        //format the date to a string using the local date time formatting
        //change instance variable for the confirmFragment to use

        currentDateTimeString = DateFormat.getDateTimeInstance().format(date);

        Button dateButton = (Button) findViewById(R.id.setDate);

        Date dateOnly = new Date(c.getTimeInMillis());
        String dateString = DateFormat.getDateInstance().format(dateOnly);
        dateButton.setText(dateString);
    }

    public long timeToInt() {
        Calendar c = Calendar.getInstance();


        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 3); //for reminders, set time at 3 in the morning
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c.getTimeInMillis();
    }

    /**
     * when save button clicked
     *  also sets alarm
     */
    public void addAlarm() {
        String message = this.message.getText().toString();
        message = message.trim();

        if (message.equals("")) {
            Toast.makeText(this, "Please fill in text field.", Toast.LENGTH_SHORT).show();
        } else {

            Bundle extras = getIntent().getExtras();
            long time = timeToInt(); //get chosen (or otherwise current) time in millis
            if (extras != null) {
                if (Value > 0) {
                    //if update succeeded
                    if (mydb.updateAlarm(idToUpdate, message,
                            time, interval, repeat)) {
                        Toast.makeText(getApplicationContext(),
                                "Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "not Updated", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mydb.insertAlarm(message,
                            time, interval, repeat)) {
                        Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "not done", Toast.LENGTH_SHORT).show();
                    }
                    //id to pass via notification also has to be set!
                    List<Integer> listID = mydb.getAllAlarmIDs();
                    //id is the last added, so the last element of ListID
                    idToUpdate = listID.get(listID.size() - 1);
                }

                //set alarm using alarmmanager
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                //intent to DisplayNotification
                Intent intent = new Intent(getApplicationContext(), DisplayNotification.class);
                //add the message and id
                intent.putExtra("id", idToUpdate);
                intent.putExtra("message", message);
                intent.putExtra("snoozeMessage", R.string.snooze_message);

                /** set the flags so the mainactivity isn't started when the notification is
                 * triggered use new task to put the displaynotif in a new task, and multiple task
                 * so it doesn't interfere with the main task. This way, the backstack of that
                 * main task isn't disturbed(?) and when you hit the back button when in
                 * reminder activity you go back to your last page(?)
                 */
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                PendingIntent displayIntent = PendingIntent.getBroadcast(
                        //assign a unique id, using the database id
                        getApplicationContext(), idToUpdate,
                        //instead of FLAG_UPDATE_CURRENT:
                        intent, PendingIntent.FLAG_CANCEL_CURRENT);

                if (repeat) {
                    //finally, set repeating alarm
                    alarmManager.setRepeating(AlarmManager.RTC,
                            time, interval, displayIntent);
                } else {
                    //finally, set alarm
                    alarmManager.set(AlarmManager.RTC,
                            time, displayIntent);
                }

                //after alarm added, to back to main
                Intent mainIntent = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(mainIntent);
            }
        }
    }

    /**
     * uses global idToUpdate
     */
    public void cancelAlarmIfExists() {
        try {
            Intent intent = new Intent(getApplicationContext(), DisplayNotification.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(), idToUpdate, intent, PendingIntent.FLAG_CANCEL_CURRENT);
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
        getMenuInflater().inflate(R.menu.menu_reminder, menu); //inflate reminder menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        addAlarm(); //also save
        return true;
    }
}
