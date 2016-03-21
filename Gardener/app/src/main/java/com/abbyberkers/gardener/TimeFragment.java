package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TimePicker;
import android.text.format.DateFormat;

import android.support.v4.app.DialogFragment;

public class TimeFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int hour = getArguments().getInt("hour");
        int minute = getArguments().getInt("minute");

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));

    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        ((ShowAlarm) getActivity()).timePass(hour, minute);
    }
}
