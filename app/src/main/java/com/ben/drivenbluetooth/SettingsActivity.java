package com.ben.drivenbluetooth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.ben.drivenbluetooth.drivenbluetooth.R;

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

			try {
				switch (key) {
				case "prefMode":
					int mode = Integer.valueOf(sharedPreferences.getString("prefMode", ""));
					Global.Mode = Global.MODE.values()[mode];
					MainActivity.myMode.setText(Global.Mode.toString());
					break;
				case "prefSpeedUnits":
					int units = Integer.valueOf(sharedPreferences.getString("prefSpeedUnits", ""));
					Global.Unit = Global.UNIT.values()[units];
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.toString();
		}
	}

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
}
