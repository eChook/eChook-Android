package com.ben.drivenbluetooth.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

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
        addPreferencesFromResource(R.xml.user_settings);
        updateAllPreferenceSummary();

        //Added to support BT device list generation

        final ListPreference btDevListPreference = (ListPreference) findPreference("prefBTDeviceName");

        // THIS IS REQUIRED IF YOU DON'T HAVE 'entries' and 'entryValues' in your XML
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
                shareDataLog ();
                return true;
            }});

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

    //OnClick callback to generate list of BT devices each time settings is launched
    protected static void setListPreferenceData(ListPreference lp) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        //Count the number of paired devices - Must be a more elegant solution...
        int devTotalCount = 0;
        for(BluetoothDevice bt : pairedDevices) {
            devTotalCount ++;
        }

        CharSequence[] entries = new CharSequence[devTotalCount];
        CharSequence[] entryValues = new CharSequence[devTotalCount];

        int devCount = 0;

        for(BluetoothDevice bt : pairedDevices) {
            entries[devCount] = bt.getName();
            entryValues[devCount] = bt.getName();

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
				case "prefMode":
					int mode = sharedPreferences.getBoolean("prefMode",false)? 1:0;// getString("prefMode", ""));
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
					MainActivity.UpdateBTCarName();
					break;
				case "prefCarName":
					Global.CarName = sharedPreferences.getString("prefCarName","");
                    MainActivity.UpdateBTCarName();
					break;
				case "prefGraphs":
					Global.EnableGraphs = Integer.valueOf(sharedPreferences.getString("prefGraphs", "")) != 0;
                    break;
                case "prefUDP":
                    Global.UDPPassword = sharedPreferences.getString("prefUDP", "");
                    Global.UDPEnabled = true;
                    if (Global.UDPEnabled) {
                        MainActivity.mUDPSender.OpenUDPSocketHandler.sendEmptyMessage(0);
                    }
                    break;
                case "prefMotorTeeth":
                    Global.MotorTeeth = DrivenSettings.parseMotorTeeth(sharedPreferences.getString("prefMotorTeeth", ""));
                    break;
                case "prefWheelTeeth":
                    Global.WheelTeeth = DrivenSettings.parseWheelTeeth(sharedPreferences.getString("prefWheelTeeth", ""));
                    break;
				default:
					break;
			}
            mListener.onSettingChanged(sharedPreferences, key);
		} catch (Exception e) {
			MainActivity.showError(e);
            ACRA.getErrorReporter().handleException(e);
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
