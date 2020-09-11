package com.ben.drivenbluetooth.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.events.PreferenceEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public final class DrivenSettings {

    private DrivenSettings() {
        // required empty constructor
    }

    // This file only initialises the settings! Use SettingsFragment.java for onChange listeners
    public static void InitializeSettings(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.user_settings, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        WheelTeeth(prefs);
        MotorTeeth(prefs);
        Mode(prefs);
        Location(prefs);
        SpeedDisplay(prefs);
        Units(prefs);
        BTDevice(prefs);
        CarName(prefs);
        dweetEnabled(prefs);
        dweetThingName(prefs);
        dweetLocation(prefs);
        echookEnabled(prefs);
        eChookCarName(prefs);
        eChookPassword(prefs);

        customUrlEnabled(prefs);
        customUrl(prefs);
        customURLUsername(prefs);
        customURLPassword(prefs);

        customDataFields(prefs);

    }

    public static void QuickChangeMode(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int mode = prefs.getBoolean("prefMode", false) ? 1 : 0;

            mode = mode == 0 ? 1 : 0; // flip it

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("prefMode", (mode != 0));
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
            Global.Mode = Global.MODE.TEST;
        }
        EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.ModeChange));
    }

    private static void Units(SharedPreferences prefs) {
        try {
            int units = Integer.parseInt(Objects.requireNonNull(prefs.getString("prefSpeedUnits", "0")));
            Global.SpeedUnit = Global.UNIT.values()[units];
            Log.d("eChook", "Units Changed");
        } catch (Exception e) {
            Global.SpeedUnit = Global.UNIT.MPH;
        }
    }

    private static void Location(SharedPreferences prefs) {
        try {
            int location = prefs.getBoolean("prefLocationSwitch", false) ? 1 : 0;
            Global.LocationStatus = Global.LOCATION.values()[location];
//            if (location == 1) {
//                //Enable or disable GPS speed preference here.
//            }
        } catch (Exception e) {
            Global.LocationStatus = Global.LOCATION.DISABLED;
        }
    }

    private static void SpeedDisplay(SharedPreferences prefs) {
        try {
            Global.dispalyGpsSpeed = prefs.getBoolean("prefSpeedDisplaySwitch", false);
            if (Global.dispalyGpsSpeed) {
                if (prefs.getBoolean("prefLocationSwitch", false)) {
                    //Use GPS is Not Enabled, enable it
                    Log.d("eChook", "Init - Attempting to enable GPS");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("prefLocationSwitch", true);
                    editor.apply();

                }
            }
        } catch (Exception e) {
            Global.dispalyGpsSpeed = false;
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


    private static void dweetEnabled(SharedPreferences prefs) {
        Global.dweetEnabled = prefs.getBoolean("prefDweetEnabled", false);
    }


    private static void dweetThingName(SharedPreferences prefs) {
        Global.dweetThingName = prefs.getString("prefDweetName", "");
    }

    private static void dweetLocation(SharedPreferences prefs) {
        Global.dweetEnabled = prefs.getBoolean("prefDweetLocation", false);
    }

    private static void echookEnabled(SharedPreferences prefs) {
        Global.eChookLiveEnabled = prefs.getBoolean("prefEchookEnabled", false);
    }

    private static void eChookCarName(SharedPreferences prefs) {
        Global.eChookCarName = prefs.getString("prefEchookCarName", "");
    }

    private static void eChookPassword(SharedPreferences prefs) {
        Global.eChookPassword = prefs.getString("prefEchookPassword", "");
    }

    private static void customUrlEnabled(SharedPreferences prefs){
        Global.customUrlEnabled = prefs.getBoolean("prefCustomUrlEnabled", false);
    }

    private static void customUrl(SharedPreferences prefs){
        Global.customUrl = prefs.getString("prefCustomUrl", "");
    }

    private static void customDataFields(SharedPreferences prefs){
        Global.sendCustomData = prefs.getBoolean("prefCustomData", false);
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

    private static void customURLUsername(SharedPreferences prefs) {
        Global.customURLUsername = prefs.getString("prefCustomURLUsername", "");
    }

    private static void customURLPassword(SharedPreferences prefs) {
        Global.customURLPassword = prefs.getString("prefCustomURLPassword", "");
    }
}
