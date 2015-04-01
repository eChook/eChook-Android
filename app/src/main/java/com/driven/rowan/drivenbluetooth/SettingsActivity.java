package com.driven.rowan.drivenbluetooth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by BNAGY4 on 01/04/2015.
 */
public class SettingsActivity 	extends PreferenceActivity
								implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.user_settings);
		PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}
}
