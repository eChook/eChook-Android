package com.ben.drivenbluetooth.fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.events.PreferenceEvent;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.ben.drivenbluetooth.util.DrivenSettings;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

public class SettingsFragment 	extends PreferenceFragmentCompat
                                implements SharedPreferences.OnSharedPreferenceChangeListener
{
    static final String TAG = "Settings Fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        view.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()).getApplicationContext(), android.R.color.background_light));
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
//            updateAllPreferenceSummary();

            //Added to support BT device list generation

            final ListPreference btDevListPreference = (ListPreference) findPreference("prefBTDeviceName");
            // This is required if you don't have 'entries' and 'entryValues' in your XML - which naturally can't be hard coded for BT devices
            String[] defaultEntries = new String[]{"Is Bluetooth Enabled?"};

            btDevListPreference.setEntries(defaultEntries);
//            btDevListPreference.setDefaultValue("1");
            btDevListPreference.setEntryValues(defaultEntries);
            setListPreferenceData(btDevListPreference);

            btDevListPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setListPreferenceData(btDevListPreference);
                    return false;
                }
            });

            registerSettingsListeners();


        }catch (Exception e) {
            Log.d("eChook", "Error occurred in Settings onCreatePreference.");
            e.printStackTrace();

            final AlertDialog.Builder errorBox = new AlertDialog.Builder(this.getActivity());
            errorBox.setMessage("That wasn't supposed to happen. Please clear app cache and try again")
                    .setTitle("Oops! Sorry.");
            // AlertDialog dialog = errorBox.show();
        }

    }

    private void registerSettingsListeners(){
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

    }

    // Share function for onClick preference
    private void shareDataLog ()
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        //Open file
        File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
        Uri logFileUri = FileProvider.getUriForFile(Objects.requireNonNull(getContext()), "com.ben.drivenbluetooth.fileprovider",logFile);
        sendIntent.putExtra(Intent.EXTRA_STREAM, logFileUri);
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
                final boolean delete = logFile.delete();
                if(delete) EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.DataFileSettingChange));
            }
        });
        warningBox.setNegativeButton("Don't Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.dismiss();
            }
        });
        warningBox.show();


    }

    //OnClick callback to generate list of BT devices
    private static void setListPreferenceData(ListPreference lp) {
        BluetoothAdapter mBluetoothAdapter = null;
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }catch (Exception e){
            Log.d(TAG, "setListPreferenceData: No Access to Bluetooth Module ");
        }
        if(mBluetoothAdapter != null) {
//       Log.d("eChook", "setListPreferenceData: "+  mBluetoothAdapter.getName());
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            //Count the number of paired devices - Must be a more elegant solution!!
            int devTotalCount = 0;
            for (BluetoothDevice ignored : pairedDevices) devTotalCount++;

            Log.d("BT", "BT Device Count:" + devTotalCount);

            CharSequence[] entries = new CharSequence[devTotalCount];

            int devCount = 0;
            Global.BTDeviceNames.add(0, "null"); //pre fill the 0 index of the list to keep everything else in sync
            for (BluetoothDevice bt : pairedDevices) {
                entries[devCount] = bt.getName();
                devCount++;

            }


            lp.setEntries(entries);
            lp.setEntryValues(entries);
            lp.setSummary(Global.BTDeviceName);
        }else{// Purely for ADV testing purposes
            CharSequence[] entries = new CharSequence[1];
            entries[0] = "None";
            lp.setEntries(entries);
            lp.setEntryValues(entries);
            lp.setSummary("Bluetooth Device Not Found");
        }

    }

//    private void updatePreferenceSummary(String key) {
//        try {
//            Preference pref = findPreference(key);
//            if (pref instanceof ListPreference) {
//                ListPreference listPref = (ListPreference) pref;
//                pref.setSummary(listPref.getEntry());
//            } else if (pref instanceof EditTextPreference) {
//                EditTextPreference editTextPref = (EditTextPreference) pref;
//                pref.setSummary(editTextPref.getText());
//            }
//        } catch (Exception e) {
//            EventBus.getDefault().post(new SnackbarEvent(e));
//            e.printStackTrace();
//        }
//	}

//	private void updateAllPreferenceSummary() {
////		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
////		Map<String,?> keys = sharedPreferences.getAll();
////
////		for (Map.Entry<String, ?> entry : keys.entrySet()) {
////			//updatePreferenceSummary(entry.getKey());
////		}
//	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// UpdateLocationSetting the preference summaries
		//updatePreferenceSummary(key);

		try {
			switch (key) {
				case "prefModeSwitch":
                    //int mode = Integer.valueOf(sharedPreferences.getString("prefMode", ""));
                    int mode = sharedPreferences.getBoolean("prefModeSwitch", false)? 1:0;
					Global.Mode = Global.MODE.values()[mode];
					EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.ModeChange));
					break;
				case "prefSpeedUnits":
					int units = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("prefSpeedUnits", "")));
                    Global.SpeedUnit = Global.UNIT.values()[units];
					break;
				case "prefLocationSwitch":
					int location = sharedPreferences.getBoolean("prefLocationSwitch", false)? 1:0;
					Global.LocationStatus = Global.LOCATION.values()[location];
					EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.LocationChange));
					if(!sharedPreferences.getBoolean("prefLocationSwitch", false)&&sharedPreferences.getBoolean("prefSpeedDisplaySwitch", false)){
                        final SwitchPreference spSpeed = (SwitchPreference) findPreference("prefSpeedDisplaySwitch");
                        spSpeed.setChecked(false);
                        final SwitchPreference spDweet = (SwitchPreference) findPreference("prefDweetEnabled");
                        spDweet.setChecked(false);
                    }

					break;
                case "prefSpeedDisplaySwitch":
                    Global.dispalyGpsSpeed = sharedPreferences.getBoolean("prefSpeedDisplaySwitch", false);
                    //Now enable GPS Location if Disabled
                    if (Global.dispalyGpsSpeed) {
                        if (!sharedPreferences.getBoolean("prefLocationSwitch", false)) {

                            final AlertDialog.Builder warningBox = new AlertDialog.Builder(this.getActivity());
                            warningBox.setMessage("eChook requires Location to be enabled for this feature")
                                    .setTitle("Location Disabled");
                            warningBox.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    final SwitchPreference sp = (SwitchPreference) findPreference("prefLocationSwitch");
                                    sp.setChecked(true);
                                }
                            });
                            warningBox.setNegativeButton("Don't Enable", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    final SwitchPreference sp = (SwitchPreference) findPreference("prefSpeedDisplaySwitch");
                                    sp.setChecked(false);
                                }
                            });
                            warningBox.show();
                        }
                    }
                    break;
