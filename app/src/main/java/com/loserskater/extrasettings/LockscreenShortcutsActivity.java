package com.loserskater.extrasettings;

import android.app.Activity;
import android.os.Bundle;

public class LockscreenShortcutsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LockscreenShortcuts shortcuts = new LockscreenShortcuts();
        shortcuts.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(android.R.id.content, shortcuts).commit();
        setTitle(getString(R.string.lockscreen_targets_message));
    }
}
