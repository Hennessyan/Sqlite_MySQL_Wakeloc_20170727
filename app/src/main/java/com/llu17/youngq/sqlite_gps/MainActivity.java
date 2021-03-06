package com.llu17.youngq.sqlite_gps;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

import static com.llu17.youngq.sqlite_gps.CollectorService.mark;


public class MainActivity extends AppCompatActivity  implements SharedPreferences.OnSharedPreferenceChangeListener{


    private String sampling_rate;
    static TextView upload_state;
    private Button MarkBusStop;
    private Switch DataCollectionSwitch, AutoCheckUploadSwitch, ManuCheckUploadSwitch;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MarkBusStop = (Button)findViewById(R.id.Mark_Bus_Stop);
        MarkBusStop.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mark[0] = true;
                Toast.makeText(getApplicationContext(), "Mark this position as bus stop", Toast.LENGTH_SHORT).show();
            }

        });

        DataCollectionSwitch = (Switch) findViewById(R.id.DataCollectionSwitch);
        AutoCheckUploadSwitch = (Switch) findViewById(R.id.AutoCheckUploadSwitch);
        ManuCheckUploadSwitch = (Switch) findViewById(R.id.ManuCheckUploadSwitch);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sampling_rate = sharedPreferences.getString(getResources().getString(R.string.sr_key_all),"1000");
        Log.e("-----ALL SR-----",""+sampling_rate);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        /*used to keep the status recorded, otherwise everytime reopen the app, switch will be off*/
        editor = getSharedPreferences("com.llu17.youngq.sqlite_gps", MODE_PRIVATE).edit();
        prefs = getSharedPreferences("com.llu17.youngq.sqlite_gps", MODE_PRIVATE);

        DataCollectionSwitch.setChecked(prefs.getBoolean("DCS_status",false));
        AutoCheckUploadSwitch.setChecked(prefs.getBoolean("ACU_status",false));
        ManuCheckUploadSwitch.setChecked(prefs.getBoolean("MCU_status",false));
        //////////
        /*===check sqlite data using "chrome://inspect"===*/
        Stetho.initializeWithDefaults(this);
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        //////////
        upload_state = (TextView)findViewById(R.id.Upload_State);
        upload_state.setVisibility(android.view.View.GONE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);

        }


//        int v = 0;
//        try {
//            v = getPackageManager().getPackageInfo("com.google.android.gms", 0 ).versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        Log.e("version:-------",""+ v);

        //Can't use:
        //ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        //android.content.ActivityNotFoundException: No Activity found to handle Intent { act=android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS }
        //Can use:
        //ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        try {
            Intent intent = new Intent();
            String packageName = this.getPackageName();
            Log.e("pachageName: ", packageName);
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!pm.isIgnoringBatteryOptimizations(packageName)){

//                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        DataCollectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                Log.e("DataCollectionSwitch","i am here !");
                editor.putBoolean("DCS_status", bChecked);
                editor.commit();
                if (bChecked) {
                    startService();
                } else {
                    stopService();
                }
            }
        });
        AutoCheckUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                Log.e("AutoCheckUploadSwitch","i am here !");
                editor.putBoolean("ACU_status", bChecked);
                editor.commit();
                if (bChecked) {
                    uploadService();
                } else {
                    breakService();
                }
            }
        });
        ManuCheckUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                Log.e("ManuCheckUploadSwitch","i am here !");
                editor.putBoolean("MCU_status", bChecked);
                editor.commit();
                if (bChecked) {
                    uploadServiceM();
                } else {
                    breakServiceM();
                }
            }
        });

    }
    /*Sampling rate menu*/
    //Add the menu to the menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sampling_rate_menu, menu);
        return true;
    }
    //When the "Settings" menu item is pressed, open SettingsActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sr_key_all))) {
            sampling_rate = sharedPreferences.getString(key, "1000");
            Log.e("-----ALL SR-----","changed: "+sampling_rate);
        }
    }
    public void startService() {        //startService(View view) 如果使用button.click去控制就要使用:startService(View v)
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        Log.e("unregister","success");

        Toast.makeText(this, "Starting the service", Toast.LENGTH_SHORT).show();
        startService(new Intent(getBaseContext(), CollectorService.class));
        startService(new Intent(getBaseContext(), Activity_Tracker.class));
    }

    // Method to stop the service
    public void stopService() {         //stopService(View view)
        Toast.makeText(this, "Stopping the service", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), CollectorService.class));
        stopService(new Intent(getBaseContext(), Activity_Tracker.class));
        stopService(new Intent(getBaseContext(), HandleActivity.class));

    }

    public void uploadService(){        //uploadService(View view)
        Toast.makeText(this, "Begin to upload data automatically", Toast.LENGTH_SHORT).show();
        startService(new Intent(getBaseContext(), UploadService.class));
    }

    public void breakService(){        //breakService(View view)
        Toast.makeText(this, "Stop to upload data automatically", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), UploadService.class));
    }

    public void uploadServiceM(){      //uploadServiceM(View view)
        Toast.makeText(this, "Begin to upload data manually", Toast.LENGTH_SHORT).show();
        startService(new Intent(getBaseContext(), UploadServiceM.class));
    }

    public void breakServiceM(){       //breakServiceM(View view)
        Toast.makeText(this, "Stop to upload data manually", Toast.LENGTH_SHORT).show();
        stopService(new Intent(getBaseContext(), UploadServiceM.class));
    }
}
