package com.ben.drivenbluetooth.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;

import java.util.Map;

public class SettingsFragment 	extends PreferenceFragment
								implements SharedPreferences.OnSharedPreferenceChangeListener
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());
		prefs.edit().remove("prefMode").commit();
		addPreferencesFromResource(R.xml.user_settings);
		updateAllPreferenceSummary();
	}

	private void updatePreferenceSummary(String key) {
		Preference pref = findPreference(key);
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		} else if (pref instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) pref;
			pref.setSummary(editTextPref.getText());
		}
	}

	private void updateAllPreferenceSummary() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());
		Map<String,?> keys = sharedPreferences.getAll();

		for (Map.Entry<String, ?> entry : keys.entrySet()) {
			updatePreferenceSummary(entry.getKey());
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// update the preference summaries
		updatePreferenceSummary(key);

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
				case "prefLocation":
					int location = Integer.valueOf(sharedPreferences.getString("prefLocation", ""));
					Global.LocationStatus = Global.LOCATION.values()[location];
					MainActivity.myDrivenLocation.update();
					break;
				case "prefAccelerometer":
					int accelerometer = Integer.valueOf(sharedPreferences.getString("prefAccelerometer", ""));
					Global.Accelerometer = Global.ACCELEROMETER.values()[accelerometer];
					MainActivity.myAccelerometer.update();
					break;
				case "prefBTDeviceName":
					Global.BTDeviceName = sharedPreferences.getString("prefBTDeviceName", "");
					break;
				case "prefCarName":
					Global.CarName = sharedPreferences.getString("prefCarName","");
				default:
					break;
			}
		} catch (Exception e) {
			MainActivity.showError(e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}
}
