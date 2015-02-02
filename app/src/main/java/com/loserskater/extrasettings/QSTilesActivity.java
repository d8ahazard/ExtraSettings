package com.loserskater.extrasettings;

import android.app.Activity;
import android.os.Bundle;

public class QSTilesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QSTiles shortcuts = new QSTiles();
        shortcuts.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(android.R.id.content, shortcuts).commit();
        setTitle(getString(R.string.pref_header_qs));
    }
}
