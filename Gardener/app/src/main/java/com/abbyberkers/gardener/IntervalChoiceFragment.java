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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class IntervalChoiceFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    /**
     * Interval choice when adding/editing a repeating alarm in ShowAlarm
     */
    String[] listitems = { "Every minute", "Every hour", "Every day", "Every week" };

    ListView mylist;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, listitems);

        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {



        long delay = 0;
        switch (position) {
            case 0 :  delay = TimeUnit.MINUTES.toMillis(1);
                break;
            case 1 : delay = TimeUnit.HOURS.toMillis(1);
                break;
            case 2 : delay = TimeUnit.DAYS.toMillis(1);
                break;
            case 3 : delay = TimeUnit.DAYS.toMillis(7);
                break;
            default : Toast.makeText(getActivity(),
                    "Something went wrong selecting the delay",Toast.LENGTH_SHORT).show();
        }
        ((ShowAlarm)getActivity()).interval = delay;
        dismiss();
    }


}
