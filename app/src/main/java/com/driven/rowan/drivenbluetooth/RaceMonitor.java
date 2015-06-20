package com.driven.rowan.drivenbluetooth;

/**
 * Created by BNAGY4 on 19/06/2015.
 */
public class RaceMonitor extends Thread {
	private boolean stopWorker = false;

	@Override
	public void run() {
		while (!this.stopWorker) {

		}
	}

	public void cancel() {
		this.stopWorker = true;
	}
}
