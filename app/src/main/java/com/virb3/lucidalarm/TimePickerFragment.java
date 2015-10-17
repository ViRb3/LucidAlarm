package com.virb3.lucidalarm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        return new TimePickerDialog(getActivity(), this, activity.Hours, activity.Minutes, true);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        MainActivity activity = (MainActivity) getActivity();
        activity.Hours = hourOfDay;
        activity.Minutes = minute;

        TextView txtAlarmTimeActual = (TextView) activity.findViewById(R.id.txtAlarmTimeActual);
        txtAlarmTimeActual.setText(String.format("%1$02d:%2$02d", hourOfDay, minute));
        activity.Save();
    }
}