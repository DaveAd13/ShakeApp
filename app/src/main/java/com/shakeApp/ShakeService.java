package com.shakeApp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;

import com.mukesh.tinydb.TinyDB;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class ShakeService extends Service implements SensorEventListener
{
    static Camera camera = null;
    static MediaRecorder mRecorder = null;
    private long lastUpdate = 0;
    private double last_x, last_y, last_z;
    private ArrayList<Double> new_x = new ArrayList<>();
    private ArrayList<Double> new_y = new ArrayList<>();
    private ArrayList<Double> new_z = new ArrayList<>();
    private ArrayList<Double> new_x_t = new ArrayList<>();
    private ArrayList<Double> new_y_t = new ArrayList<>();
    private ArrayList<Double> new_z_t = new ArrayList<>();
    private int j = 1;
    private SensorManager sm;
    private int minSavedCount;
    private double temp_x, temp_y, temp_z;
    private WifiManager wifiManager;
    private AudioManager audiomanager;
    private ArrayList<Double> saved_x;
    private ArrayList<Double> saved_y;
    private ArrayList<Double> saved_z;
    private Notification notification;
    private NotificationManager notificationManager;

    private double[] x_min_saved = new double[5];
    private double[] y_min_saved = new double[5];
    private double[] z_min_saved = new double[5];

    private double[] x_max_saved = new double[5];
    private double[] y_max_saved = new double[5];
    private double[] z_max_saved = new double[5];

    private double[] x_min_new = new double[5];
    private double[] y_min_new = new double[5];
    private double[] z_min_new = new double[5];

    private double[] x_max_new = new double[5];
    private double[] y_max_new = new double[5];
    private double[] z_max_new = new double[5];

    private double[] x_saved_min_average = new double[2];
    private double[] y_saved_min_average = new double[2];
    private double[] z_saved_min_average = new double[2];

    private double[] x_saved_max_average = new double[2];
    private double[] y_saved_max_average = new double[2];
    private double[] z_saved_max_average = new double[2];

    private double[] x_new_min_average = new double[2];
    private double[] y_new_min_average = new double[2];
    private double[] z_new_min_average = new double[2];

    private double[] x_new_max_average = new double[2];
    private double[] y_new_max_average = new double[2];
    private double[] z_new_max_average = new double[2];

    private double[] x_max_difference = new double[2];
    private double[] y_max_difference = new double[2];
    private double[] z_max_difference = new double[2];

    private double[] x_min_difference = new double[2];
    private double[] y_min_difference = new double[2];
    private double[] z_min_difference = new double[2];

    private double[] dif_average = {100, 100};

    private double[] x_mi_hat_el_array = new double[2];
    private double[] y_mi_hat_el_array = new double[2];
    private double[] z_mi_hat_el_array = new double[2];
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private ScreenReceiver mReceiver = new ScreenReceiver();
    private KeyEvent event;

    private double minEl;
    private String tempN;
    private int n;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }
        if(getDefaultsBoolean("unlock", getBaseContext()))
            wakeLock.acquire();                                     //Do Something
        get_minCount();
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mySensor;
        if (sm != null) {
            mySensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        if(getDefaultsBoolean("unlock", getBaseContext()) && wakeLock.isHeld())
            wakeLock.release();
        sm.unregisterListener(this);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        Sensor mSensor = event.sensor;

        if(mSensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            long curTime = System.currentTimeMillis();
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float k = 0.7f;
            x = k * x + (1 - k) * temp_x;
            if (temp_x == 0)
                x = event.values[0];
            temp_x = x;

            y = k * y + (1 - k) * temp_y;
            if(temp_y == 0)
                y = event.values[1];
            temp_y = y;

            z = k * z + (1 - k) * temp_z;
            if(temp_z == 0)
                z = event.values[2];
            temp_z = z;

            double speed = (abs(x + y + z - last_x - last_y - last_z) / diffTime) * 10000;

            if(speed > getDefaultsIntShake("shake_threshold", getBaseContext()) && diffTime < 1000)
            {
                new_x.add(x);
                new_y.add(y);
                new_z.add(z);

                if(j == minSavedCount)
                {
                    for(int i = 1; i <= 2; i++)
                    {
                        if(getDefaultsInt("savedCount" + String.valueOf(i), getBaseContext()) != 0)
                            getCompNumber(i - 1);
                    }
                    doIt();
                }
                    else
                        j++;

            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    private void get_minCount()
    {
        if(getDefaultsInt("savedCount1", getBaseContext()) != 0 && getDefaultsInt("savedCount2", getBaseContext()) == 0)
            minSavedCount = getDefaultsInt("savedCount1", getBaseContext());
        else if(getDefaultsInt("savedCount2", getBaseContext()) != 0 && getDefaultsInt("savedCount1", getBaseContext()) == 0)
            minSavedCount = getDefaultsInt("savedCount2", getBaseContext());
        else if(getDefaultsInt("savedCount1", getBaseContext()) != 0 && getDefaultsInt("savedCount2", getBaseContext()) != 0)
            minSavedCount = min(getDefaultsInt("savedCount1", getBaseContext()), getDefaultsInt("savedCount2", getBaseContext()));
    }

    private void getCompNumber(int n)
    {
        TinyDB tinyDB = new TinyDB(getBaseContext());
        saved_x = tinyDB.getListDouble("saved_x" + String.valueOf(n + 1));
        saved_y = tinyDB.getListDouble("saved_y" + String.valueOf(n + 1));
        saved_z = tinyDB.getListDouble("saved_z" + String.valueOf(n + 1));

        Collections.sort(saved_x);
        Collections.sort(saved_y);
        Collections.sort(saved_z);

        new_x_t = new_x;
        new_y_t = new_y;
        new_z_t = new_z;

        Collections.sort(new_x_t);
        Collections.sort(new_y_t);
        Collections.sort(new_z_t);

        for(int i = 0; i < 5; i++)
        {
            x_min_saved[i] = saved_x.get(i);
            y_min_saved[i] = saved_y.get(i);
            z_min_saved[i] = saved_z.get(i);

            x_max_saved[i] = saved_x.get(saved_x.size() - 1 - i);
            y_max_saved[i] = saved_y.get(saved_y.size() - 1 - i);
            z_max_saved[i] = saved_z.get(saved_z.size() - 1 - i);

            x_min_new[i] = new_x_t.get(i);
            y_min_new[i] = new_y_t.get(i);
            z_min_new[i] = new_z_t.get(i);

            x_max_new[i] = new_x_t.get(new_x_t.size() - 1 - i);
            y_max_new[i] = new_y_t.get(new_y_t.size() - 1 - i);
            z_max_new[i] = new_z_t.get(new_z_t.size() - 1 - i);
        }

        x_saved_min_average[n] = get_average(x_min_saved);
        y_saved_min_average[n] = get_average(y_min_saved);
        z_saved_min_average[n] = get_average(z_min_saved);

        x_saved_max_average[n] = get_average(x_max_saved);
        y_saved_max_average[n] = get_average(y_max_saved);
        z_saved_max_average[n] = get_average(z_max_saved);

        x_new_min_average[n] = get_average(x_min_new);
        y_new_min_average[n] = get_average(y_min_new);
        z_new_min_average[n] = get_average(z_min_new);

        x_new_max_average[n] = get_average(x_max_new);
        y_new_max_average[n] = get_average(y_max_new);
        z_new_max_average[n] = get_average(z_max_new);

        x_max_difference[n] = x_saved_max_average[n] - x_new_max_average[n];
        y_max_difference[n] = y_saved_max_average[n] - y_new_max_average[n];
        z_max_difference[n] = z_saved_max_average[n] - z_new_max_average[n];

        x_min_difference[n] = x_saved_min_average[n] - x_new_min_average[n];
        y_min_difference[n] = y_saved_min_average[n] - y_new_min_average[n];
        z_min_difference[n] = z_saved_min_average[n] - z_new_min_average[n];

        x_mi_hat_el_array[n] = (x_max_difference[n] + x_min_difference[n])/2;
        y_mi_hat_el_array[n] = (y_max_difference[n] + y_min_difference[n])/2;
        z_mi_hat_el_array[n] = (z_max_difference[n] + z_min_difference[n])/2;

        dif_average[n] = abs((x_mi_hat_el_array[n] + y_mi_hat_el_array[n] + z_mi_hat_el_array[n])/3);
    }

    private double get_average(double[] a)
    {
        double sum = 0;
        for (Double anA : a) sum = sum + anA;
        return sum / a.length;
    }

    private void doIt()
    {
        if (dif_average[1] < dif_average[0])
        {
            minEl = dif_average[1];
            tempN = "2";
            n = 1;
        }
        else
        {
            minEl = dif_average[0];
            tempN = "1";
            n = 0;
        }

        if (minEl < 0.5 && x_new_max_average[n] * x_saved_max_average[n] > 0 && y_new_max_average[n] * y_saved_max_average[n] > 0 && z_new_max_average[n] * z_saved_max_average[n] > 0
                && x_new_min_average[n] * x_saved_min_average[n] > 0 && y_new_min_average[n] * y_saved_min_average[n] > 0 && z_new_min_average[n] * z_saved_min_average[n] > 0)
        {
            if (getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) && getDefaultsBoolean(tempN + "flashLight", getBaseContext()))
            {
                checkVibration();
                if (!getDefaultsBoolean("flash", this))
                {
                    setDefaultsBoolean("flash", true, this);
                    camera = Camera.open();
                    Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);
                    camera.startPreview();
                } else
                {
                    setDefaultsBoolean("flash", false, this);
                    camera.stopPreview();
                    camera.release();
                }
            }

            if(getDefaultsString("music_action" + tempN, getBaseContext()) != null)
            {
                if (getDefaultsString("music_action" + tempN, getBaseContext()).equals("next"))
                {
                    checkVibration();
                    audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
                    audiomanager.dispatchMediaKeyEvent(event);
                }

                if (getDefaultsString("music_action" + tempN, getBaseContext()).equals("prev"))
                {
                    checkVibration();
                    audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                    audiomanager.dispatchMediaKeyEvent(event);
                }

                if (getDefaultsString("music_action" + tempN, getBaseContext()).equals("play"))
                {
                    checkVibration();
                    audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (audiomanager.isMusicActive())
                    {
                        event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
                        audiomanager.dispatchMediaKeyEvent(event);
                    } else
                    {
                        event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
                        audiomanager.dispatchMediaKeyEvent(event);
                    }
                }
            }

            if (getDefaultsBoolean(tempN + "wifi", getBaseContext()))
            {
                checkVibration();
                wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean wifiEnabled = wifiManager.isWifiEnabled();

                if (!wifiEnabled)
                {
                    wifiManager.setWifiEnabled(true);
                } else
                {
                    wifiManager.setWifiEnabled(false);
                }
            }

            if (getDefaultsBoolean(tempN + "sound", getBaseContext()))
            {
                checkVibration();
                audiomanager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

                if (audiomanager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
                {
                    audiomanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else
                {
                    audiomanager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
            }

            if (getDefaultsBoolean(tempN + "record", getBaseContext()))
            {
                checkVibration();
                notification = new Notification.Builder(this)
                                .setSmallIcon(R.drawable.ic_rec)
                                .setOngoing(true)
                                .setContentTitle("ShakeApp")
                                .setContentText("Recording...").build();
                notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

                if(!getDefaultsBoolean("record", getBaseContext()))
                {
                    notificationManager.notify(1, notification);
                    startRecording();
                    setDefaultsBoolean("record", true, getBaseContext());
                }
                else
                {
                    notificationManager.cancel(1);
                    stopRecording();
                    setDefaultsBoolean("record", false, getBaseContext());
                }
            }

            if (getDefaultsString(tempN + "Action", getBaseContext()) != null)
            {
                checkVibration();
                if (getDefaultsBoolean("unlock", getBaseContext()))
                {
                    Intent launchIntent = new Intent(this, LaunchActivity.class);
                    launchIntent.putExtra("key", getDefaultsString(tempN + "Action", getBaseContext()));
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                    stopService(new Intent(this, ShakeService.class));
                }
                else
                {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getDefaultsString(tempN + "Action", getBaseContext()));
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                    stopService(new Intent(this, ShakeService.class));
                }
            }
            j = 1;
            new_x.clear();
            new_y.clear();
            new_z.clear();
            stopService(new Intent(this, ShakeService.class));
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    startService(new Intent(ShakeService.this, ShakeService.class));
                }
            }, 500);
        } else
        {
            j--;
            new_x.remove(0);
            new_y.remove(0);
            new_z.remove(0);
            for (int i = j; i < new_x.size(); i++)
            {
                new_x.remove(i);
                new_y.remove(i);
                new_z.remove(i);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    private void startRecording()
    {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        String a = Environment.getExternalStorageDirectory().getAbsolutePath();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.US);
        String currentDateAndTime = sdf.format(new Date());
        a += "/ShakeRecords/ShakeVoice_" + currentDateAndTime + ".mp3";
        mRecorder.setOutputFile(a);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try
        {
            mRecorder.prepare();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        try {
            mRecorder.start();
        } catch (Throwable t) {
            t.printStackTrace();
            Log.w("Error", t);
        }
    }

    private void stopRecording()
    {
        try {
            mRecorder.stop();
        } catch (Throwable t) {
            t.printStackTrace();
            Log.w("Error", t);
        }
        mRecorder.release();
        mRecorder = null;
    }

    private void checkVibration() {
        if (getDefaultsBoolean("vibro", getBaseContext()))
        {
            Vibrator v = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(150);
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

    private int getDefaultsInt(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 0);
    }

    private int getDefaultsIntShake(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 500);
    }

    private String getDefaultsString(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
}