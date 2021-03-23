package com.ben.drivenbluetooth.threads;

import android.util.Log;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.events.SnackbarEvent;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.greenrobot.eventbus.EventBus;

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
            EventBus.getDefault().post(new SnackbarEvent("Bluetooth is not connected!"));
        }
    }

    @Override
    public void run() {
		byte[] buffer = new byte[1024];
		long latestMillis = System.currentTimeMillis();
		int bytes;

	    this.stopWorker = false;
	    int readBufferPosition = 0;

	    while (!this.stopWorker) {
			try {
//				Log.d("eChook", "Entering Stream Reader Loop");

				int bytesAvailable = mmInStream.available();
//				Log.d("eChook", "Bytes Available: " + bytesAvailable);

				//Is there too much data in the buffer?
//                if (bytesAvailable > 1024) {
//                    EventBus.getDefault().post(new SnackbarEvent("Too Much BT Data!"));
//                    mmInStream.skip(bytesAvailable - 1024); //This should stop the buffer overrun seen by some teams
//                }

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
//							Log.d("eChook", "Buffer position: "+readBufferPosition+" Data: "+b);

							try {
                                buffer[readBufferPosition] = b;
                            } catch(OutOfRangeException e){
//                                Log.d("eChook", "Stream Reader Buffer out of range");
                            }

                            if(readBufferPosition == buffer.length-1){
//                                Log.d("eChook", "Buffer Position out of range, resetting buffer");

                                buffer = new byte[1024];
                                readBufferPosition = 0;
                            }else {
                                readBufferPosition++;
                            }

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
								EventBus.getDefault().post(new SnackbarEvent(e));
                                e.printStackTrace();
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
