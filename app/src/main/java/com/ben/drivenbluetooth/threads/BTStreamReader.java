package com.ben.drivenbluetooth.threads;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import org.acra.ACRA;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Ben on 09/03/2015.
 *
 * This Stream reader assumes that the Bluetooth data is a byte array
 * starting with '{' and ending with '}'
 */
public class BTStreamReader extends Thread {
    // Member variables
    private InputStream mmInStream = null;
	private volatile boolean stopWorker = false;
	private int errorCount = 0;

	// Constructor
    public BTStreamReader() {
        try {
            this.mmInStream = Global.BTSocket.getInputStream();

        } catch (IOException e) {
            MainActivity.showMessage("Bluetooth is not connected!");
        }
    }

    @Override
    public void run() {
		byte[] buffer = new byte[1024];
	    int bytes;
		long latestMillis = System.currentTimeMillis();

	    this.stopWorker = false;
	    int readBufferPosition = 0;

	    while (!this.stopWorker) {
			try {
				int bytesAvailable = mmInStream.available();

				if (bytesAvailable > 0) {
					byte[] packetBytes = new byte[bytesAvailable];
					bytes = mmInStream.read(packetBytes);

					// UpdateLocationSetting the timekeeping variable
					latestMillis = System.currentTimeMillis();
					Global.BTState = Global.BTSTATE.CONNECTED;

					for (int i = 0; i < bytesAvailable; i++) {
						byte b = packetBytes[i];

						if (b != Global.STOPBYTE) {
							// delimiter not reached yet so continue adding to buffer
							buffer[readBufferPosition] = b;
							readBufferPosition++;
						} else {
							buffer[readBufferPosition] = b; // still need the delimiter
							// delimiter reached; flush buffer into the global queue
							byte[] encodedBytes = new byte[readBufferPosition + 1];
							System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);

							// encodedBytes now holds the data until the delimiter
							// flush it to the global queue
							Global.BTStreamQueue.add(encodedBytes);

							// post message to BTDataParser
							try {
								BTDataParser.mHandler.sendEmptyMessage(0);
							} catch (Exception e) {
								ACRA.getErrorReporter().handleException(e);
							}

							// reset the buffer pointer
							readBufferPosition = 0;
						}
					}
				}

			} catch (IOException e) {
				// Add to error count but don't stop working
				errorCount++;
			}

			if (System.currentTimeMillis() - latestMillis > Global.BT_DATA_TIMEOUT) {
				// Disconnected from bluetooth
				/* BLUETOOTH RECONNECT PROCEDURE *
				 *
				 * During the race it is expected that the driver is wearing gloves
				 * as per regulation and is therefore unable to interact with
				 * the touchscreen.Furthermore, it is likely that the device will be in a waterproof
				 * or water-resistant case, blocking any input from the driver
				 *
				 * If the bluetooth connection drops for whatever reason, the app must
				 * be able to reconnect without external input
				 *
				 *
				 */

				Global.BTState = Global.BTSTATE.DISCONNECTED;
				DisconnectedBTRoutine(); // this function will block until the connection attempt finishes
				latestMillis = System.currentTimeMillis(); // some more grace time
				try {
					// the input stream needs to be reset
					this.mmInStream = Global.BTSocket.getInputStream();
				} catch (Exception e) {
					// do nothing
				}
			}
	    }
    }

	private void DisconnectedBTRoutine() {
		try {
			MainActivity.myBluetoothManager.reconnectBT();
		} catch (Exception e) {
			e.toString();
		}
	}

	public void cancel() {
		this.stopWorker = true;
	}
}
