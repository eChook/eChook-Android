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

	    this.stopWorker = false;
	    int readBufferPosition = 0;

	    while (!this.stopWorker) {
			if (this.mmInStream != null && Global.BTSocket.isConnected()) {
				// still connected which is good
				try {
					int bytesAvailable = mmInStream.available();

					if (bytesAvailable > 0) {
						byte[] packetBytes = new byte[bytesAvailable];
						bytes = mmInStream.read(packetBytes);

						MainActivity.MainActivityHandler.post(new IncomingUIUpdateRunnable(buffer));

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

								// TODO: maybe log the raw message?

								// reset the buffer pointer
								readBufferPosition = 0;
							}
						}
					}

				} catch (IOException e) {
					// Add to error count but don't stop working
					errorCount++;
				}
			} else {
				// Disconnected from bluetooth
				/* BLUETOOTH RECONNECT PROCEDURE *
				 *
				 * During the race it is expected that the driver is wearing gloves
				 * as per regulation and is therefore unable to interact with
				 * the touchscreen.
				 *
				 * Furthermore, it is likely that the device will be in a waterproof
				 * or water-resistant case, blocking any input
				 *
				 * If the bluetooth connection drops for whatever reason, the app must
				 * be able to reconnect without input
				 *
				 * The procedure:
				 * 1. Notify the MainActivity that the Bluetooth Socket is disconnected
				 *
				 *
				 */
			}
	    }
    }

	public void cancel() {
		this.stopWorker = true;
	}
}
