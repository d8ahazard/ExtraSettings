package com.loserskater.extrasettings;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

/**
 * Created by Ben on 10/8/2015.
 */
public class MainActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_display, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_lockscreen, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_navigation_bar, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_network_traffic, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_status_bar, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_volume_buttons, false);
        setContentView(R.layout.activity_main);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(android.R.id.widget_frame, new SettingsFragment());
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
