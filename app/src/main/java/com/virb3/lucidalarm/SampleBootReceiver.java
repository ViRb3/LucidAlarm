package com.virb3.lucidalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SampleBootReceiver extends BroadcastReceiver
{
    SampleAlarmReceiver alarm = new SampleAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (!intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            return;

        SharedPreferences settings = context.getSharedPreferences("PREFERENCES", 0);
        int hours = settings.getInt("hours", -1);
        int minutes = settings.getInt("minutes", -1);
        int setAlarms = settings.getInt("ringCount", -1);
        int interval = settings.getInt("ringInterval", -1);
        int ringDuration = settings.getInt("ringDuration", -1);

        if (hours == -1 || minutes == -1 || setAlarms == -1 || interval == -1 || ringDuration == -1)
            return;

        int currentMinutes = minutes;
        int currentSeconds = 0;

        for(int i = 1; i <= setAlarms; i++)
        {
            alarm.SetAlarm(context, hours, currentMinutes, currentSeconds, i);
            currentSeconds += interval + ringDuration;

            while (currentSeconds >= 60)
            {
                currentMinutes++;
                currentSeconds -= 60;
            }
        }
    }
}