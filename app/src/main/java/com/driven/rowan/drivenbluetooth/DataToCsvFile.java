package com.driven.rowan.drivenbluetooth;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by BNAGY4 on 15/04/2015.
 */
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
	 * Every save interval the logger will write each variable to the file
	 */

    /* OLD PROCESS */
	/* Each variable will have its own .csv file.
	 * We can't compile it all into one because each variable
	 * reading will have a different timestamp.
	 *
	 * Each filename will be named after its identifier code, e.g.
	 *              "v.csv"
	 * for voltage.
	 *
	 * The lists are effectively 'buffers' and need to be emptied out
	 * every time a line is written.
	 */

	private volatile boolean stopWorker = false;

	public DataToCsvFile() {
		try {
			this.variable_identifiers = new String[]{
					// these should match ArrayOfVariables
					new String(new byte[]{Global.THROTTLEID}, "UTF-8"),    	// 1
					new String(new byte[]{Global.VOLTID}, "UTF-8"),        	// 2
					new String(new byte[]{Global.AMPID}, "UTF-8"),         	// 3
					new String(new byte[]{Global.MOTORRPMID}, "UTF-8"),    	// 4
					new String(new byte[]{Global.SPEEDMPHID}, "UTF-8"),    	// 5
					new String(new byte[]{Global.TEMP1ID}, "UTF-8"),       	// 6
					new String(new byte[]{Global.TEMP2ID}, "UTF-8"),       	// 7
					new String(new byte[]{Global.TEMP3ID}, "UTF-8"),        // 8

					/* Location */
					"Latitude (deg)",										// 9
					"Longitude (deg)",										// 10
					"Altitude (m)",											// 11
					"Bearing (deg)",										// 12
					"SpeedGPS (m/s)",										// 13
					"GPSTime",			// milliseconds since epoch (UTC)	// 14
					"Accuracy (m)",		// radius of 68% confidence			// 15
					"DeltaDistance (m)"										// 16
			};
		} catch (Exception e) {
			MainActivity.showMessage(MainActivity.getAppContext(), e.toString(), Toast.LENGTH_SHORT);
		}

		try {
			this.f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
			this.oStream = new FileOutputStream(f, true);
		} catch (Exception e) {
			MainActivity.showMessage(MainActivity.getAppContext(), e.toString(), Toast.LENGTH_LONG);
		}
	}

	public void run() {
		while (!this.stopWorker) {
			try {
				this.ArrayOfVariables = new Double[] {
									Global.Throttle,    	// 1
									Global.Volts,       	// 2
									Global.Amps,        	// 3
									Global.MotorRPM,    	// 4
									Global.SpeedMPH,    	// 5
									Global.TempC1,      	// 6
									Global.TempC2,      	// 7
									Global.TempC3,      	// 8

									/* Location */
									Global.Latitude,		// 9
									Global.Longitude,		// 10
									Global.Altitude,		// 11
									Global.Bearing,			// 12
									Global.SpeedGPS,		// 13
									Global.GPSTime,			// 14
									Global.Accuracy,		// 15
						(double) 	Global.DeltaDistance	// 16
				};

				WriteToFile(GetLatestDataAsString());

			} catch (Exception e) {
				e.toString();
			}

			try { // for some reason this needs to be in a try/catch block
				Thread.sleep(Global.DATA_SAVE_INTERVAL);
			} catch (Exception e) {
				// ??
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
		Global.Gx = 0;
		Global.Gy = 0;
		Global.Gz = 0;
	}

	public void cancel() {
		this.stopWorker = true;
		try {
			oStream.close();
		} catch (Exception ignored) {}
	}
}
