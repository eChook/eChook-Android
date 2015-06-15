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
		// Arduino sensor readings
		UpdateVoltage();
		UpdateCurrent();
		UpdateThrottle();
		UpdateSpeed();
		UpdateTemp(1);
		UpdateMotorRPM();

		// Android sensor readings
		UpdateAccelerometer();

		// Other readings
		UpdateBTStatus();
		UpdateLocation();
	}

	private void UpdateVoltage() {
		try {
			MainActivity.Voltage.setText(String.format("%.2f", Global.Volts));
			MainActivity.VoltageBar.setValue(Global.Volts);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateCurrent() {
		try {
			MainActivity.Current.setText(String.format("%.2f", Global.Amps));
			MainActivity.CurrentBar.setValue(Global.Amps);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateThrottle() {
		try {
			MainActivity.Throttle.setText(String.format("%.0f", Global.Throttle));
			MainActivity.ThrottleBar.setValue(Global.Throttle);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateSpeed() {
		try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
				MainActivity.Speed.setText(String.format("%.1f", Global.SpeedMPH) + " mph");
				MainActivity.SpeedBar.setValue(Global.SpeedMPH);
			} else if (Global.Unit == Global.UNIT.KPH) {
				MainActivity.Speed.setText(String.format("%.1f", Global.SpeedKPH) + " kph");
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
				TempText.setText(String.format("%.1f", TempValue) + " C");
				TempBar.setValue(TempValue);
			} catch (Exception e) {
				e.toString();
			}
		}
	}

	private void UpdateMotorRPM() {
		try {
			MainActivity.RPM.setText(String.format("%.0f", Global.MotorRPM));
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
			MainActivity.myLatitude.setText(String.format("%.5f", Global.Latitude));
			MainActivity.myLongitude.setText(String.format("%.5f", Global.Longitude));
		} catch (Exception ignored) {}
	}

	private void UpdateAccelerometer() {
		MainActivity.myGx.setText(String.format("%.2f", Global.Gx));
		MainActivity.myGy.setText(String.format("%.2f", Global.Gy));
		MainActivity.myGz.setText(String.format("%.2f", Global.Gz));
	}

	private void UpdateFileSize() {
		if (Global.DataFileLength < 1024) {
			MainActivity.myDataFileSize.setText(String.valueOf(Global.DataFileLength) + " B");
		} else if (Global.DataFileLength < 1048576) {
			MainActivity.myDataFileSize.setText(String.format("%.2f", (float) Global.DataFileLength / 1024.0) + " KB");
		} else {

		}
	}
}
