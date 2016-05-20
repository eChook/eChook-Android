package com.ben.echookcompanion.threads;

import android.os.Environment;

import com.ben.echookcompanion.Global;
import com.ben.echookcompanion.MainActivity;

import java.io.File;
import java.io.FileOutputStream;

public class DataToCsvFile extends Thread {

	private String[] ArrayOfVariables;
	private String[] variable_identifiers;
	private File f;
	private FileOutputStream oStream;

	private volatile boolean stopWorker = false;

	public DataToCsvFile() {
		try {
			this.variable_identifiers = new String[]{
					// these should match ArrayOfVariables
					"Input throttle (%)",
					"Actual throttle (%)",
					"Volts (V)",
					"Amps (A)",
					"Amp hours (Ah)",
					"Motor speed (RPM)",
                    "Speed (m/s)",
                    "Distance (m)",
                    "Temp 1 (C)",
					"Gear Ratio",

					/* LocationStatus */
					"Latitude (deg)",
					"Longitude (deg)",
					"Altitude (m)",
					"Bearing (deg)",
					"SpeedGPS (m/s)",
					"GPSTime",			    // milliseconds since epoch (UTC)
					"GPSAccuracy (m)",		// radius of 68% confidence
					"Lap",
					"Vehicle"
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
				this.ArrayOfVariables = new String[] {
								String.format("%.0f", Global.InputThrottle),
								String.format("%.0f", Global.ActualThrottle),
								String.format("%.2f", Global.Volts),
								String.format("%.2f", Global.Amps),
								String.format("%.2f", Global.AmpHours),
								String.format("%.0f", Global.MotorRPM),
                                String.format("%.1f", Global.SpeedMPS),
                                String.format("%.3f", Global.DistanceMeters),
                                String.format("%.1f", Global.TempC1),
								String.format("%.3f", Global.GearRatio),
								String.format("%.6f", Global.Latitude),
								String.format("%.6f", Global.Longitude),
								String.format("%.1f", Global.Altitude),
								String.format("%.1f", Global.Bearing),
								String.format("%.1f", Global.SpeedGPS),
								String.format("%.1f", Global.GPSTime),
								String.format("%.1f", Global.GPSAccuracy),
							 	String.format("%d", Global.Lap),
								Global.CarName
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

		for (String value : this.ArrayOfVariables) {
			data_string += value + ",";
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
