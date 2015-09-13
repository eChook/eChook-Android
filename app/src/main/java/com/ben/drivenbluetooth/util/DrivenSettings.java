package com.ben.drivenbluetooth.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;

public final class DrivenSettings {

	private DrivenSettings() {
		// required empty constructor
	}

	public static void InitializeSettings() {
		PreferenceManager.setDefaultValues(MainActivity.getAppContext(), R.xml.user_settings, false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());

		Mode(prefs);
		Location(prefs);
		Units(prefs);
		BTDevice(prefs);
		CarName(prefs);
	}

	public static void QuickChangeMode() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());
		int mode = Integer.valueOf(prefs.getString("prefMode", ""));

		mode = mode == 0 ? 1 : 0; // magic trick!

		prefs.edit().putString("prefMode", mode == 0 ? "Demo" : "Race").apply();

		Global.Mode = Global.MODE.values()[mode];
		MainActivity.myMode.setText(Global.MODE.values()[mode].name());
	}

	private static void Mode(SharedPreferences prefs) {
		try {
			int mode = Integer.valueOf(prefs.getString("prefMode", ""));
			Global.Mode = Global.MODE.values()[mode];
			MainActivity.myMode.setText(Global.MODE.values()[mode].name());
		} catch (Exception e) {
			Global.Mode = Global.MODE.DEMO;
			MainActivity.myMode.setText("DEMO");
		}
	}

	private static void Units(SharedPreferences prefs) {
		try {
			int units = Integer.valueOf(prefs.getString("prefSpeedUnits", ""));
			Global.Unit = Global.UNIT.values()[units];
		} catch (Exception e) {
			Global.Unit = Global.UNIT.MPH;
		}
	}

	private static void Location(SharedPreferences prefs) {
		try {
			int location = Integer.valueOf(prefs.getString("prefLocation", ""));
			Global.LocationStatus = Global.LOCATION.values()[location];
		} catch (Exception e) {
			Global.LocationStatus = Global.LOCATION.DISABLED;
		}
	}

	private static void BTDevice(SharedPreferences prefs) {
		try {
			Global.BTDeviceName = prefs.getString("prefBTDeviceName", "");
		} catch (Exception e) {
			// probably not needed
		}
	}

	private static void CarName(SharedPreferences prefs) {
		try {
			Global.CarName = prefs.getString("prefCarName", "");
		} catch (Exception e) {
			// probably not needed
		}
	}
}
