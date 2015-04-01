package com.driven.rowan.drivenbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

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

	//Constants
	private final static int BTREQUEST = 1;

    String TAG = "DBDebug - BtManager";


    //_____________________________________________________________________________FIND BT
    void findBT() {

		Log.d(TAG, "Entering FindBT");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "FindBT before If");

        mBluetoothAdapter.cancelDiscovery();

        if(mBluetoothAdapter == null) {
            Log.d(TAG, "Entering BT Adapter == null");
            myLabel.setText("No bluetooth adapter available");
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

                if(device.getName().equals("JaguarJ4-62D8")) { //TODO Handle scenario where device is not paired to phone

                    Log.d(TAG, "Device Matching Name Found");
                    matchingDeviceFound = true;
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.d(TAG, "Bluetooth Device Found " + this.mmDevice.getName());
        myLabel.setText("Bluetooth Device Found: " + this.mmDevice.getName());
    }

    //_____________________________________________________________________________OPEN BT
    //TODO Set this to open in a separate thread
    void openBT() throws IOException {
        if(matchingDeviceFound) {
            Log.d(TAG, "Attempting to open BT Device");

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard //SerialPortService ID
            Log.d(TAG, "Connecting Socket Service to device");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

			Global.BTSocket = mmSocket;
			Global.BTSocket.connect();

            deviceConnected = true;
            myLabel.setText("Bluetooth Connection Open - Please press 'Start'");
        } else {
            Log.d(TAG, "No matching device found to open");
        }
    }

    //_____________________________________________________________________________CLOSE BT
    void closeBT() throws IOException {

		if (deviceConnected) {
            mmSocket.close();
			Global.BTSocket.close();
            myLabel.setText("Bluetooth Closed");
            deviceConnected = false;
        }
    }

	public boolean reconnectBT() {
		try {
			this.findBT();
			this.openBT();

			// if the above succeeds, return true
			return true;
		} catch (Exception e) {
			showMessage(e.getMessage().toString());
			return false;
		}
	}
}
