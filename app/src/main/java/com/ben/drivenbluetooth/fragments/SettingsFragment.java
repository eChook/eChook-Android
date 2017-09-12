package com.ben.drivenbluetooth.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;

import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.Intent;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.util.DrivenSettings;

import org.acra.ACRA;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsFragment 	extends PreferenceFragmentCompat
								implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private SettingsInterface mListener;

    public interface SettingsInterface {

        void onSettingChanged(SharedPreferences sharedPreferences, String key);
    }

    public void setSettingsListener(SettingsInterface settingsInterface) {
        mListener = settingsInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(MainActivity.getAppContext(), android.R.color.background_light));
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        try{
            try {
                addPreferencesFromResource(R.xml.user_settings);
            }catch (Exception e) {
                e.printStackTrace();

            }
            updateAllPreferenceSummary();

            //Added to support BT device list generation

            final ListPreference btDevListPreference = (ListPreference) findPreference("prefBTDeviceName");

            // This is required if you don't have 'entries' and 'entryValues' in your XML - which can't be hard coded for BT devices
            setListPreferenceData(btDevListPreference);


            btDevListPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setListPreferenceData(btDevListPreference);
                    return false;
                }
            });

            //On click event for Sharing Log
            Preference sharePref = findPreference("prefShareLog");
            sharePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    shareDataLog();
                    return true;
                }
            });

            //On click event for Deleting Log
            Preference deletePref = findPreference("prefClearLog");
            deletePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    clearDataLog();
                    return true;
                }
            });

            //Disable live data option if password incorrect
            Preference dataPref = findPreference("prefUdpEnabled");
            if (!Global.UDPPassword.equals("eChookLiveData")) {
                dataPref.setEnabled(false);
                Global.UDPEnabled = false;
            }
        }catch (Exception e) {
            e.printStackTrace();

            final AlertDialog.Builder errorBox = new AlertDialog.Builder(this.getActivity());
            errorBox.setMessage("That wasn't supposed to happen. Please clear app cache and try again")
                    .setTitle("Oops! Sorry.");
            AlertDialog dialog = errorBox.show();
        }

    }

    // Share function for onClick preference
    private void shareDataLog ()
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        //Open file
        File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    //OnClick callback for deleting data log
    private void clearDataLog()
    {
        final AlertDialog.Builder warningBox = new AlertDialog.Builder(this.getActivity());
        warningBox.setMessage("This will delete all logged data")
                .setTitle("Are you sure?");
        warningBox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
                logFile.delete();
                MainActivity.UpdateDataFileInfo();
            }
        });
        warningBox.setNegativeButton("Don't Delete", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
        }
    });
        AlertDialog dialog = warningBox.show();


    }

    //OnClck callback to generate list of BT devices
    protected static void setListPreferenceData(ListPreference lp) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        //Count the number of paired devices - Must be a more elegant solution!! TODO
        int devTotalCount = 0;
        for(BluetoothDevice bt : pairedDevices) {
            devTotalCount ++;
        }

        CharSequence[] entries = new CharSequence[devTotalCount];
        CharSequence[] entryValues = new CharSequence[devTotalCount];

        int devCount = 0;
        Global.BTDeviceNames.add(0, "null"); //pre fill the 0 index of the list to keep everything else in sync
        for(BluetoothDevice bt : pairedDevices) {
            entries[devCount] = bt.getName();
            entryValues[devCount] = String.format("%d",devCount+1);
            Global.BTDeviceNames.add(devCount+1,bt.getName());
            devCount ++;

        }


        lp.setEntries(entries);
        lp.setDefaultValue("1");
        lp.setEntryValues(entryValues);


    }

    private void updatePreferenceSummary(String key) {
        try {
            Preference pref = findPreference(key);
            if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                pref.setSummary(editTextPref.getText());
            }
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(e);
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
				case "prefModeSwitch":
                    //int mode = Integer.valueOf(sharedPreferences.getString("prefMode", ""));
                    int mode = sharedPreferences.getBoolean("prefModeSwitch", false)? 1:0;
					Global.Mode = Global.MODE.values()[mode];
					MainActivity.myMode.setText(Global.Mode.toString());
					break;
				case "prefSpeedUnits":
					int units = Integer.valueOf(sharedPreferences.getString("prefSpeedUnits", ""));
					Global.Unit = Global.UNIT.values()[units];
					break;
				case "prefLocationSwitch":
					int location = sharedPreferences.getBoolean("prefLocationSwitch", false)? 1:0;
					Global.LocationStatus = Global.LOCATION.values()[location];
					MainActivity.myDrivenLocation.UpdateLocationSetting();
					break;
				case "prefAccelerometer":
					int accelerometer = Integer.valueOf(sharedPreferences.getString("prefAccelerometer", ""));
					Global.Accelerometer = Global.ACCELEROMETER.values()[accelerometer];
					MainActivity.myAccelerometer.update();
					break;
				case "prefBTDeviceName":
                    if(!Global.BTDeviceNames.isEmpty()) {
                        int nameID = Integer.parseInt(sharedPreferences.getString("prefBTDeviceName", ""));
                        Global.BTDeviceName = Global.BTDeviceNames.get(nameID);
                        MainActivity.UpdateBTCarName();
                    }
					break;
				case "prefCarName":
					Global.CarName = sharedPreferences.getString("prefCarName","");
                    MainActivity.UpdateBTCarName();
					break;
				case "prefGraphsSwitch":
					Global.EnableGraphs = sharedPreferences.getBoolean("prefGraphsSwitch", false);
                    break;
                case "prefUDP":
                    Global.UDPPassword = sharedPreferences.getString("prefUDP", "");
                    Preference pref = findPreference("prefUdpEnabled");
                    if(Global.UDPPassword.equals("eChookLiveData"))
                    {
                        Toast.makeText(this.getContext(), "Password Correct :)", Toast.LENGTH_SHORT).show();
                        pref.setEnabled(true);

                    } else{

                        Toast.makeText(this.getContext(), "Nice Guess... try again :p", Toast.LENGTH_SHORT).show();

                        pref.setEnabled(false);
                    }

                    break;
                case "prefMotorTeeth":
                    Global.MotorTeeth = DrivenSettings.parseMotorTeeth(sharedPreferences.getString("prefMotorTeeth", ""));
                    break;
                case "prefWheelTeeth":
                    Global.WheelTeeth = DrivenSettings.parseWheelTeeth(sharedPreferences.getString("prefWheelTeeth", ""));
                    break;
                case "prefUdpEnabled":
                    if(sharedPreferences.getBoolean("prefUdpEnabled", false))
                    {

                    }else{
                        Global.UDPEnabled = false;
                    }

				default:
					break;
			}
            mListener.onSettingChanged(sharedPreferences, key);
		} catch (Exception e) {
			MainActivity.showError(e);
            e.printStackTrace();
            ACRA.getErrorReporter().handleException(e);
		}
	}

	@Override
	public void onResume() {
		try {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }catch (Exception e) {

        }
	}

	@Override
	public void onPause() {
        try {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }catch (Exception e) {
        }
	}
}
