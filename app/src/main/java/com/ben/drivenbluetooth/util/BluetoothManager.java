package com.ben.drivenbluetooth.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Rowan on 06/03/2015.
 */
public final class BluetoothManager extends MainActivity {

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mmSocket;
    private static BluetoothDevice mmDevice;

	private boolean deviceConnected = false;
    private boolean matchingDeviceFound = false;

    String TAG = "DBDebug - BtManager";

    //_____________________________________________________________________________FIND BT
    public void findBT() {

		Log.d(TAG, "Entering FindBT");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "FindBT before If");

        mBluetoothAdapter.cancelDiscovery();

        if(mBluetoothAdapter == null) {
            Log.d(TAG, "Entering BT Adapter == null");
        }

        if(!mBluetoothAdapter.isEnabled()) {

            Log.d(TAG, "Attempting to enable BT Adapter");
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Log.d(TAG, "Attempting to get bonded devices");

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        Log.d(TAG, "Got bonded devices");

        Log.d(TAG, "Returned " + pairedDevices.size() + " Devices");

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {

                Log.d(TAG, "Checking Name "+ device.getName());

                if(device.getName().equals(Global.BTDeviceName)) { //TODO Handle scenario where device is not paired to phone

                    Log.d(TAG, "Device Matching Name Found");
                    matchingDeviceFound = true;
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.d(TAG, "Bluetooth Device Found " + mmDevice.getName());
    }

    //_____________________________________________________________________________OPEN BT
    //TODO Set this to open in a separate thread
    public void openBT() throws IOException {
        if(matchingDeviceFound) {

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard //SerialPortService ID
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

			mBluetoothAdapter.cancelDiscovery();
			Global.BTSocket = mmSocket;
			Global.BTSocket.connect();

            deviceConnected = true;
			Global.BTState = Global.BTSTATE.CONNECTED;
        } else {
            MainActivity.showMessage("Could not find a matching device", Toast.LENGTH_LONG);
        }
    }

    //_____________________________________________________________________________CLOSE BT
    public void closeBT() throws IOException {
		if (deviceConnected) {
            mmSocket.close();
			Global.BTSocket.close();
            deviceConnected = false;
			Global.BTState = Global.BTSTATE.DISCONNECTED;
        }
    }

	public boolean reconnectBT() {
		try {
			this.openBT();

			// if the above succeeds, return true
			return true;
		} catch (Exception e) {
			e.getMessage();
			return false;
		}
	}
}
