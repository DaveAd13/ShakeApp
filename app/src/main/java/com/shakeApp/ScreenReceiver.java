package com.shakeApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ScreenReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            //setDefaultsBoolean("screenOff", false, context);
            if(!getDefaultsBoolean("unlock", context))
            {
                context.startService(new Intent(context, ShakeService.class));
                context.stopService(new Intent(context, CheckService.class));
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            //setDefaultsBoolean("screenOff", true, context);
            if(!getDefaultsBoolean("unlock", context))
            {
                context.startService(new Intent(context, CheckService.class));
                context.stopService(new Intent(context, ShakeService.class));
            }
        }
    }

    private void setDefaultsBoolean(String key, Boolean value, Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private Boolean getDefaultsBoolean(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, false);
    }

}