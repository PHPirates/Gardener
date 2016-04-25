package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.widget.DatePicker;

public class DateFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Use the current date as the default date in the picker
        int year = getArguments().getInt("year"); //use the already chosen date from Main
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        String alarmType = getArguments().getString("alarmType");
        if (alarmType != null) {
            ((ShowReminder) getActivity()).datePass(year, month, day); //send data to showreminder

        } else {
            ((ShowAlarm) getActivity()).datePass(year, month, day); //send data to showalarm
        }
    }


}
