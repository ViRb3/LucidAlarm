package com.virb3.lucidalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;
import java.util.Date;

public class SampleAlarmReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent service = new Intent(context, SampleSchedulingService.class);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }

    public void SetAlarm(Context context, int hours, int minutes, int seconds, int id) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SampleAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTime().before(Calendar.getInstance().getTime())) // already passed, schedule for next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void CancelAlarm(Context context) {

        SharedPreferences settings = context.getSharedPreferences("PREFERENCES", 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        int setAlarms = settings.getInt("ringCount", -1);

        if (setAlarms == -1)
            return;

        for(int i = 1; i <= setAlarms; i++)
        {
            Intent intent = new Intent(context, SampleAlarmReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
            alarmManager.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the 
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
