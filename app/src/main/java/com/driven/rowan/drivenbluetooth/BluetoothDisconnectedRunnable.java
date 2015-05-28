package com.driven.rowan.drivenbluetooth;

/**
 * Created by BNAGY4 on 01/04/2015.
 */
public final class BluetoothDisconnectedRunnable implements Runnable {

	private int mNumberOfAttempts = 0;

	@Deprecated
	public void run() {
		try {
			Global.BTSocket = null;
			do {
				MainActivity.myBluetoothManager.reconnectBT();
				MainActivity.myLabel.setText("Bluetooth disconnected. Attempting to reconnect... [" + this.mNumberOfAttempts++ + "]");
			} while (!Global.BTSocket.isConnected() && this.mNumberOfAttempts <= 10);

			if (this.mNumberOfAttempts >= 10) {
				// after 10 attempts it is clear that something else is up
				MainActivity.MainActivityHandler.post(new Runnable() {
					public void run() {
						MainActivity.myLabel.setText("Bluetooth could not be reconnected. Please check hardware");
						// then cancel every thread
						MainActivity.stopButton.callOnClick();
					}
				});
			}
		} catch (Exception e) {
			e.toString();
			MainActivity.stopButton.callOnClick();
		} finally {
			// notify the blocked BTStreamReader thread to continue
			synchronized (Global.BTReconnectLock) {
				Global.BTReconnectLock.notifyAll();
			}
		}
	}

	public void fixIt() {
		MainActivity.MainActivityHandler.post(new BluetoothDisconnectedRunnable());
	}
}
