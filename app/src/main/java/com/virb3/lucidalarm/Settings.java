package com.virb3.lucidalarm;

import android.content.Context;
import android.content.SharedPreferences;

public final class Settings
{
    private static SharedPreferences Settings;

    //TODO: Always initialize first
    public static void Initialize(Context context)
    {
        Settings = context.getSharedPreferences("PREFERENCES", 0);
    }

    public static String AlarmSoundPath()
    {
        return Settings.getString("alarmSoundPath", "none");
    }

    public static int Hours()
    {
        return Settings.getInt("hours", 3);
    }

    public static int Minutes()
    {
        return Settings.getInt("minutes", 0);
    }

    public static int RingCount()
    {
        return Settings.getInt("ringCount", 3);
    }

    public static int RingInterval()
    {
        return Settings.getInt("ringInterval", 15);
    }

    public static int RingDuration()
    {
        return Settings.getInt("ringDuration", 4);
    }

    public static boolean Enabled()
    {
        return Settings.getBoolean("enabled", false);
    }

    public static int CompletedAlarms()
    {
        return Settings.getInt("completedAlarms", 0);
    }
}
