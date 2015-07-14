package com.ben.drivenbluetooth.util;

import android.graphics.Color;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.jjoe64.graphview.series.DataPoint;

/* This class should only be used by posting to the UI thread */
public class UIUpdateRunnable implements Runnable {

	@Override
	public void run() {
		UpdateBTStatus();
		UpdateFileSize();
		UpdateLap();
	}

	private void UpdateLap() {
		MainActivity.LapNumber.setText("L" + Global.Lap);
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
