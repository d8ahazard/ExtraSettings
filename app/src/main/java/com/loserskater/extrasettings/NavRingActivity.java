package com.loserskater.extrasettings;

import android.app.Activity;
import android.os.Bundle;

public class NavRingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavRing shortcuts = new NavRing();
        shortcuts.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(android.R.id.content, shortcuts).commit();
        setTitle(getString(R.string.navigation_ring_title));
    }
}
