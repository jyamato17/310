package com.example.myapplication.ui.setting;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.ComponentCallbacks2;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;

import com.bumptech.glide.ListPreloader;
import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.prefs.Preferences;


public class activity_preference extends PreferenceActivity implements ComponentCallbacks2 {

    private FusedLocationProviderClient fusedLocationClient;
    private final static int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        Preference button = findPreference("BUTTON");

        ActivityManager.MemoryInfo memoryInfo = getAvailableMemory();
        Long used_mem = memoryInfo.totalMem-memoryInfo.availMem;
        double mem = used_mem.doubleValue();

        mem  *= Double.valueOf(9.31 * Math.pow(10,-10));
        mem = Math.round(mem*100.0)/100.0;
        String str_sum = Double.toString(mem);
        Preference mem_pref = findPreference("CACHE");
        mem_pref.setSummary(str_sum + " GB");


        if (!memoryInfo.lowMemory) {
            // Do memory intensive work ...
            mem_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // delete the cache memory

                    return true;
                }
            });
        }


        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do

                setContentView(R.layout.popwindow);

                return true;
            }
        });

        Preference button_noti = findPreference("NOTIF");
        button_noti.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do

//                DisplayMetrics dm = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getRealMetrics(dm);
//
//                int width = dm.widthPixels;
//                int height = dm.heightPixels;
//
//                getWindow().setLayout((int)(width * .8), (int)(height * .6));

                return true;
            }
        });

        Load_setting();

    }

    // Get a MemoryInfo object for the device's current memory status.
    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    /**
     * Release memory when the UI becomes hidden or when system resources become low.
     * @param level the memory-related event that was raised.
     */
    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }

    private void Load_setting() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        boolean chk_night = sp.getBoolean("NIGHT",false);
        if (chk_night){
            setTheme(R.style.SettingStyleDarkMode);
            getListView().setBackgroundColor(Color.parseColor("#222222"));
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            setTheme(R.style.SettingStyle);
            getListView().setBackgroundColor(Color.parseColor("#ffffff"));
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        CheckBoxPreference chk_night_instant = (CheckBoxPreference)findPreference("NIGHT");
        chk_night_instant.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                boolean yes = (boolean) obj;

                if (yes) {
                    setTheme(R.style.SettingStyleDarkMode);
                    getListView().setBackgroundColor(Color.parseColor("#222222"));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else {
                    setTheme(R.style.SettingStyle);
                    getListView().setBackgroundColor(Color.parseColor("#ffffff"));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            }
        });

        boolean loc = sp.getBoolean("fetch_location", false);
        if (loc){
            fetchLocation();
        }


        CheckBoxPreference chk_location = (CheckBoxPreference)findPreference("fetch_location");
        chk_location.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                boolean yes = (boolean) obj;
                if(yes){
                    fetchLocation();
                }

                return true;
            }
        });

        ListPreference LP = (ListPreference)findPreference("ORIENTATION");
        String orien = sp.getString("ORIENTATION", "false");
        if ("1".equals(orien)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
            LP.setSummary(LP.getEntry());
        }
        else if ("2".equals(orien)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            LP.setSummary(LP.getEntry());
        }
        else if ("3".equals(orien)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            LP.setSummary(LP.getEntry());
        }

        LP.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                String items = (String) obj;
                if(preference.getKey().equals("ORIENTATION")){
                    switch (items){
                        case "1":
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
                        case "2":
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        case "3":
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            break;
                    }
                    ListPreference LPP = (ListPreference) preference;
                    LPP.setSummary(LPP.getEntries()[LPP.findIndexOfValue(items)]);
                }
                return true;
            }
        });
    }





    private void fetchLocation() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
//            // You can use the API that requires the permission.
//            fusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // Got last known location. In some rare situations this can be null.
//                            if (location != null) {
//                                // Logic to handle location object
//                                Double latitude = location.getLatitude();
//                                Double longitude = location.getLongitude();
//
////                                user_location.setText("Latitude = " + latitude + "\nLongitude = " + longitude);
//
//                            }
//                        }
//                    });

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity_preference.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            new AlertDialog.Builder(this)
                    .setTitle("require location permission")
                    .setMessage("you have to give this permission to access the location")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity_preference.this,
                                    new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_REQUEST_CODE
                            );
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        } else {
            // You can directly ask for the permission.
            ActivityCompat.requestPermissions(activity_preference.this,
                    new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_REQUEST_CODE
            );
        }

    }

    @Override
    protected void onResume() {
        Load_setting();
        super.onResume();
    }

}
