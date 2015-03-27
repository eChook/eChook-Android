package com.driven.rowan.drivenbluetooth;

import android.bluetooth.BluetoothSocket;

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

	    while (!this.stopWorker && this.mmInStream != null) {
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
							byte[] encodedBytes = new byte[readBufferPosition];
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
	    }
    }

	public void cancel() {
		this.stopWorker = true;
	}
}
