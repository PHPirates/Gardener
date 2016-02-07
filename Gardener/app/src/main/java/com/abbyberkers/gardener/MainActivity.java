package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 * Abby's branch
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Main activity, displays a ListView of all the alarms
     */
    private ListView obj;
    DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onResume();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mydb = new DBHelper(this);
        //make an object of the DBHelper class so we can use its methods
        final List<String> arrayList = mydb.getAllAlarms();
        final List<String> timesList = mydb.getAllAlarmTimes();
        final ArrayList<Integer> arrayListID = mydb.getAllAlarmIDs();
        //also get all id's so the first id in the array corresponds with the first message in the
        //other array
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_1, arrayList);

        boolean isEmpty = false;
        if (arrayList.isEmpty()) { //default
            arrayList.add("You have no alarms");
            timesList.add("");
            isEmpty=true;
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_2, android.R.id.text1, arrayList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                //no idea why this works, but it seems to.
                // it also seems to catch if messages disappear from database/listview
                LayoutInflater inflater = (LayoutInflater)getApplicationContext().
                            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (convertView == null) {
                    //if there's no view yet, create it
                    convertView = inflater.inflate(android.R.layout.simple_list_item_2,parent,false);
                }

                //View view = super.getView(position, convertView, parent); // throws NPE sometimes
                TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
                text1.setText(arrayList.get(position));
                text1.setTextColor(Color.BLACK);
                text2.setText(timesList.get(position));
                text2.setTextColor(Color.BLACK);
                return convertView;
            }
        };
        obj = (ListView) findViewById(R.id.listView1); //set obj to listview
        obj.setAdapter(arrayAdapter); //set our custom adapter to the listview

      
        if (!isEmpty) { //only set clicklistener if there are alarms
            //set clicklistener for items in the listview
            obj.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //what id do we need to search in the database
                    //set id to search from the id corresponding in the second (id) array
                    int idToSearch = arrayListID.get(position);

                    Bundle dataBundle = new Bundle();
                    dataBundle.putInt("id", idToSearch);
                    //start the ShowAlarm activity with the id to search in the database
                    Intent intent = new Intent(getApplicationContext(), ShowAlarm.class);
                    intent.putExtras(dataBundle);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Bundle dataBundle = new Bundle();
            dataBundle.putInt("id", 0);
            Intent intent = new Intent(getApplicationContext(), ShowAlarm.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }
}
