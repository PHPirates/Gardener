package com.abbyberkers.gardener;
/**
 * @author Thomas
 * @coauthor Abby
 */


import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.widget.DatePicker;

import java.util.Calendar;

public class DateFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        int year = getArguments().getInt("year"); //use the already chosen date from Main
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        ((ShowAlarm) getActivity()).datePass(year, month, day); //send data to showalarm
    }


}
