package com.ben.drivenbluetooth.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;

import java.util.Map;

public class SettingsFragment 	extends PreferenceFragmentCompat
								implements SharedPreferences.OnSharedPreferenceChangeListener
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//addPreferencesFromResource(R.xml.user_settings);
		//updateAllPreferenceSummary();
	}

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
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
		// UpdateLocationSetting the preference summaries
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
					MainActivity.myDrivenLocation.UpdateLocationSetting();
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
					break;
				case "prefGraphs":
					Global.EnableGraphs = Integer.valueOf(sharedPreferences.getString("prefGraphs", "")) != 0;
                    break;
                case "prefUDP":
                    Global.UDPEnabled = sharedPreferences.getString("prefUDP", "").equals(Global.UDP_PASSWORD);
                    break;
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
