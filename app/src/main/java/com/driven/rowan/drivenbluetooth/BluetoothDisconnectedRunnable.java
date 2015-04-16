package com.driven.rowan.drivenbluetooth;

/**
 * Created by BNAGY4 on 01/04/2015.
 */
public final class BluetoothDisconnectedRunnable implements Runnable {

	private int mNumberOfAttempts = 0;

	@Deprecated
	public void run() {
		do {
			MainActivity.myLabel.setText("Bluetooth disconnected. Attempting to reconnect... [" + this.mNumberOfAttempts++ + "]");
			MainActivity.myBluetoothManager.reconnectBT();
		} while (Global.BTSocket == null && this.mNumberOfAttempts < 10);

		if (this.mNumberOfAttempts >= 10) {
			// after 10 attempts it is clear that something else is up
			MainActivity.MainActivityHandler.post(new Runnable() {
				public void run() {
					MainActivity.myLabel.setText("Bluetooth could not be reconnected. Please check hardware");
				}
			});
		}
	}

	public void fixIt() {
		MainActivity.MainActivityHandler.post(new BluetoothDisconnectedRunnable());
	}
}
