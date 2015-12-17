package com.virb3.lucidalarm;

import java.util.TimerTask;

class Muter extends TimerTask
{
    public void run()
    {
        if (AlarmPlayer.MediaPlayer == null)
            return;

        AlarmPlayer.MediaPlayer.stop();
        AlarmPlayer.MediaPlayer.release();
        AlarmPlayer.MediaPlayer = null;
    }
}