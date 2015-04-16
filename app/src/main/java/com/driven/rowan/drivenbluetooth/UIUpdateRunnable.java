package com.driven.rowan.drivenbluetooth;

/**
 * Created by BNAGY4 on 26/03/2015.
 */

/* This class should only be used by posting to the UI thread */
public class UIUpdateRunnable implements Runnable {
	private volatile boolean stopWorker = false;

	public void run() {

		// code to update UI elements
		// hopefully this is inherently thread safe

		// Voltage
		try {
			MainActivity.myVoltage.setText(Global.Volts.get(Global.Volts.size() - 1).get(1).toString() + " V");
			MainActivity.myVoltsDataCount.setText(Integer.toString(Global.Volts.size()));
		} catch (Exception e) {
			e.toString();
		}

		// Speed
		try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
				MainActivity.mySpeed.setText(Global.Throttle.get(Global.Throttle.size() - 1).get(1).toString() + " %");
				MainActivity.mySpeedDataCount.setText(Integer.toString(Global.Throttle.size()));
			} else if (Global.Unit == Global.UNIT.KPH) {
				MainActivity.mySpeed.setText(Global.SpeedKPH.get(Global.SpeedKPH.size() - 1).get(1).toString() + " kph");
				MainActivity.mySpeedDataCount.setText(Integer.toString(Global.SpeedKPH.size()));
			}

		} catch (Exception e) {
			e.toString();
		}

		// Current
		try {
			MainActivity.myCurrent.setText(Global.Amps.get(Global.Amps.size() - 1).get(1).toString() + " A");
			MainActivity.myAmpsDataCount.setText(Integer.toString(Global.Amps.size()));
		} catch (Exception e) {
			e.toString();
		}
	}
}
