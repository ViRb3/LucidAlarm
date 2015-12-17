package com.virb3.lucidalarm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

public class AlarmSchedulingService extends IntentService
{
    public AlarmSchedulingService()
    {
        super("SchedulingService");
    }

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    private NotificationManager _notificationManager;
    private NotificationCompat.Builder _builder;

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Settings.Initialize(this);
        sendNotification("WAKE UP");

        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg)
    {
        _notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        _builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Lucid Alarm")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true);

        _builder.setContentIntent(contentIntent);
        _notificationManager.notify(NOTIFICATION_ID, _builder.build());

        AlarmPlayer.Play(this, Settings.RingDuration() * 1000);

        SharedPreferences settings = this.getSharedPreferences("PREFERENCES", 0);
        SharedPreferences.Editor editor = settings.edit();

        int completedAlarms = Settings.CompletedAlarms() + 1;

        // last alarm, reset alarms
        if (completedAlarms == Settings.RingCount())
        {
            editor.putInt("completedAlarms", 0);

            AlarmReceiver alarmReceiver = new AlarmReceiver();
            alarmReceiver.SetAlarms(this, Settings.Hours(), Settings.Minutes());
        } else
            editor.putInt("completedAlarms", completedAlarms);

        editor.apply();
    }
}
