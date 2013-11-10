package com.secaucus.Shuffly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan Tseng
 * Date: 11/9/13
 * Time: 7:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShufflePreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.buttons);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}