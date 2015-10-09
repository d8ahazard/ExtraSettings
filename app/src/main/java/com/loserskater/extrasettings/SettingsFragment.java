package com.loserskater.extrasettings;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import eu.chainfire.libsuperuser.Shell;


public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceClickListener, OnPreferenceChangeListener {

    private SharedPreferences sharedPreferences;



    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_display);

        addPreferencesFromResource(R.xml.pref_status_bar);

        addPreferencesFromResource(R.xml.pref_navigation_bar);

        addPreferencesFromResource(R.xml.pref_volume_buttons);

        addPreferencesFromResource(R.xml.pref_lockscreen);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceScreen) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); j++) {
                    Preference subPreference = preferenceGroup.getPreference(j);
                    if (subPreference instanceof PreferenceCategory) {
                        PreferenceGroup subPreferenceGroup = (PreferenceGroup) subPreference;
                        for (int k = 0; k < subPreferenceGroup.getPreferenceCount(); k++) {
                            Preference anotherSubPreference = subPreferenceGroup.getPreference(k);
                            if (anotherSubPreference instanceof ListPreference) {
                                bindPreferenceSummaryToValue(anotherSubPreference);
                            } else {
                                anotherSubPreference.setOnPreferenceClickListener(this);
                            }
                        }
                    }
                    if (subPreference instanceof ListPreference) {
                        bindPreferenceSummaryToValue(subPreference);
                    } else {
                        subPreference.setOnPreferenceClickListener(this);
                    }
                }
            }
        }


    }


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.matches(getString(R.string.pref_key_heads_up_enabled))) {
            Settings.Global.putInt(getActivity().getContentResolver(), key, sharedPreferences.getBoolean(key, false) ? 1 : 0);
        } else if (key.matches(getString(R.string.pref_key_density))) {
            setLcdDensity(sharedPreferences.getString(key, ""));
        } else {
            if (findPreference(key) instanceof SwitchPreference) {
                Settings.System.putInt(getActivity().getContentResolver(), key, sharedPreferences.getBoolean(key, false) ? 1 : 0);
            } else if (findPreference(key) instanceof ListPreference) {
                Settings.System.putString(getActivity().getContentResolver(), key, sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey() != null) {
            if (preference.getKey().matches(getString(R.string.pref_key_heads_up_test))) {
                showNotification();
            }
        }
        return false;
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ic_launcher)
                                //Should move these to strings.xml but I doubt I'll need translations so I'll keep it this way for now.
                        .setContentTitle("TEST")
                        .setContentText("Testing heads up.")
                        .setContentIntent(PendingIntent.getActivity(getActivity(), 0, new Intent(), 0))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND);
        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        int mId = 0;
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private void setLcdDensity(String newDensity) {
        Shell.SU.run("mount -o remount,rw /system /system");
        Shell.SU.run("busybox sed -i 's/ro.sf.lcd_density=[0-9][0-9][0-9]/ro.sf.lcd_density="
                + newDensity + "/g' /system/build.prop");
        Shell.SU.run("mount -o remount,ro /system /system");
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage("You must reboot in order for these changes to take effect.")
                .setPositiveButton("Later", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Now", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PowerManager pm = (PowerManager) getActivity()
                                .getSystemService(Context.POWER_SERVICE);
                        pm.reboot("Resetting density");
                    }
                });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }



    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().matches(getString(R.string.pref_key_battery_save_mode_color))) {
                        int hex = Integer.valueOf(String.valueOf(newValue));
                        Settings.System.putInt(getActivity().getContentResolver(),
                                        getString(R.string.pref_key_battery_save_mode_color), hex);
                        return true;
                    }
                return false;
    }
}
