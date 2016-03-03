package com.abbyberkers.gardener;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SnoozeChoiceFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    /**
     * Choice the snoozing delay in the AlarmReceiver
     */


    ListView mylist;
    long minute = TimeUnit.MINUTES.toMillis(1);
    long hour = TimeUnit.HOURS.toMillis(1);
    long day = TimeUnit.DAYS.toMillis(1);
    long week = TimeUnit.DAYS.toMillis(7);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choice_fragment, null, false);
        mylist = (ListView) view.findViewById(R.id.list);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        long oldTime = getArguments().getLong("oldtime"); //get old time from AlarmReceiver
        //set listitems including additions
        String[] listItems = {"1 minute, " + millisToText(oldTime + minute),
                "1 hour, " + millisToText(oldTime + hour),
                "1 day, " + millisToText(oldTime + day),
                "1 week, " + millisToText(oldTime + week)};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, listItems);

        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {


        long delay = 0;
        switch (position) {
            case 0:
                delay = minute;
                break;
            case 1:
                delay = hour;
                break;
            case 2:
                delay = day;
                break;
            case 3:
                delay = week;
                break;
            default:
                Toast.makeText(getActivity(),
                        "Something went wrong selecting the delay", Toast.LENGTH_SHORT).show();
        }
        ((AlarmReceiver) getActivity()).choicePass(delay);
        dismiss();
    }

    public String millisToText(long m) {
        Date date = new Date(m);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
    }

}
