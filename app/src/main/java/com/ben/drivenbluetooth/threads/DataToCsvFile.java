package com.ben.drivenbluetooth.threads;

import android.os.Environment;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import java.io.File;
import java.io.FileOutputStream;

public class DataToCsvFile extends Thread {

	private Double[] ArrayOfVariables;
	private String[] variable_identifiers;
	private File f;
	private FileOutputStream oStream;

	/********* SAVING DATA TO FILE **********/

	/** NEW PROCESS **/
	/**
	 * All of the variables are now stored as a global Double which is updated by the parser
	 *
	 * Every save interval the logger will write each variable to a common file. The save interval
	 * generates the timestamp, it is NOT generated when the data is received. There may be a delay
	 * between the actual values though it is negligible
	 */

	private volatile boolean stopWorker = false;

	public DataToCsvFile() {
		try {
			this.variable_identifiers = new String[]{
					// these should match ArrayOfVariables
					"InputThrottle (%)",
					"ActualThrottle (%)",
					"Volts (V)",
					"Amps (A)",
					"Motor speed (RPM)",
					"Speed (mph)",
					"Temp 1 (C)",
					"Gear Ratio",

					/* LocationStatus */
					"Latitude (deg)",
					"Longitude (deg)",
					"Altitude (m)",
					"Bearing (deg)",
					"SpeedGPS (m/s)",
					"GPSTime",			// milliseconds since epoch (UTC)
					"Accuracy (m)",		// radius of 68% confidence
					"Lap"
			};
		} catch (Exception e) {
			MainActivity.showError(e);
		}

		try {
			this.f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
			this.oStream = new FileOutputStream(f, true);
		} catch (Exception e) {
			MainActivity.showError(e);
		}
	}

	public void run() {
		while (!this.stopWorker) {
			try {
				this.ArrayOfVariables = new Double[] {
									Global.InputThrottle,
									Global.ActualThrottle,
									Global.Volts,
									Global.Amps,
									Global.MotorRPM,
									Global.SpeedMPH,
									Global.TempC1,
									Global.GearRatio,

									/* LocationStatus */
									Global.Latitude,
									Global.Longitude,
									Global.Altitude,
									Global.Bearing,
									Global.SpeedGPS,
									Global.GPSTime,
									Global.Accuracy,
						(double) 	Global.Lap
				};

				WriteToFile(GetLatestDataAsString());

			} catch (Exception e) {
				MainActivity.showError(e);
			}

			try { // for some reason this needs to be in a try/catch block
				Thread.sleep(Global.DATA_SAVE_INTERVAL);
			} catch (Exception e) {
				MainActivity.showError(e);
			}
		}
	}

	/**
	 * Returns a string representation of the sensor variables stored
	 * in the Global class with the current timestamp
	 * @return a string formatted as "timestamp,x,y,z,..."
	 */
	private String GetLatestDataAsString() {
		String data_string = String.valueOf(System.currentTimeMillis()) + ",";

		for (Double value : this.ArrayOfVariables) {
			data_string += String.valueOf(value) + ",";
		}

		//remove last comma
		data_string = data_string.substring(0, data_string.length() - 1);

		// add newline
		data_string += "\n";

		return data_string;
	}

	private void WriteToFile(String data) {
		if (data.length() > 0) {
			try {

				if (f.length() == 0) {
					// file is empty; write headers

					/* 	|	timestamp	|	t 	| 	v	|	i	|  ...	|
						|				|		|		|		|		|
					*/
					String headers = "timestamp,";
					for (int i = 0; i <= this.variable_identifiers.length - 1; i++) {
						headers += variable_identifiers[i] + ",";
					}
					// remove the last comma
					headers = headers.substring(0, headers.length() - 1);

					// add newline
					headers += "\n";

					// write to file
					oStream.write(headers.getBytes());

					resetValues();
				}

				// Write data
				oStream.write(data.getBytes());

			} catch (Exception e) {
				e.toString();
				try {
					oStream.close();
				} catch (Exception ignored) {}
			}
		}
	}

	private void resetValues() {
		// This function holds the references to each value which needs to be reset when they are
		// written to file, e.g. accelerometer, delta distances
		Global.DeltaDistance = 0;
	}

	public void cancel() {
		this.stopWorker = true;
		try {
			oStream.close();
		} catch (Exception ignored) {}
	}
}
