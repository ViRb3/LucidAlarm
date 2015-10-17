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

import java.io.IOException;
import java.util.Timer;

public class SampleSchedulingService extends IntentService {
    public SampleSchedulingService() {
        super("SchedulingService");
    }

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    SampleAlarmReceiver alarm = new SampleAlarmReceiver();
    public static MediaPlayer MediaPlayer;

    @Override
    protected void onHandleIntent(Intent intent) {
        sendNotification("WAKE UP");

        // Release the wake lock provided by the BroadcastReceiver.
        SampleAlarmReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg)
    {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Lucid Alarm")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        SharedPreferences settings = getApplicationContext().getSharedPreferences("PREFERENCES", 0);
        PlayAlarm(this, settings.getInt("ringDuration", 4) * 1000);

        //TODO: Re-create alarm
    }

    public static void PlayAlarm(Context context, int duration)
    {
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

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
        Timer myTimer = new Timer();
        myTimer.schedule(muter, duration);
    }
}
