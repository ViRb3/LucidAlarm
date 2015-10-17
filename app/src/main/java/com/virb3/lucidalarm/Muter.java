package com.virb3.lucidalarm;

import java.util.TimerTask;

class Muter extends TimerTask
{
    public void run()
    {
        if(SampleSchedulingService.MediaPlayer == null)
            return;

        if(SampleSchedulingService.MediaPlayer.isPlaying())
            SampleSchedulingService.MediaPlayer.stop();

        SampleSchedulingService.MediaPlayer.reset();
        SampleSchedulingService.MediaPlayer.release();
        SampleSchedulingService.MediaPlayer = null;
    }
}