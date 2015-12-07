package com.virb3.lucidalarm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;


public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new TimePickerDialog(getActivity(), this, Settings.Hours(), Settings.Minutes(), true);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        SharedPreferences settings = getActivity().getSharedPreferences("PREFERENCES", 0);
        SharedPreferences.Editor editor = settings.edit();

        MainActivity activity = (MainActivity) getActivity();
        editor.putInt("hours", hourOfDay);
        editor.putInt("minutes", minute);

        editor.commit();

        TextView txtAlarmTimeActual = (TextView) activity.findViewById(R.id.txtAlarmTimeActual);
        txtAlarmTimeActual.setText(String.format("%1$02d:%2$02d", hourOfDay, minute));
        activity.Save();
    }
}