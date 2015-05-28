package com.driven.rowan.drivenbluetooth;

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
	public int errorCount = 0;

	// Constructor
    public BTStreamReader() {
        try {
            this.mmInStream = Global.BTSocket.getInputStream();

        } catch (IOException e) {
            // We need to warn that we couldn't connect to the bluetooth device
			// TODO: figure out how to warn about failed bluetooth connection
        }
    }

    @Override
    public void run() {
		byte[] buffer = new byte[1024];
	    int bytes;
		long latestMillis = 0;

	    this.stopWorker = false;
	    int readBufferPosition = 0;

	    while (!this.stopWorker) {
			if (this.mmInStream != null && Global.BTSocket.isConnected()) {
				/* using isConnected() is not a reliable way to check whether the Bluetooth
				 * connection is still alive.
				 *
				 * Instead, every time a successful packet is received, update an internal
				 * timekeeping variable to the current millis(). Every loop gone by without
				 * data received will not update this variable, therefore we can implement a
				 * check to how long it has been since we have last received data. If this time
				 * becomes greater than a preset time, we can assume that the Bluetooth connection
				 * has been lost
				 */
				try {
					int bytesAvailable = mmInStream.available();

					if (bytesAvailable > 0) {
						byte[] packetBytes = new byte[bytesAvailable];
						bytes = mmInStream.read(packetBytes);

						// update the timekeeping variable
						latestMillis = System.currentTimeMillis();

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
					 * The Bluetooth Reconnect procedure is handled by the
					 * BluetoothDisconnectedRunnable class.
					 *
					 * Unfortunately, because of thread rules, the BluetoothDisconnectedRunnable class
					 * must be initialized by the main (UI) thread and called from here
					 */

					MainActivity.BTReconnect.fixIt(); // do NOT call BTReconnect.run()

					// Now this thread has to block until it receives notification from
					// BTReconnect. We do this by calling wait() on a Globally accessible object
					// which blocks until another thread calls notify() or notifyAll() on it.
					synchronized (Global.BTReconnectLock) {
						try {
							Global.BTReconnectLock.wait();
						} catch (InterruptedException e) {
							cancel();
						}
					}
				}
			}
	    }
    }

	public void cancel() {
		this.stopWorker = true;
	}
}
