package com.driven.rowan.drivenbluetooth;

/**
 * Created by BNAGY4 on 01/04/2015.
 */
public final class BluetoothDisconnectedRunnable implements Runnable {

	private int mNumberOfAttempts = 0;

	public void run() {
		do {
			MainActivity.myLabel.setText("Bluetooth disconnected. Attempting to reconnect... [" + this.mNumberOfAttempts++ + "]");
			MainActivity.myBluetoothManager.reconnectBT();
		} while (Global.BTSocket == null);
	}

	public void fixIt() {
		MainActivity.MainActivityHandler.post(new BluetoothDisconnectedRunnable());
	}
}
