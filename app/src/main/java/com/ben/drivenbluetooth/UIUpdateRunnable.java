package com.ben.drivenbluetooth;

import android.graphics.Color;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

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
		//UpdateAccelerometer();

		// Other readings
		UpdateBTStatus();
		//UpdateLocation();
		UpdateFileSize();

		Global.GraphTimeStamp += (float) Global.UI_UPDATE_INTERVAL / 1000.0f;
	}

	private void UpdateVoltage() {
		try {
			MainActivity.Voltage.setText(String.format("%.2f", Global.Volts));
			MainActivity.VoltageBar.setValue(Global.Volts);
			Global.VoltsHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Volts), true, Global.maxGraphDataPoints);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateCurrent() {
		try {
			MainActivity.Current.setText(String.format("%.1f", Global.Amps));
			MainActivity.CurrentBar.setValue(Global.Amps);
			Global.AmpsHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Amps), true, Global.maxGraphDataPoints);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateThrottle() {
		try {
			MainActivity.Throttle.setText(String.format("%.0f", Global.Throttle));
			MainActivity.ThrottleBar.setValue(Global.Throttle);
			Global.ThrottleHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Throttle), true, Global.maxGraphDataPoints);
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
				Global.SpeedHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.SpeedMPH), true, Global.maxGraphDataPoints);
			} else if (Global.Unit == Global.UNIT.KPH) {
				MainActivity.Speed.setText(String.format("%.1f", Global.SpeedKPH) + " kph");
				MainActivity.SpeedBar.setValue(Global.SpeedKPH);
				Global.SpeedHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.SpeedKPH), true, Global.maxGraphDataPoints);
			}

		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateTemp(int sensorIndex) {
		Double TempValue;
		TextView TempText;
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
				Global.TempC1History.appendData(new DataPoint(Global.GraphTimeStamp, TempValue), true, Global.maxGraphDataPoints);
			} catch (Exception e) {
				e.toString();
			}
		}
	}

	private void UpdateMotorRPM() {
		try {
			MainActivity.RPM.setText(String.format("%.0f", Global.MotorRPM));
			MainActivity.RPMBar.setValue(Global.MotorRPM);
			Global.MotorRPMHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.MotorRPM), true, Global.maxGraphDataPoints);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateBTStatus() {
		switch (Global.BTState) {
			case DISCONNECTED:
				MainActivity.myBTState.setText("DISCONNECTED");
				MainActivity.myBTState.setTextColor(Color.RED);
				break;
			case CONNECTING:
				MainActivity.myBTState.setText("CONNECTING");
				MainActivity.myBTState.setTextColor(Color.YELLOW);
				break;
			case CONNECTED:
				MainActivity.myBTState.setText("CONNECTED");
				MainActivity.myBTState.setTextColor(Color.GREEN);
				break;
			case RECONNECTING:
				MainActivity.myBTState.setText("RECONNECTING... [" + Global.BTReconnectAttempts + "]");
				MainActivity.myBTState.setTextColor(Color.YELLOW);
				break;
		}
	}

	private void UpdateFileSize() {
		if (Global.DataFileLength < 1024) {
			MainActivity.myDataFileSize.setText(String.valueOf(Global.DataFileLength) + " B");
		} else if (Global.DataFileLength < 1048576) {
			MainActivity.myDataFileSize.setText(String.format("%.2f", (float) Global.DataFileLength / 1024.0) + " KB");
		} else {
			MainActivity.myDataFileSize.setText(String.format("%.2f", (float) Global.DataFileLength / 1048576) + " MB");
		}
	}
}
