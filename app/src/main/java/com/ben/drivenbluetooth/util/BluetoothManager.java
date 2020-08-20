package com.ben.drivenbluetooth.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.events.SnackbarEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public final class BluetoothManager {

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mmSocket;
    private static BluetoothDevice mmDevice;

	private static BluetoothEvents mListener;

    private boolean matchingDeviceFound = false;

	private static volatile boolean isConnecting = false;

	public interface BluetoothEvents {
		void onBluetoothConnected(BluetoothSocket BTSocket);
        void onBluetoothDisconnected();
		void onFailConnection();
        void onBluetoothConnecting();
		void onBluetoothDisabled();
        void onBluetoothReconnecting();
	}

    /** Registers the listener for BluetoothEvents
     *
     * @param BE    The BluetoothEvents interface listener
     */
	public void setBluetoothEventsListener(BluetoothEvents BE) {
		mListener = BE;
	}

    /** Unregisters any registered BluetoothEvents listeners */
	public void unregisterListeners() {
		mListener = null;
	}

    /** Attempts to find the Bluetooth device with the name specified in settings */
    public void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.cancelDiscovery();

        if(!mBluetoothAdapter.isEnabled()) {
            mListener.onBluetoothDisabled();
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			if(!mBluetoothAdapter.isEnabled()) {
				EventBus.getDefault().post(new SnackbarEvent("Bluetooth adapter not enabled"));
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
		EventBus.getDefault().post(new SnackbarEvent(Global.BTDeviceName + " is not paired with this phone. Please open Settings and pair the device first"));
    }

    /**
	 * Attempts to open a connection to the Bluetooth device by using a background thread.
	 *
	 * @param wait If set to true, this function will block until the background thread finishes. This is used for the Bluetooth reconnect routine if the connection fails during a race
	 */

	public void openBT(boolean wait) {
		if (matchingDeviceFound && !isConnecting) {
			Global.BTState = Global.BTSTATE.CONNECTING;
			if (wait) {
				// usually wait is only true when we are in a reconnect loop...
				mListener.onBluetoothReconnecting();
			} else {
				mListener.onBluetoothConnecting();
			}
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

    /** Closes the Bluetooth connection if open
     *
	 *  throws IOException
     */
    public void closeBT() throws IOException {
		if (Global.BTState != Global.BTSTATE.DISCONNECTED) {
            mmSocket.close();
			Global.BTSocket.close();
			Global.BTState = Global.BTSTATE.DISCONNECTED;
            mListener.onBluetoothDisconnected();
        }
    }

    /** The routine to reconnect to Bluetooth if it has become unresponsive or disconnected during a race */
	public void reconnectBT() {
        mListener.onBluetoothReconnecting();
        Global.BTReconnectAttempts++;
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
