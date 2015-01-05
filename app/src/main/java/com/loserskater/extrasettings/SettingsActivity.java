package com.loserskater.extrasettings;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import java.util.List;


public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_general);

        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_music_controls);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_music_controls);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof ListPreference) {
                bindPreferenceSummaryToValue(preference);
            } else if (preference instanceof CheckBoxPreference) {
                preference.setOnPreferenceClickListener(this);
            }
        }
        setDefaultValues();
    }

    private void setDefaultValues() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference.getKey() != null) {
                if (preference instanceof ListPreference) {
                    ((ListPreference) preference).setValue(Settings.System.getString(getContentResolver(), preference.getKey()));
                } else if (preference instanceof CheckBoxPreference) {
                    ((CheckBoxPreference) preference).setChecked(Settings.System.getInt(getContentResolver(), preference.getKey(), 0) == 1 ? true : false);
                } else if (preference instanceof SwitchPreference) {
                    ((SwitchPreference) preference).setChecked(Settings.System.getInt(getContentResolver(), preference.getKey(), 0) == 1 ? true : false);
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
        if (findPreference(key) instanceof SwitchPreference) {
            Settings.System.putInt(getContentResolver(), key, sharedPreferences.getBoolean(key, false) ? 1 : 0);
        } else if (findPreference(key) instanceof ListPreference) {
            Settings.System.putString(getContentResolver(), key, sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof CheckBoxPreference) {
            Settings.System.putInt(getContentResolver(), preference.getKey(), ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }
}
