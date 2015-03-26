package com.driven.rowan.drivenbluetooth;

import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

/**
 * Created by BNAGY4 on 26/03/2015.
 */

/* This class should only be used by posting to the UI thread */
public class UIUpdate implements Runnable {

	public void run() {

			// code to update UI elements
			// hopefully this is inherently thread safe
		try {
			MainActivity.mySpeed.setText(Global.SpeedMPH.get(Global.SpeedMPH.size() - 1).get(1).toString());
			MainActivity.myVoltage.setText(Global.Volts.get(Global.Volts.size() - 1).get(1).toString());
			MainActivity.myCurrent.setText(Global.Amps.get(Global.Amps.size() - 1).get(1).toString());
		} catch (Exception e) {
			// something
		}
	}
}
