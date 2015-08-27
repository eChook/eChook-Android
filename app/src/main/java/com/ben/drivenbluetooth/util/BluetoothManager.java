package com.ben.drivenbluetooth.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

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

	private static volatile boolean isConnecting = false;

	public interface BluetoothEvents {
		void onBluetoothConnected(BluetoothSocket BTSocket);
		void onFailConnection();
		void onBluetoothDisabled();
	}

	public void setBluetoothEventsListener(BluetoothEvents BE) {
		mListener = BE;
	}

	public void unregisterListeners() {
		mListener = null;
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

    public void openBT(boolean wait) {
		if (matchingDeviceFound && !isConnecting) {
			Global.BTState = Global.BTSTATE.CONNECTING;
			Thread connectToBTDevice = new Thread(new Runnable() {
				@Override
				public void run() {
					isConnecting = true;
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
						if (mmSocket != null) {
							mmSocket = null;
							mListener.onFailConnection();
						}
					}
					isConnecting = false;
				}
			});

			connectToBTDevice.start();

			if (wait) {
				try {
					connectToBTDevice.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
    }

    public void closeBT() throws IOException {
		if (Global.BTState != Global.BTSTATE.DISCONNECTED) {
            mmSocket.close();
			Global.BTSocket.close();
            deviceConnected = false;
			Global.BTState = Global.BTSTATE.DISCONNECTED;
        }
    }

	public void reconnectBT() {
		if (Global.BTState == Global.BTSTATE.DISCONNECTED) {
			try {
				try {
					Global.BTSocket.close();
				} catch (Exception ignored) {}
				try {
					mmSocket.close();
				} catch (Exception ignored) {}
				this.openBT(true); // true waits for openBT to finish before returning
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}
}
