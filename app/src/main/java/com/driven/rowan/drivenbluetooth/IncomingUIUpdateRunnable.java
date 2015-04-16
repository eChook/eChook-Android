package com.driven.rowan.drivenbluetooth;

/**
 * Created by BNAGY4 on 01/04/2015.
 */
public class IncomingUIUpdateRunnable implements Runnable {

	// for displaying the contents of the Bluetooth buffer
	private byte[] mBuffer;

	public IncomingUIUpdateRunnable(byte[] buffer) {
		this.mBuffer = buffer;
	}

	public void run() {
		try {
			MainActivity.myIncoming.setText(new String(this.mBuffer, "UTF-8"));
		} catch (Exception e) {
			// do nothing
		}
	}
}
