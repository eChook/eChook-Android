package com.driven.rowan.drivenbluetooth;

/**
 * Created by BNAGY4 on 01/04/2015.
 */
public final class BluetoothDisconnectedThread extends Thread {

	private boolean stopWorker = false;

	public void run() {
		while (!this.stopWorker) {
			try {
				Global.BTSocket = null;
				do {
					MainActivity.myBluetoothManager.reconnectBT();
				} while (!Global.BTSocket.isConnected() && Global.BTReconnectAttempts <= 10);

				if (Global.BTReconnectAttempts >= 10) {
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
	}

	public void cancel() {
		this.stopWorker = true;
	}
}
