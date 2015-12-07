package com.virb3.lucidalarm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

public class AlarmSchedulingService extends IntentService {
    public AlarmSchedulingService() {
        super("SchedulingService");
    }

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    private NotificationManager _notificationManager;
    private NotificationCompat.Builder _builder;

    public static MediaPlayer MediaPlayer;

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

        PlayAlarm(this, Settings.RingDuration() * 1000);

        SharedPreferences settings = this.getSharedPreferences("PREFERENCES", 0);
        SharedPreferences.Editor editor = settings.edit();

        int completedAlarms = Settings.CompletedAlarms() + 1;

        // last alarm, reset alarms
        if (completedAlarms == Settings.RingCount())
        {
            editor.putInt("completedAlarms", 0);

            AlarmReceiver alarmReceiver = new AlarmReceiver();
            alarmReceiver.SetAlarms(this, Settings.Hours(), Settings.Minutes());
        }
        else
            editor.putInt("completedAlarms", completedAlarms);

        editor.apply();
    }

    public static void PlayAlarm(Context context, int duration)
    {
        String alarmPath = Settings.AlarmSoundPath();

        if (alarmPath.equals("none") || !new File(alarmPath).exists())
            alarmPath = "";

        Uri alarm = alarmPath.equals("") ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
                Uri.fromFile(new File(alarmPath));

        if (MediaPlayer != null && MediaPlayer.isPlaying())
            MediaPlayer.stop();

        MediaPlayer = new MediaPlayer();

        MediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        try
        {
            MediaPlayer.setDataSource(context, alarm);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            MediaPlayer.prepare();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        MediaPlayer.start();

        Muter muter = new Muter();
        Timer timer = new Timer();
        timer.schedule(muter, duration);
    }
}
