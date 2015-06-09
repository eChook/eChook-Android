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
		// Sensor readings
		UpdateVoltage();
		UpdateCurrent();
		UpdateThrottle();
		UpdateSpeed();
		UpdateTemp(1);
		UpdateMotorRPM();

		UpdateBTStatus();
		UpdateLocation();
	}

	private void UpdateVoltage() {
		try {
			MainActivity.Voltage.setText(Global.Volts.toString());
			MainActivity.VoltageBar.setValue(Global.Volts);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateCurrent() {
		try {
			MainActivity.Current.setText(Global.Amps.toString());
			MainActivity.CurrentBar.setValue(Global.Amps);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateThrottle() {
		try {
			MainActivity.Throttle.setText(Global.Throttle.toString());
			MainActivity.ThrottleBar.setValue(Global.Throttle);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateSpeed() {
		try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
				MainActivity.Speed.setText(Global.SpeedMPH.toString());
				MainActivity.SpeedBar.setValue(Global.SpeedMPH);
			} else if (Global.Unit == Global.UNIT.KPH) {
				MainActivity.Speed.setText(Global.SpeedKPH.toString());
				MainActivity.SpeedBar.setValue(Global.SpeedKPH);
			}

		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateTemp(int sensorIndex) {
		Double TempValue;
		EditText TempText;
		DataBar TempBar;
		switch (sensorIndex) {
			case 1:
				TempValue = Global.TempC1;
				TempText = MainActivity.Temp1;
				TempBar = MainActivity.T1Bar;
				break;

			default:
				TempValue = null;
				TempText = null;
				TempBar = null;
				break;
		}

		if (TempValue != null && TempText != null && TempBar != null) {
			try {
				TempText.setText(String.valueOf(TempValue) + " C");
				TempBar.setValue(TempValue);
			} catch (Exception e) {
				e.toString();
			}
		}
	}

	private void UpdateMotorRPM() {
		try {
			MainActivity.RPM.setText(Global.MotorRPM.toString());
			MainActivity.RPMBar.setValue(Global.MotorRPM);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateBTStatus() {
		switch (Global.BTState) {
			case NONE:
				MainActivity.myLabel.setText("Select 'Open BT' to connect to Bluetooth");
				break;

			case CONNECTED:
				MainActivity.myLabel.setText("Logging...");
				break;

			case DISCONNECTED:
				MainActivity.myLabel.setText("Bluetooth disconnected. Retrying... [" + Global.BTReconnectAttempts + "]");
				break;
		}
	}

	private void UpdateLocation() {
		try {
			MainActivity.myLatitude.setText(String.valueOf(Global.Latitude));
			MainActivity.myLongitude.setText(String.valueOf(Global.Longitude));
		} catch (Exception ignored) {}
	}
}
