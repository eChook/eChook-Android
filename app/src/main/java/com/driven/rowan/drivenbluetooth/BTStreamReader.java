package com.driven.rowan.drivenbluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Ben on 09/03/2015.
 *
 * This Stream reader assumes that the Bluetooth data is a string of the format XYYYY
 * Where X is an identifier and YYYY is the data
 *
 * ========  IDENTIFIERS  =============
 *      sYYYY :: wheel speed (raw)
 *      vYYYY :: voltage (raw)
 *      iYYYY :: current (raw)
 *      mYYYY :: motor speed (raw)
 *      bYYYY :: brake position (raw)
 *      tYYYY :: throttle position (raw)
 */
public class BTStreamReader implements Runnable {
    // Member variables
    private InputStream mmInStream = null;
	private volatile boolean stopWorker = false;
	public int errorCount = 0;

	// Constructor
    public BTStreamReader(BluetoothSocket socket) {
        try {
            this.mmInStream = socket.getInputStream();

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

	    while (!Thread.currentThread().isInterrupted() && !this.stopWorker && this.mmInStream != null) {
		    try {
			    int bytesAvailable = mmInStream.available();

			    if (bytesAvailable > 0) {
				    byte[] packetBytes = new byte[bytesAvailable];
				    bytes = mmInStream.read(packetBytes);

				    for (int i = 0; i < bytesAvailable; i++) {
					    byte b = packetBytes[i];

					    if (b != Global.STOPBYTE) {
							// delimiter not reached yet so continue adding to buffer
							buffer[readBufferPosition] = b;
							readBufferPosition++;
					    } else {
							// delimiter reached; flush buffer into the global queue
							Global.BTStreamQueue.add(buffer);
						}
				    }
			    }

		    } catch (IOException e) {
			    // Add to error count but don't stop working
				errorCount++;
		    }
	    }
    }

	public void stop() {
		this.stopWorker = true;
	}
}
