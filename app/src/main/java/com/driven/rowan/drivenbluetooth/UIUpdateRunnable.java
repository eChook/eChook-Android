package com.driven.rowan.drivenbluetooth;

import android.widget.EditText;
import java.util.ArrayList;

/**
 * Created by BNAGY4 on 26/03/2015.
 */

/* This class should only be used by posting to the UI thread */
public class UIUpdateRunnable implements Runnable {
	private volatile boolean stopWorker = false;

	public void run() {
		UpdateVoltage();
		UpdateCurrent();
		UpdateThrottle();
		UpdateSpeed();
		UpdateTemp(1);
	}

	private void UpdateVoltage() {
		try {
			double value = Global.Volts.get(Global.Volts.size() - 1).get(1);
			MainActivity.Voltage.setText(String.valueOf(value) + " V");
			MainActivity.VoltageBar.setValue(value);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateCurrent() {
		try {
			double value = Global.Amps.get(Global.Amps.size() - 1).get(1);
			MainActivity.Current.setText(String.valueOf(value) + " A");
			MainActivity.CurrentBar.setValue(value);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateThrottle() {
		try {
			double value = Global.Throttle.get(Global.Throttle.size() - 1).get(1);
			MainActivity.Throttle.setText(String.valueOf(value) + " %");
			MainActivity.ThrottleBar.setValue(value);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateSpeed() {
		try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
				double value = Global.SpeedMPH.get(Global.SpeedMPH.size() - 1).get(1);
				MainActivity.Speed.setText(String.valueOf(value) + " mph");
				MainActivity.SpeedBar.setValue(value);
			} else if (Global.Unit == Global.UNIT.KPH) {
				double value = Global.SpeedKPH.get(Global.SpeedKPH.size() - 1).get(1);
				MainActivity.Speed.setText(String.valueOf(value) + " kph");
				MainActivity.SpeedBar.setValue(value);
			}

		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateTemp(int sensorIndex) {
		ArrayList<ArrayList<Double>> TempArray;
		EditText TempText;
		DataBar TempBar;
		switch (sensorIndex) {
			case 1:
				TempArray = Global.TempC1;
				TempText = MainActivity.Temp1;
				TempBar = MainActivity.T1Bar;
				break;

			default:
				TempArray = null;
				TempText = null;
				TempBar = null;
				break;
		}

		if (TempArray != null && TempText != null && TempBar != null) {
			try {
				double value = TempArray.get(Global.TempC1.size() - 1).get(1);
				TempText.setText(String.valueOf(value) + " C");
				TempBar.setValue(value);
			} catch (Exception e) {
				e.toString();
			}
		}
	}

	private void UpdateMotorRPM() {
		try {
			double value = Global.MotorRPM.get(Global.MotorRPM.size() - 1).get(1);
			MainActivity.RPM.setText(String.valueOf(value) + " RPM");
			MainActivity.RPMBar.setValue(value);
		} catch (Exception e) {
			e.toString();
		}
	}
}
