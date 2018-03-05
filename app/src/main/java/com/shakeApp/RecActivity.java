package com.shakeApp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.mukesh.tinydb.TinyDB;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.shakeApp.R.id.pager;
import static java.lang.Math.abs;

public class RecActivity extends FragmentActivity implements SensorEventListener, Toolbar.OnMenuItemClickListener
{
    private int SHAKE_THRESHOLD;
    private long lastUpdate = 0;
    private double last_x, last_y, last_z;
    private ArrayList<Double> saved_x = new ArrayList<>();
    private ArrayList<Double> saved_y = new ArrayList<>();
    private ArrayList<Double> saved_z = new ArrayList<>();
    private Button recB1;
    private Button recB2;
    private Button selB1;
    private Button selB2;
    private Button startB;
    private Button listButton1;
    private Button listButton2;
    private Button prev1;
    private Button play1;
    private Button next1;
    private Button prev2;
    private Button play2;
    private Button next2;
    private double temp_x, temp_y, temp_z;
    private boolean record = false;
    private int savedCount = 0;
    private TinyDB tinyDB;
    private String number = "";
    private PackageManager packageManager;
    private ListView list;
    private ApplicationAdapter listAdapter;
    private InterstitialAd mInterstitialAd;
    private Dialog app_list;
    private ProgressBar progressBar;
    private ProgressBar progressBar2;
    private RelativeLayout progressLayout;
    private RelativeLayout selectLayout;
    private RelativeLayout mControl1;
    private RelativeLayout mControl2;
    private RelativeLayout tutorialLayout;
    private ImageView image1, image2;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu);
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.rootView);
        LayoutTransition lt = new LayoutTransition();
        lt.setDuration(100);
        rootView.setLayoutTransition(lt);

        tutorialLayout = (RelativeLayout) findViewById(R.id.tutorialLayout);
        ViewPager viewpager = (ViewPager) findViewById(pager);
        viewpager.setAdapter(new MyPagerAdapter(this));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewpager, true);

        if(!getDefaultsBoolean("first time", getBaseContext()))
        {
            startService(new Intent(getBaseContext(), ShakeService.class));
            setDefaultsBoolean("serviceState", true, getBaseContext());
            setDefaultsBoolean("first time", true, getBaseContext());
            setDefaultsBoolean("vibro", true, getBaseContext());
        }
        if(!getDefaultsBoolean("how to use", getBaseContext()))
        {
            tutorialLayout.setVisibility(View.VISIBLE);
        }

        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        toolbar2.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(this);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-8284087608066656~2257845921");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8284087608066656/7327173921");

        mInterstitialAd.setAdListener(new AdListener()
        {
            @Override
            public void onAdClosed()
            {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();

        SHAKE_THRESHOLD = getDefaultsInt("shake_threshold", getBaseContext());
        tinyDB = new TinyDB(getBaseContext());

        SensorManager SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert SM != null;
        Sensor mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_GAME);

        Button play_store = (Button) findViewById(R.id.play_store);
        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);
        prev1 = (Button) findViewById(R.id.prev1);
        play1 = (Button) findViewById(R.id.play1);
        next1 = (Button) findViewById(R.id.next1);
        prev2 = (Button) findViewById(R.id.prev2);
        play2 = (Button) findViewById(R.id.play2);
        next2 = (Button) findViewById(R.id.next2);
        Button button0 = (Button) findViewById(R.id.button0);
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);
        Button button5 = (Button) findViewById(R.id.button5);
        Button button6 = (Button) findViewById(R.id.button6);
        TextView skip_button = (TextView) findViewById(R.id.skip_but);
        textView = (TextView) findViewById(R.id.textView);
        progressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
        selectLayout = (RelativeLayout) findViewById(R.id.selectLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        recB1 = (Button) findViewById(R.id.recButton1);
        recB2 = (Button) findViewById(R.id.recButton2);
        selB1 = (Button) findViewById(R.id.selButton1);
        selB2 = (Button) findViewById(R.id.selButton2);
        startB = (Button) findViewById(R.id.startButton);
        listButton1 = (Button) findViewById(R.id.listButton1);
        listButton2 = (Button) findViewById(R.id.listButton2);
        mControl1 = (RelativeLayout) findViewById(R.id.mControl1);
        mControl2 = (RelativeLayout) findViewById(R.id.mControl2);

        progressBar.setOnClickListener(onClickListener);
        recB1.setOnClickListener(onClickListener);
        recB2.setOnClickListener(onClickListener);
        selB1.setOnClickListener(onClickListener);
        selB2.setOnClickListener(onClickListener);
        prev1.setOnClickListener(onClickListener);
        prev2.setOnClickListener(onClickListener);
        play1.setOnClickListener(onClickListener);
        play2.setOnClickListener(onClickListener);
        next1.setOnClickListener(onClickListener);
        next2.setOnClickListener(onClickListener);
        startB.setOnClickListener(onClickListener);
        button0.setOnClickListener(onClickListener);
        button1.setOnClickListener(onClickListener);
        button2.setOnClickListener(onClickListener);
        button3.setOnClickListener(onClickListener);
        button4.setOnClickListener(onClickListener);
        button5.setOnClickListener(onClickListener);
        button6.setOnClickListener(onClickListener);
        play_store.setOnClickListener(onClickListener);
        progressLayout.setOnClickListener(onClickListener);
        selectLayout.setOnClickListener(onClickListener);
        listButton1.setOnClickListener(onClickListener);
        listButton2.setOnClickListener(onClickListener);
        skip_button.setOnClickListener(onClickListener);
        setButtonIconsAndSpinners();

    }

    public void swapActions(String n, String action)
    {
        setDefaultsBoolean(n + "sound", false, getBaseContext());
        setDefaultsBoolean(n + "wifi", false, getBaseContext());
        setDefaultsBoolean(n + "flashLight", false, getBaseContext());
        setDefaultsBoolean(n + "record", false, getBaseContext());
        setDefaultsString("music_action" + n, null, getBaseContext());

        if(!action.equals("action"))
            setDefaultsString(n + "Action", null, getBaseContext());
        if(!action.equals("disable") && !action.equals("action"))
            setDefaultsBoolean(n + action, true, getBaseContext());
    }

    public void clickNext(int n)
    {
        if(n == 1)
        {
            next1.setAlpha(1f);
            scaleView(next1, 1f, 2f, 1f, 2f, 0f);

            if (getDefaultsAction("music_action1", getBaseContext()).equals("play"))
            {
                play1.setAlpha(0.7f);
                scaleView(play1, 2f, 1f, 2f, 1f, 0.5f);
            }
            if (getDefaultsAction("music_action1", getBaseContext()).equals("prev"))
            {
                prev1.setAlpha(0.7f);
                scaleView(prev1, 2f, 1f, 2f, 1f, 0.5f);
            }
            setDefaultsString("music_action1", "next", getBaseContext());
        }
        else
        {
            next2.setAlpha(1f);
            scaleView(next2, 1f, 2f, 1f, 2f, 0f);

            if (getDefaultsAction("music_action2", getBaseContext()).equals("play"))
            {
                play2.setAlpha(0.7f);
                scaleView(play2, 2f, 1f, 2f, 1f, 0.5f);
            }
            if (getDefaultsAction("music_action2", getBaseContext()).equals("prev"))
            {
                prev2.setAlpha(0.7f);
                scaleView(prev2, 2f, 1f, 2f, 1f, 0.5f);
            }
            setDefaultsString("music_action2", "next", getBaseContext());
        }
    }

    public void clickPlay(int n)
    {
        if(n == 1)
        {
            play1.setAlpha(1f);
            scaleView(play1, 1f, 2f, 1f, 2f, 0.5f);

            if (getDefaultsAction("music_action1", getBaseContext()).equals("next"))
            {
                next1.setAlpha(0.7f);
                scaleView(next1, 2f, 1f, 2f, 1f, 0.5f);
            }
            if (getDefaultsAction("music_action1", getBaseContext()).equals("prev"))
            {
                prev1.setAlpha(0.7f);
                scaleView(prev1, 2f, 1f, 2f, 1f, 0.5f);
            }
            setDefaultsString("music_action1", "play", getBaseContext());
        }
        else
        {
            play2.setAlpha(1f);
            scaleView(play2, 1f, 2f, 1f, 2f, 0.5f);

            if (getDefaultsAction("music_action2", getBaseContext()).equals("next"))
            {
                next2.setAlpha(0.7f);
                scaleView(next2, 2f, 1f, 2f, 1f, 0.5f);
            }
            if (getDefaultsAction("music_action2", getBaseContext()).equals("prev"))
            {
                prev2.setAlpha(0.7f);
                scaleView(prev2, 2f, 1f, 2f, 1f, 0.5f);
            }
            setDefaultsString("music_action2", "play", getBaseContext());
        }
    }

    public void clickPrev(int n)
    {
        if(n == 1)
        {
            prev1.setAlpha(1f);
            scaleView(prev1, 1f, 2f, 1f, 2f, 1f);

            if (getDefaultsAction("music_action1", getBaseContext()).equals("play"))
            {
                play1.setAlpha(0.7f);
                scaleView(play1, 2f, 1f, 2f, 1f, 0.5f);
            }
            if (getDefaultsAction("music_action1", getBaseContext()).equals("next"))
            {
                next1.setAlpha(0.7f);
                scaleView(next1, 2f, 1f, 2f, 1f, 0.5f);
            }
            setDefaultsString("music_action1", "prev", getBaseContext());
        }
        else
        {
            prev2.setAlpha(1f);
            scaleView(prev2, 1f, 2f, 1f, 2f, 1f);

            if (getDefaultsAction("music_action2", getBaseContext()).equals("play"))
            {
                play2.setAlpha(0.7f);
                scaleView(play2, 2f, 1f, 2f, 1f, 0.5f);
            }
            if (getDefaultsAction("music_action2", getBaseContext()).equals("next"))
            {
                next2.setAlpha(0.7f);
                scaleView(next2, 2f, 1f, 2f, 1f, 0.5f);
            }
            setDefaultsString("music_action2", "prev", getBaseContext());
        }
    }

    public void scaleView(View v, float x, float x1, float y, float y1, float a)
    {
        Animation anim = new ScaleAnimation(x, x1, y, y1, Animation.RELATIVE_TO_SELF, a, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);
        anim.setDuration(50);
        v.startAnimation(anim);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 100:
            {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    if(getDefaultsBoolean("1flashLight", getBaseContext()))
                        setSelection1(0);
                    if(getDefaultsBoolean("2flashLight", getBaseContext()))
                        setSelection2(0);
                }
            }
            break;
            case 200:
            {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                                               grantResults[1] != PackageManager.PERMISSION_GRANTED)
                {
                    if(getDefaultsBoolean("1record", getBaseContext()))
                        setSelection1(0);
                    if(getDefaultsBoolean("2record", getBaseContext()))
                        setSelection2(0);
                }
                else
                {
                    File folder = new File(Environment.getExternalStorageDirectory() + "/ShakeRecords");
                    if (!folder.exists())
                    {
                        if (!folder.mkdir())
                            Toast.makeText(RecActivity.this, "Failed to create folder for records, disable and reselect this action again pls.",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v) {
            switch(v.getId())
            {
                case R.id.recButton1:
                    number = "1";
                    progressLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.recButton2:
                    number = "2";
                    progressLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.progressLayout:
                    progressLayout.setVisibility(View.GONE);
                    break;
                case R.id.startButton:
                    progressBar.setVisibility(View.VISIBLE);
                    startB.setClickable(false);
                    progressLayout.setClickable(false);
                    textView.setText("STOP!");
                    savedCount = 0;
                    if(getDefaultsBoolean("serviceState", getBaseContext()))
                        stopService(new Intent(getBaseContext(), ShakeService.class));
                    record = !record;
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(record)
                                progressBar.performClick();
                        }
                    }, 1500);
                    break;
                case R.id.selButton1:
                    if (mInterstitialAd.isLoaded())
                        mInterstitialAd.show();
                    packageManager = getPackageManager();
                    setDefaultsString("selectApp", "1", getBaseContext());
                    app_list = new Dialog(RecActivity.this, R.style.NewDialog);
                    if(app_list.getWindow() != null)
                        app_list.getWindow().setBackgroundDrawable(new ColorDrawable(0xB3000000));
                    app_list.setContentView(R.layout.list);
                    app_list.setCancelable(true);
                    list = (ListView) app_list.findViewById(R.id.list);
                    LoadApplications loadApplications1 = new LoadApplications(selB1, recB1);
                    loadApplications1.execute();
                    break;
                case R.id.selButton2:
                    if (mInterstitialAd.isLoaded())
                        mInterstitialAd.show();
                    packageManager = getPackageManager();
                    setDefaultsString("selectApp", "2", getBaseContext());
                    app_list = new Dialog(RecActivity.this, R.style.NewDialog);
                    if(app_list.getWindow() != null)
                        app_list.getWindow().setBackgroundDrawable(new ColorDrawable(0xB3000000));
                    app_list.setContentView(R.layout.list);
                    app_list.setCancelable(true);
                    list = (ListView) app_list.findViewById(R.id.list);
                    LoadApplications loadApplications2 = new LoadApplications(selB2, recB2);
                    loadApplications2.execute();
                    break;
                case R.id.progressBar:
                    record = !record;
                    progressBar2.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    progressBar.setClickable(false);

                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            progressBar.setClickable(true);
                            startB.setClickable(true);
                            progressBar2.setVisibility(View.GONE);
                            textView.setText("START!");
                            progressLayout.setVisibility(View.GONE);
                            progressLayout.setClickable(true);
                        }
                    }, 800);
                    if(savedCount < 8)
                    {
                        textView.setText("Not Saved!");
                        Toast.makeText(getBaseContext(), "Too short move, record again please.", Toast.LENGTH_LONG).show();
                        saved_x.clear();
                        saved_y.clear();
                        saved_z.clear();
                    }
                    else
                    {
                        textView.setText("Saving...");
                        setDefaultsInt("savedCount" + number, savedCount, getBaseContext());
                        tinyDB.putListDouble("saved_x" + number, saved_x);
                        tinyDB.putListDouble("saved_y" + number, saved_y);
                        tinyDB.putListDouble("saved_z" + number, saved_z);
                        saved_x.clear();
                        saved_y.clear();
                        saved_z.clear();
                    }

                    if (getDefaultsBoolean("serviceState", getBaseContext()))
                        startService(new Intent(getBaseContext(), ShakeService.class));
                    break;
                case R.id.selectLayout:
                    onBackPressed();
                    break;
                case R.id.listButton1:
                    setDefaultsInt("action", 1, getBaseContext());
                    selectLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.listButton2:
                    setDefaultsInt("action", 2, getBaseContext());
                    selectLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.button0:
                    if(getDefaultsInt("action", getBaseContext()) == 1)
                        setSelection1(0);
                    else if(getDefaultsInt("action", getBaseContext()) == 2)
                        setSelection2(0);
                    selectLayout.setVisibility(View.GONE);
                    break;
                case R.id.button1:
                    if(getDefaultsInt("action", getBaseContext()) == 1)
                        setSelection1(1);
                    else if(getDefaultsInt("action", getBaseContext()) == 2)
                        setSelection2(1);
                    selectLayout.setVisibility(View.GONE);
                    break;
                case R.id.button2:
                    if(getDefaultsInt("action", getBaseContext()) == 1)
                        setSelection1(2);
                    else if(getDefaultsInt("action", getBaseContext()) == 2)
                        setSelection2(2);
                    selectLayout.setVisibility(View.GONE);
                    break;
                case R.id.button3:
                    if(getDefaultsInt("action", getBaseContext()) == 1)
                        setSelection1(3);
                    else if(getDefaultsInt("action", getBaseContext()) == 2)
                        setSelection2(3);
                    selectLayout.setVisibility(View.GONE);
                    break;
                case R.id.button4:
                    if(getDefaultsInt("action", getBaseContext()) == 1)
                        setSelection1(4);
                    else if(getDefaultsInt("action", getBaseContext()) == 2)
                        setSelection2(4);
                    selectLayout.setVisibility(View.GONE);
                    break;
                case R.id.button5:
                    if(getDefaultsInt("action", getBaseContext()) == 1)
                        setSelection1(5);
                    else if(getDefaultsInt("action", getBaseContext()) == 2)
                        setSelection2(5);
                    selectLayout.setVisibility(View.GONE);
                    break;
                case R.id.button6:
                    if(getDefaultsInt("action", getBaseContext()) == 1)
                        setSelection1(6);
                    else if(getDefaultsInt("action", getBaseContext()) == 2)
                        setSelection2(6);
                    selectLayout.setVisibility(View.GONE);
                    break;
                case R.id.play_store:
                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.shakeApp&hl=en");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                case R.id.prev1:
                    clickPrev(1);
                    break;
                case R.id.prev2:
                    clickPrev(2);
                    break;
                case R.id.play1:
                    clickPlay(1);
                    break;
                case R.id.play2:
                    clickPlay(2);
                    break;
                case R.id.next1:
                    clickNext(1);
                    break;
                case R.id.next2:
                    clickNext(2);
                    break;
                case R.id.skip_but:
                    setDefaultsBoolean("how to use", true, getBaseContext());
                    tutorialLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void requestNewInterstitial()
    {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void setButtonIconsAndSpinners()
    {
        if (getDefaultsInt("setSelection1", getBaseContext()) < 7)
            setSelection1(getDefaultsInt("setSelection1", getBaseContext()));
        else
            setSelection1(0);
        if (getDefaultsInt("setSelection2", getBaseContext()) < 7)
            setSelection2(getDefaultsInt("setSelection2", getBaseContext()));
        else
            setSelection2(0);

        if (getDefaultsString("1image", getBaseContext()) != null)
        {
            byte[] decodedString = Base64.decode(getDefaultsString("1image", getBaseContext()), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getBaseContext().getResources(), decodedByte);
            selB1.setBackground(bitmapDrawable);
        }

        if (getDefaultsString("2image", getBaseContext()) != null)
        {
            byte[] decodedString = Base64.decode(getDefaultsString("2image", getBaseContext()), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getBaseContext().getResources(), decodedByte);
            selB2.setBackground(bitmapDrawable);
        }
    }

    private void setSelection1(int position)
    {
        setDefaultsInt("setSelection1", position, getBaseContext());
        if (position == 0)
        {
            listButton1.setText("Select Action");
            image1.setVisibility(View.VISIBLE);
            selB1.setVisibility(View.GONE);
            recB1.setVisibility(View.GONE);
            mControl1.setVisibility(View.GONE);
            setDefaultsInt("savedCount1", 0, getBaseContext());
            tinyDB.remove("saved_x1");
            tinyDB.remove("saved_y1");
            tinyDB.remove("saved_z1");
            setDefaultsString("1image", null, getBaseContext());
            swapActions("1", "disable");
        }
        else if (position == 1)
        {
            listButton1.setText("Run Installed App");
            if (getDefaultsString("1Action", getBaseContext()) == null)
            {
                selB1.setBackgroundResource(R.drawable.select_button);
                recB1.setVisibility(View.GONE);
            }
            else
                recB1.setVisibility(View.VISIBLE);
            selB1.setVisibility(View.GONE);
            selB1.setVisibility(View.VISIBLE);
            image1.setVisibility(View.GONE);
            mControl1.setVisibility(View.GONE);
            selB1.setClickable(true);
            swapActions("1", "action");
        }
        else if (position == 2)
        {
            listButton1.setText("FlashLight On/Off");
            if (ContextCompat.checkSelfPermission(RecActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(RecActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
            }
            selB1.setBackgroundResource(R.drawable.flashlight_button);
            recB1.setVisibility(View.VISIBLE);
            selB1.setVisibility(View.GONE);
            selB1.setVisibility(View.VISIBLE);
            image1.setVisibility(View.GONE);
            mControl1.setVisibility(View.GONE);
            selB1.setClickable(false);
            swapActions("1", "flashLight");
        }
        else if (position == 3)
        {
            listButton1.setText("Wifi On/Off");
            selB1.setBackgroundResource(R.drawable.wifi_button);
            recB1.setVisibility(View.VISIBLE);
            selB1.setVisibility(View.GONE);
            selB1.setVisibility(View.VISIBLE);
            image1.setVisibility(View.GONE);
            mControl1.setVisibility(View.GONE);
            selB1.setClickable(false);
            swapActions("1", "wifi");
        }
        else if (position == 4)
        {
            listButton1.setText("Sound On/Off");
            selB1.setBackgroundResource(R.drawable.sound_button);
            recB1.setVisibility(View.VISIBLE);
            selB1.setVisibility(View.GONE);
            selB1.setVisibility(View.VISIBLE);
            image1.setVisibility(View.GONE);
            mControl1.setVisibility(View.GONE);
            selB1.setClickable(false);
            swapActions("1", "sound");
        }
        else if (position == 5)
        {
            listButton1.setText("Control Music Player");
            recB1.setVisibility(View.VISIBLE);
            selB1.setVisibility(View.GONE);
            image1.setVisibility(View.GONE);
            mControl1.setVisibility(View.VISIBLE);
            selB1.setClickable(false);
            setDefaultsBoolean("1sound", false, getBaseContext());
            setDefaultsBoolean("1wifi", false, getBaseContext());
            setDefaultsString("1Action", null, getBaseContext());
            setDefaultsBoolean("1flashLight", false, getBaseContext());
            if(getDefaultsAction("music_action1", getBaseContext()).equals("play"))
            {
                scaleView(play1, 1f, 2f, 1f, 2f, 0.5f);
                prev1.setAlpha(0.7f);
                next1.setAlpha(0.7f);
                setDefaultsString("music_action1", "play", getBaseContext());
            }
            if(getDefaultsAction("music_action1", getBaseContext()).equals("prev"))
            {
                scaleView(prev1, 1f, 2f, 1f, 2f, 1f);
                play1.setAlpha(0.7f);
                next1.setAlpha(0.7f);
            }
            if(getDefaultsAction("music_action1", getBaseContext()).equals("next"))
            {
                scaleView(next1, 1f, 2f, 1f, 2f, 0f);
                prev1.setAlpha(0.7f);
                play1.setAlpha(0.7f);
            }
        }
        else if(position == 6)
        {
            listButton1.setText("Record Sound");
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP)
                if (ContextCompat.checkSelfPermission(RecActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(RecActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(RecActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO}, 200);
                }
                else
                {
                    File folder = new File(Environment.getExternalStorageDirectory() + "/ShakeRecords");
                    if (!folder.exists())
                    {
                        if (!folder.mkdir())
                            Toast.makeText(RecActivity.this, "Failed to create folder for records, disable and reselect this action again pls.", Toast.LENGTH_LONG).show();
                    }
                }
            recB1.setVisibility(View.VISIBLE);
            selB1.setBackgroundResource(R.drawable.record_button);
            selB1.setVisibility(View.GONE);
            selB1.setVisibility(View.VISIBLE);
            image1.setVisibility(View.GONE);
            mControl1.setVisibility(View.GONE);
            selB1.setClickable(false);
            swapActions("1", "record");
        }
    }
    private void setSelection2(int position)
    {
        setDefaultsInt("setSelection2", position, getBaseContext());
        if (position == 0)
        {
            listButton2.setText("Select Action");
            image2.setVisibility(View.VISIBLE);
            selB2.setVisibility(View.GONE);
            recB2.setVisibility(View.GONE);
            mControl2.setVisibility(View.GONE);
            selB2.setBackgroundResource(R.drawable.select_button);
            setDefaultsInt("savedCount2", 0, getBaseContext());
            tinyDB.remove("saved_x2");
            tinyDB.remove("saved_y2");
            tinyDB.remove("saved_z2");
            setDefaultsString("2image", null, getBaseContext());
            swapActions("2", "disable");
        }
        else if (position == 1)
        {
            listButton2.setText("Run Installed App");
            if (getDefaultsString("2Action", getBaseContext()) == null)
            {
                selB2.setBackgroundResource(R.drawable.select_button);
                recB2.setVisibility(View.GONE);
            }
            else
                recB2.setVisibility(View.VISIBLE);
            selB2.setVisibility(View.GONE);
            selB2.setVisibility(View.VISIBLE);
            image2.setVisibility(View.GONE);
            mControl2.setVisibility(View.GONE);
            selB2.setClickable(true);
            swapActions("2", "action");
        }
        else if (position == 2)
        {
            listButton2.setText("FlashLight On/Off");
            if (ContextCompat.checkSelfPermission(RecActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(RecActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
            }
            recB2.setVisibility(View.VISIBLE);
            selB2.setVisibility(View.GONE);
            selB2.setVisibility(View.VISIBLE);
            image2.setVisibility(View.GONE);
            mControl2.setVisibility(View.GONE);
            selB2.setBackgroundResource(R.drawable.flashlight_button);
            selB2.setClickable(false);
            swapActions("2", "flashLight");
        }
        else if (position == 3)
        {
            listButton2.setText("Wifi On/Off");
            recB2.setVisibility(View.VISIBLE);
            selB2.setVisibility(View.GONE);
            selB2.setVisibility(View.VISIBLE);
            image2.setVisibility(View.GONE);
            mControl2.setVisibility(View.GONE);
            selB2.setBackgroundResource(R.drawable.wifi_button);
            selB2.setClickable(false);
            swapActions("2", "wifi");
        }
        else if (position == 4)
        {
            listButton2.setText("Sound On/Off");
            recB2.setVisibility(View.VISIBLE);
            selB2.setVisibility(View.GONE);
            selB2.setVisibility(View.VISIBLE);
            image2.setVisibility(View.GONE);
            mControl2.setVisibility(View.GONE);
            selB2.setBackgroundResource(R.drawable.sound_button);
            selB2.setClickable(false);
            swapActions("2", "sound");
        }
        else if (position == 5)
        {
            listButton2.setText("Control Music Player");
            recB2.setVisibility(View.VISIBLE);
            selB2.setVisibility(View.GONE);
            image2.setVisibility(View.GONE);
            mControl2.setVisibility(View.VISIBLE);
            selB2.setClickable(false);
            setDefaultsBoolean("2sound", false, getBaseContext());
            setDefaultsBoolean("2wifi", false, getBaseContext());
            setDefaultsString("2Action", null, getBaseContext());
            setDefaultsBoolean("2flashLight", false, getBaseContext());
            if(getDefaultsAction("music_action2", getBaseContext()).equals("play"))
            {
                scaleView(play2, 1f, 2f, 1f, 2f, 0.5f);
                prev2.setAlpha(0.7f);
                next2.setAlpha(0.7f);
                setDefaultsString("music_action2", "play", getBaseContext());
            }
            if(getDefaultsAction("music_action2", getBaseContext()).equals("prev"))
            {
                scaleView(prev2, 1f, 2f, 1f, 2f, 1f);
                play2.setAlpha(0.7f);
                next2.setAlpha(0.7f);
            }
            if(getDefaultsAction("music_action2", getBaseContext()).equals("next"))
            {
                scaleView(next2, 1f, 2f, 1f, 2f, 0f);
                prev2.setAlpha(0.7f);
                play2.setAlpha(0.7f);
            }
        }
        else if(position == 6)
        {
            listButton2.setText("Record Sound");
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP)
                if (ContextCompat.checkSelfPermission(RecActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(RecActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(RecActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO}, 200);
                }
                else
                {
                    File folder = new File(Environment.getExternalStorageDirectory() + "/ShakeRecords");
                    if (!folder.exists())
                    {
                        if (!folder.mkdir())
                            Toast.makeText(RecActivity.this, "Failed to create folder for records, disable and reselect this action again pls.", Toast.LENGTH_LONG).show();
                    }
                }
            recB2.setVisibility(View.VISIBLE);
            selB2.setVisibility(View.GONE);
            selB2.setVisibility(View.VISIBLE);
            image2.setVisibility(View.GONE);
            mControl2.setVisibility(View.GONE);
            selB2.setBackgroundResource(R.drawable.record_button);
            selB2.setClickable(false);
            swapActions("2", "record");
        }

    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list)
    {
        ArrayList<ApplicationInfo> appList = new ArrayList<>();
        for (ApplicationInfo info : list)
        {
            try
            {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName))
                {
                    appList.add(info);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return appList;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadApplications extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog progress = null;
        private Button selB, recB;
        LoadApplications(Button selB, Button recB)
        {
            this.selB = selB;
            this.recB = recB;
        }

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(RecActivity.this, null,
                    "Loading application list...");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            List<ApplicationInfo> appList = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            listAdapter = new ApplicationAdapter(RecActivity.this, R.layout.list_item, appList, app_list, selB, recB);
            return null;
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result)
        {
            list.setAdapter(listAdapter);
            app_list.show();
            progress.dismiss();
            super.onPostExecute(result);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.info:
                AlertDialog.Builder builder = new AlertDialog.Builder(RecActivity.this);
                builder.setTitle("Info!");
                builder.setCancelable(true);
                builder.setIcon(R.drawable.ic_info_black_24dp);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                builder.setMessage("1. Press on \"Select Action\" and pick action from the list. \n" +
                        "2. If You picked \"Run Installed App\" click on appeared button and select the app. \n" +
                        "3. Press \"Record Move\" then \"Start\" button and start moving the phone. \n" +
                        "4. Press \"Stop\" exact in the end of movement. \n" +
                        "5. Repeat recorded movement to use action.");
                AlertDialog info = builder.create();
                info.show();
                info.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                return true;
            case R.id.settings:
                startActivity(new Intent(RecActivity.this, SettingsActivity.class));
                return true;
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        Sensor mSensor = event.sensor;

        if (mSensor.getType() == Sensor.TYPE_ACCELEROMETER)
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
            if (temp_y == 0)
                y = event.values[1];
            temp_y = y;

            z = k * z + (1 - k) * temp_z;
            if (temp_z == 0)
                z = event.values[2];
            temp_z = z;

            double speed = (abs(x + y + z - last_x - last_y - last_z) / diffTime) * 10000;

            if (speed > SHAKE_THRESHOLD)
            {
                if (record)
                {
                    saved_x.add(x);
                    saved_y.add(y);
                    saved_z.add(z);
                    savedCount++;
                }
            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public void onBackPressed()
    {
        if(selectLayout.getVisibility() == View.VISIBLE)
            selectLayout.setVisibility(View.GONE);
        else
        {
            super.onBackPressed();
        }
    }

    private void setDefaultsInt(String key, int value, Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private void setDefaultsBoolean(String key, Boolean value, Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void setDefaultsString(String key, String value, Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private int getDefaultsInt(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 500);
    }

    private String getDefaultsString(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    private String getDefaultsAction(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "play");
    }

    private Boolean getDefaultsBoolean(String key, Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, false);
    }
}