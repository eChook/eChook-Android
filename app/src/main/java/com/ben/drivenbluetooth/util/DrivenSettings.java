package com.ben.drivenbluetooth.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.events.PreferenceEvent;

import org.greenrobot.eventbus.EventBus;

public final class DrivenSettings {

private DrivenSettings() {
								// required empty constructor
}

public static void InitializeSettings(Context context) {
								PreferenceManager.setDefaultValues(context, R.xml.user_settings, false);
								SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

								WheelTeeth(prefs);
								MotorTeeth(prefs);
								Mode(prefs);
								Location(prefs);
								Units(prefs);
								BTDevice(prefs);
								CarName(prefs);
								Graphs(prefs);
								dweetEnabled(prefs);
								echookEnabled(prefs);
								eChookCarName(prefs);
								eChookPassword(prefs);
								dweetThingName(prefs);
}

public static void QuickChangeMode(Context context) {
								try {
																SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
																int mode = prefs.getBoolean("prefMode", false) ? 1 : 0;

																mode = mode == 0 ? 1 : 0; // flip it

																SharedPreferences.Editor editor = prefs.edit();
																editor.putBoolean("prefMode", (mode != 0) );
																editor.apply();

																Global.Mode = Global.MODE.values()[mode];
//			MainActivity.myMode.setText(Global.MODE.values()[mode].name());
																EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.ModeChange));
								} catch (Exception e) {
																e.printStackTrace();
								}
}

private static void MotorTeeth(SharedPreferences prefs) {
								try {
																Global.MotorTeeth = parseMotorTeeth(prefs.getString("prefMotorTeeth", ""));
								} catch (Exception e) {
																// probably not needed
								}
}

private static void WheelTeeth(SharedPreferences prefs) {
								try {
																Global.WheelTeeth = parseWheelTeeth(prefs.getString("prefWheelTeeth", ""));
								} catch (Exception e) {
																// probably not needed
								}
}

private static void Mode(SharedPreferences prefs) {
								try {
																int mode = prefs.getBoolean("prefModeSwitch", false) ? 1 : 0;
																Global.Mode = Global.MODE.values()[mode];
								} catch (Exception e) {
																Global.Mode = Global.MODE.DEMO;
								}
								EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.ModeChange));
}

private static void Units(SharedPreferences prefs) {
								try {
																int units = Integer.valueOf(prefs.getString("prefSpeedUnits", "0"));
																Global.Unit = Global.UNIT.values()[units];
								} catch (Exception e) {
																Global.Unit = Global.UNIT.MPH;
								}
}

private static void Location(SharedPreferences prefs) {
								try {
																int location = prefs.getBoolean("prefLocationSwitch", false) ? 1 : 0;
																Global.LocationStatus = Global.LOCATION.values()[location];
								} catch (Exception e) {
																Global.LocationStatus = Global.LOCATION.DISABLED;
								}
}

private static void BTDevice(SharedPreferences prefs) {
								try {
																Global.BTDeviceName = prefs.getString("prefBTDeviceName", "");
																EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.BTDeviceNameChange));
								} catch (Exception e) {
																// probably not needed
								}
}

private static void CarName(SharedPreferences prefs) {
								try {
																Global.CarName = prefs.getString("prefCarName", "");
																EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.CarNameChange));
								} catch (Exception e) {
																// probably not needed
								}
}

private static void Graphs(SharedPreferences prefs) {
								try {
																Global.EnableGraphs = prefs.getBoolean("prefGraphsSwitch", false);
								} catch (Exception e) {
																// probably not needed
								}
}

private static void dweetEnabled(SharedPreferences prefs) {
								Global.dweetEnabled = prefs.getBoolean("prefDweetEnabled", false);
}


private static void dweetThingName(SharedPreferences prefs){
								Global.dweetThingName = prefs.getString("prefDweetName", "");
}

private static void echookEnabled(SharedPreferences prefs) {
								Global.eChookLiveEnabled = prefs.getBoolean("prefEchookEnabled", false);
}

private static void eChookCarName(SharedPreferences prefs){
								Global.eChookCarName = prefs.getString("prefEchookCarName", "");
}

private static void eChookPassword(SharedPreferences prefs){
								Global.eChookPassword = prefs.getString("prefEchookPassword", "");
}


public static int[] parseWheelTeeth(String wheelTeethString) {
								wheelTeethString = wheelTeethString.replaceAll("\\s+", ""); // remove spaces
								String[] wheelTeethStringArray = wheelTeethString.split(",");

								int[] wheelTeeth = new int[wheelTeethStringArray.length];

								for (int i = 0; i < wheelTeethStringArray.length; i++) {
																wheelTeeth[i] = Integer.parseInt(wheelTeethStringArray[i]);
								}

								return wheelTeeth;
}

public static int parseMotorTeeth(String motorTeethString) {
								motorTeethString = motorTeethString.replaceAll("\\s+", ""); // remove spaces
								return Integer.parseInt(motorTeethString);
}
}
