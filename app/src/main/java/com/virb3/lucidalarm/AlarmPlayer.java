package com.virb3.lucidalarm;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

public class AlarmPlayer
{
    public static MediaPlayer MediaPlayer;
    private static Timer _alarmMuter;

    public static void Play(Context context, final int duration)
    {
        String alarmPath = Settings.AlarmSoundPath();

        if (alarmPath.equals("none") || !new File(alarmPath).exists())
            alarmPath = "";

        Uri alarm = alarmPath.equals("") ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
                Uri.fromFile(new File(alarmPath));

        if (_alarmMuter != null)
            _alarmMuter.cancel();

        if (MediaPlayer != null)
        {
            if (MediaPlayer.isPlaying())
                MediaPlayer.stop();

            MediaPlayer.release();
        }

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

        _alarmMuter = new Timer();
        _alarmMuter.schedule(muter, duration);
    }
}