package com.abbyberkers.gardener;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;


public class IntervalChoiceFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    /**
     * Interval choice when adding/editing a repeating alarm in ShowAlarm
     */
    String[] listitems = {"Not repeating", "Every minute", "Every hour", "Every day", "Every week"};

    ListView mylist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choice_fragment, container, false);
        mylist = (ListView) view.findViewById(R.id.list);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, listitems);

        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        long interval = 0;
        switch (position) {
            case 0:
                interval = 0; //not repeating
                break;
            case 1:
                interval = TimeUnit.MINUTES.toMillis(1);
                break;
            case 2:
                interval = TimeUnit.HOURS.toMillis(1);
                break;
            case 3:
                interval = TimeUnit.DAYS.toMillis(1);
                break;
            case 4:
                interval = TimeUnit.DAYS.toMillis(7);
                break;
            default:
                Toast.makeText(getActivity(),
                        "Something went wrong selecting the interval", Toast.LENGTH_SHORT).show();
        }
        ((ShowAlarm) getActivity()).intervalPass(interval);
        dismiss();
    }


}
