package com.ben.drivenbluetooth.threads;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

public class RaceStartMonitor extends Thread {
	private volatile boolean stop = false;
	private ThrottleListener mListener;

	public RaceStartMonitor(ThrottleListener listener) {
		_setThrottleListener(listener);
	}

	/*===================*/
	/* INTERFACE
	/*===================*/
	public interface ThrottleListener {
		void onThrottleMax();
	}

	/*===================*/
	/* MAIN FUNCS
	/*===================*/
	public void run() {
		while (!stop) {
			if(Global.InputThrottle >= 20.0) {
				MainActivity.MainActivityHandler.post(new Runnable() {
					@Override
					public void run() {
						_fireThrottleMaxEvent();
					}
				});
				this.cancel();
			}
			try {
				sleep(250);
			} catch (Exception ignored) {}
		}
	}

	public void cancel() {
		this.stop = true;
	}

	/*===================*/
	/* LISTENER REGISTERING
	/*===================*/
	private synchronized void _setThrottleListener(ThrottleListener listener) {
		mListener = listener;
	}

	/*===================*/
	/* EVENT RAISER
	/*===================*/
	private synchronized void _fireThrottleMaxEvent() {
		mListener.onThrottleMax();
	}


}
