package com.hatchtrack.app;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Globals.DEBUG) {
            Log.d(TAG, "onCreate()");
        }
        // Display the fragment as the main content.
        SettingsFrag settingsFrag = new SettingsFrag();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFrag)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
