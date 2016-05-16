package com.ben.drivenbluetooth.util;

import android.content.res.Resources;
import android.graphics.Color;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;

import org.acra.ACRA;

/* This class should only be used by posting to the UI thread */
public class UIUpdateRunnable implements Runnable {

    Resources r = MainActivity.getAppContext().getResources();

	@Override
	public void run() {
        try {
            UpdateBTCarName();
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(e);
        }
	}

	private void UpdateBTCarName() {
		MainActivity.myBTCarName.setText(Global.BTDeviceName + " :: " + Global.CarName);
	}

	private void UpdateLap() {
		MainActivity.LapNumber.setText("L" + Global.Lap);
	}

	private void UpdateBTStatus() {
		switch (Global.BTState) {
			case DISCONNECTED:
				MainActivity.myBTState.setText("DISCONNECTED");
				MainActivity.myBTState.setTextColor(r.getColor(R.color.negative));
				break;
			case CONNECTING:
				MainActivity.myBTState.setText("CONNECTING");
				MainActivity.myBTState.setTextColor(r.getColor(R.color.neutral));
				break;
			case CONNECTED:
				MainActivity.myBTState.setText("CONNECTED");
				MainActivity.myBTState.setTextColor(r.getColor(R.color.positive));
				break;
			case RECONNECTING:
				MainActivity.myBTState.setText("RECONNECTING... [" + Global.BTReconnectAttempts + "]");
				MainActivity.myBTState.setTextColor(r.getColor(R.color.neutral));
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
