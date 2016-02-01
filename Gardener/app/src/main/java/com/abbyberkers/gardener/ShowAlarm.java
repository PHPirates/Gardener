package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.AlarmManager;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ShowAlarm extends AppCompatActivity {

    /**
     * Class to add a new alarm or edit an existing alarm. Contains buttons to set time and date,
     * save the alarm (and send it to the phone) and a cancel (add) or delete (edit) button.
     */
    private DBHelper mydb;
    int idToUpdate=0;
    EditText message;

    int year,month,day,hour,minute;
    //some instance variables, handy to pass things around
    FragmentManager fm = getSupportFragmentManager();

    String currentDateTimeString; //instance to pass to confirmfrag

    int Value; //id is global, set in oncreate

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

        message = (EditText)findViewById(R.id.messageText);

        mydb = new DBHelper(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) { //if there's something in the bundle
            Value = extras.getInt("id"); //get the id to search

            if (Value>0) {
                //then we want to change an alarm and not add one
                Cursor rs = mydb.getData(Value);
                idToUpdate = Value;
                rs.moveToFirst();

                String messag = rs.getString(
                        rs.getColumnIndex(DBHelper.ALARMS_COLUMN_MESSAGE)); //get message from database
                long dataDate = rs.getLong(
                        rs.getColumnIndex(DBHelper.ALARMS_COLUMN_DATE)); //get date

                if (!rs.isClosed()) {rs.close();} //close cursor

                message.setText(messag); //set database text to edittext
                //set cursor initially to the right
                message.setSelection(message.length());

                //convert long to string
                Date date = new Date(dataDate);
                //format the date to a string using the local date time formatting
                //change instance variable for the confirmFragment to use
                currentDateTimeString = DateFormat.getDateTimeInstance().format(date);

                //set text on buttons
                Button dateButton = (Button)findViewById(R.id.setDate);
                Button timeButton = (Button)findViewById(R.id.setTime);

                Date dateOnly = new Date(dataDate);
                String dateString = DateFormat.getDateInstance().format(dateOnly);
                dateButton.setText(dateString);

                Date timeOnly = new Date(dataDate);
                String timeString = DateFormat.getTimeInstance().format(timeOnly);
                timeButton.setText(timeString);


                //if you're changing something, set a clicklistener for delete
                Button deleteButton = (Button)findViewById(R.id.cancelButton);
                deleteButton.setText(R.string.delete);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mydb.deleteAlarm(idToUpdate);
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                });

            } else {
                //if you're adding something, set a clicklistener that cancels
                Button cancelButton = (Button)findViewById(R.id.cancelButton);
                cancelButton.setText(R.string.cancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                });

                printTime(); // print time on buttons
            }
        }
//      // getTimeByID method debug
//        long time = mydb.getTimeByID(idToUpdate);
//        String timeString = millisToText(time);
//        Toast.makeText(getApplicationContext(), timeString,Toast.LENGTH_SHORT).show();
    }

    public void showDateDialog(View view){
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
                bundle.putInt("month",month);
                bundle.putInt("day",day);
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

    public long getTimeDatabase(){
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

    public void datePass(int y, int m, int d){
        //date passed by fragment
        this.year=y;
        this.month=m;
        this.day=d;
        //get the others from the database, otherwise the other button gets the current time/date
        //but only when changing something...
        if (Value>0) {
            long dataDate = getTimeDatabase();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dataDate);
            this.hour = c.get(Calendar.HOUR_OF_DAY);
            this.minute = c.get(Calendar.MINUTE);
        }
        printTime(); //update button
    }

    public void timePass(int h, int m){
        this.hour=h;
        this.minute=m;
        if (Value>0) {
            long dataDate = getTimeDatabase();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dataDate);
            this.year = c.get(Calendar.YEAR);
            this.month = c.get(Calendar.MONTH);
            this.day = c.get(Calendar.DAY_OF_MONTH);
        }
        printTime();
    }

    public String millisToText(long m) {
        Date date = new Date(m);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(date);
    }

    public void printTime() {
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

        //TextView mTimeText = (TextView) findViewById(R.id.chosenTime);
        //use a string with placeholders
        //String textString = String.format(getResources().getString(R.string.chosenTime), currentDateTimeString);
        //mTimeText.setText(textString);

        Button dateButton = (Button)findViewById(R.id.setDate);
        Button timeButton = (Button)findViewById(R.id.setTime);

        Date dateOnly = new Date(c.getTimeInMillis());
        String dateString = DateFormat.getDateInstance().format(dateOnly);
        dateButton.setText(dateString);

        Date timeOnly = new Date(c.getTimeInMillis());
        String timeString = DateFormat.getTimeInstance().format(timeOnly);
        timeButton.setText(timeString);
    }

    public long timeToInt(){
        Calendar c = Calendar.getInstance();


        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        return c.getTimeInMillis();
    }

    public void addAlarm(View view) { //when save button clicked
        //also sets alarm

        Bundle extras = getIntent().getExtras();
        long time = timeToInt(); //get chosen time in millis
        //Toast.makeText(getApplicationContext(),millisToText(time),Toast.LENGTH_SHORT).show();
        if (extras !=null) {
            int Value = extras.getInt("id");
            if (Value>0) {
                //we're in edit mode
                //if update succeeded
                if(mydb.updateAlarm(idToUpdate,message.getText().toString(),
                        time)) {
                    Toast.makeText(getApplicationContext(),"Updated",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "not Updated", Toast.LENGTH_SHORT).show();
                }
            } else {
                if(mydb.insertAlarm(message.getText().toString(),
                        time)) {
                    Toast.makeText(getApplicationContext(),"Done",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "not done", Toast.LENGTH_SHORT).show();
                }
            }

            //set alarm using alarmmanager
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            //intent to DisplayNotification
            //Intent intent = new Intent("com.abbyberkers.DisplayNotification");
            Intent intent = new Intent(this,DisplayNotification.class);
            //add the message and id
            intent.putExtra("id",idToUpdate);
            intent.putExtra("message", message.getText().toString());
            intent.putExtra("snoozeMessage",R.string.snooze_message);
            //intent.setAction("foo"); //dummy action?

            /** set the flags so the mainactivity isn't started when the notification is triggered
             * use new task to put the displaynotif in a new task, and multiple task so it doesn't
             * interfere with the main task. This way, the backstack of that main task isn't desturbed(?)
             * and when you hit the back button when in reminder activity you go back to your last page(?)
             */
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            PendingIntent displayIntent = PendingIntent.getBroadcast(
                    this, idToUpdate, //assign a unique id, using the database id
                    intent, PendingIntent.FLAG_ONE_SHOT); //instead of FLAG_UPDATE_CURRENT


            cancelAlarmIfExists(this,idToUpdate,intent);

            //finally, set alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    time,displayIntent);



            //after alarm added, to back to main
            Intent mainIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(mainIntent);
        }
    }

    public void cancelAlarmIfExists(Context mContext, int id, Intent intent){
        try{
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent,0);
            AlarmManager am=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
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
        addAlarm(findViewById(R.id.addButton)); //also save, view passed = button
        Intent k = new Intent(this, MainActivity.class);
        startActivity(k);
        return true;
    }

}
