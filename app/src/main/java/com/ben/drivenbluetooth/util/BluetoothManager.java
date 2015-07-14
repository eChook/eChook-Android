package com.ben.drivenbluetooth.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public final class BluetoothManager {

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mmSocket;
    private static BluetoothDevice mmDevice;

	private static BluetoothEvents mListener;

	private boolean deviceConnected = false;
    private boolean matchingDeviceFound = false;

	public interface BluetoothEvents {
		void onBluetoothConnected(BluetoothSocket BTSocket);
		void onFailConnection();
		void onBluetoothDisabled();
	}

	public void setBluetoothEventsListener(BluetoothEvents BE) {
		mListener = BE;
	}

    public void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.cancelDiscovery();

        if(!mBluetoothAdapter.isEnabled()) {
            mListener.onBluetoothDisabled();
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			if(!mBluetoothAdapter.isEnabled()) {
				MainActivity.showMessage("Bluetooth adapter not enabled");
				return;
			}
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals(Global.BTDeviceName)) {
                    matchingDeviceFound = true;
                    mmDevice = device;
                    return;
                }
            }
        }

		// if we have reached this point then we could not find a device
		MainActivity.showMessage(Global.BTDeviceName + " is not paired with this phone. Please open Settings and pair the device first");
    }

    public void openBT() {
		if (matchingDeviceFound) {
			Global.BTState = Global.BTSTATE.CONNECTING;
			new Thread(new Runnable() {
				@Override
				public void run() {
					UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard //SerialPortService ID
					try {
						mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
					} catch (IOException e) {
						e.printStackTrace();
					}

					mBluetoothAdapter.cancelDiscovery();

					try {
						mmSocket.connect();

						// if we have reached here then the connection was successful
						mListener.onBluetoothConnected(mmSocket);
					} catch (IOException e) {
						try {
							mmSocket.close();
						} catch (IOException ignored) {}
						mListener.onFailConnection();
					}
				}
			}).start();
		}
    }

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