//				case "prefAccelerometer":
//					int accelerometer = Integer.valueOf(sharedPreferences.getString("prefAccelerometer", ""));
//					Global.Accelerometer = Global.ACCELEROMETER.values()[accelerometer];
//					MainActivity.myAccelerometer.update();
//					break;
				case "prefBTDeviceName":
                    if(!Global.BTDeviceNames.isEmpty()) {
                        Global.BTDeviceName = sharedPreferences.getString("prefBTDeviceName", "");
                        EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.BTDeviceNameChange));
                        Log.d("eChook", "Updated Global Bluetooth Name to " + Global.BTDeviceName);
                        final ListPreference lp = (ListPreference) findPreference("prefBTDeviceName");
                        lp.setSummary(Global.BTDeviceName);
                    }
					break;
				case "prefCarName":
					Global.CarName = sharedPreferences.getString("prefCarName","");
                    EventBus.getDefault().post(new PreferenceEvent(PreferenceEvent.EventType.CarNameChange));
					break;
				case "prefMotorTeeth":
                    Global.MotorTeeth = DrivenSettings.parseMotorTeeth(sharedPreferences.getString("prefMotorTeeth", ""));
                    break;
                case "prefWheelTeeth":
                    Global.WheelTeeth = DrivenSettings.parseWheelTeeth(sharedPreferences.getString("prefWheelTeeth", ""));
                    break;
                case "prefDweetEnabled":
                    Global.dweetEnabled = sharedPreferences.getBoolean("prefDweetEnabled", false);
                    break;
                case "prefDweetName":
                    Global.dweetThingName = sharedPreferences.getString("prefDweetName", "");
                    break;
                case "prefDweetLocation":
                    Global.dweetLocation = sharedPreferences.getBoolean("prefDweetEnabled", false);
                    if (Global.dweetLocation) {
                        if (!sharedPreferences.getBoolean("prefLocationSwitch", false)) {

                            final AlertDialog.Builder warningBox = new AlertDialog.Builder(this.getActivity());
                            warningBox.setMessage("eChook requires Location to be enabled for this feature")
                                    .setTitle("Location Disabled");
                            warningBox.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    final SwitchPreference sp = (SwitchPreference) findPreference("prefLocationSwitch");
                                    sp.setChecked(true);
                                }
                            });
                            warningBox.setNegativeButton("Don't Enable", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    final SwitchPreference sp = (SwitchPreference) findPreference("prefDweetEnabled");
                                    sp.setChecked(false);
                                }
                            });
                            warningBox.show();
                        }
                    }
                    break;
                case "prefEchookEnabled":
                    Global.eChookLiveEnabled = sharedPreferences.getBoolean("prefEchookEnabled", false);
                    break;
                case "prefEchookCarName":
                    Global.eChookCarName = sharedPreferences.getString("prefEchookCarName", "");
                    break;
                case "prefEchookPassword":
                    Global.eChookPassword = sharedPreferences.getString("prefEchookPassword", "");
                    break;
                case "prefCustomUrlEnabled":
                    if(sharedPreferences.getBoolean("prefCustomUrlEnabled", false)){
                        if(!URLUtil.isValidUrl(sharedPreferences.getString("prefCustomUrl", ""))){

                            final SwitchPreference sp = (SwitchPreference) findPreference("prefCustomUrlEnabled");
                            sp.setChecked(false);

                            final AlertDialog.Builder warningBox = new AlertDialog.Builder(this.getActivity());
                            warningBox.setMessage("Please enter a valid URL, including http:// or https:// before enabling this feature")
                                    .setTitle("Invalid URL");
                            warningBox.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            warningBox.show();

                        }
                    }

                    Global.customUrlEnabled = sharedPreferences.getBoolean("prefCustomUrlEnabled", false);
                    break;
                case "prefCustomUrl":
                    Global.customUrl = sharedPreferences.getString("prefCustomUrl", "");
                    final EditTextPreference lp = (EditTextPreference) findPreference("prefCustomUrl");
                    lp.setSummary(Objects.equals(Global.customUrl, "") ? "Enter URL" : Global.customUrl);
                    break;
                case "prefCustomURLUsername":
                    Global.customURLUsername = sharedPreferences.getString("prefUserDefinedURLUsername", "");
                    break;
                case "prefCustomURLPassword":
                    Global.customURLPassword = sharedPreferences.getString("prefUserDefinedURLPassword", "");
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + key);
            }
		} catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new SnackbarEvent(e));
e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		try {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e));
e.printStackTrace();
        }
	}

	@Override
	public void onPause() {
        try {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e));
e.printStackTrace();
        }
	}
}
