package com.virb3.lucidalarm;

import java.util.TimerTask;

class Muter extends TimerTask
{
    public void run()
    {
        if(AlarmSchedulingService.MediaPlayer == null)
            return;

        if(AlarmSchedulingService.MediaPlayer.isPlaying())
            AlarmSchedulingService.MediaPlayer.stop();

        AlarmSchedulingService.MediaPlayer.reset();
        AlarmSchedulingService.MediaPlayer.release();
        AlarmSchedulingService.MediaPlayer = null;
}
}