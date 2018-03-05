package com.shakeApp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SettingsActivity extends AppCompatActivity
{
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        intent =  new Intent(this, ShakeService.class);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
                onBackPressed();
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(200);
        seekBar.setProgress(600 - getDefaultsInt("shake_threshold", getBaseContext()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                setDefaultsInt("shake_threshold", 600 - progress, getBaseContext());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        Switch serviceState = findViewById(R.id.switch1);
        final Switch activeScreenOff = findViewById(R.id.switch2);
        final Switch vibrateShake = findViewById(R.id.switch3);

        serviceState.setChecked(getDefaultsBoolean("serviceState", getBaseContext()));
        activeScreenOff.setChecked(getDefaultsBoolean("unlock", getBaseContext()));
        vibrateShake.setChecked(getDefaultsBoolean("vibro", getBaseContext()));
        if(serviceState.isChecked())
        {
            activeScreenOff.setClickable(true);
            vibrateShake.setClickable(true);
        }

        serviceState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                if(isChecked)
                {
                    vibrateShake.setClickable(true);
                    activeScreenOff.setClickable(true);
                    setDefaultsBoolean("serviceState", true, getBaseContext());
                    startService(intent);
                }
                else
                {
                    setDefaultsBoolean("unlock", false, getBaseContext());
                    activeScreenOff.setChecked(false);
                    activeScreenOff.setClickable(false);
                    vibrateShake.setChecked(false);
                    vibrateShake.setClickable(false);
                    setDefaultsBoolean("serviceState", false, getBaseContext());
                    stopService(intent);
                }
            }
        });

        activeScreenOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                if(isChecked)
                    setDefaultsBoolean("unlock", true, getBaseContext());
                else {
                    setDefaultsBoolean("unlock", false, getBaseContext());
                    //PowerManager.WakeLock  wakeLock;
                    //PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    //if (powerManager != null) {
                    //    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
                    //    if (getDefaultsBoolean("unlock", getBaseContext()) && wakeLock.isHeld())
                    //        wakeLock.release();
                    //}
                }
            }
        });

        vibrateShake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    setDefaultsBoolean("vibro", true, getBaseContext());
                else
                    setDefaultsBoolean("vibro", false, getBaseContext());
            }
        });
    }

    private void setDefaultsBoolean(String key, Boolean value, Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void setDefaultsInt(String key, int value, Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private Boolean getDefaultsBoolean(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, false);
    }

    private int getDefaultsInt(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 500);
    }
}
