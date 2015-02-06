package com.loserskater.extrasettings;

import android.app.Activity;
import android.os.Bundle;

public class NavBarActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavBar shortcuts = new NavBar();
        shortcuts.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(android.R.id.content, shortcuts).commit();
        setTitle(getString(R.string.navigation_bar_header));
    }
}
