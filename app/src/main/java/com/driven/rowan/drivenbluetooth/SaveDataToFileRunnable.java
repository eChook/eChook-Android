package com.driven.rowan.drivenbluetooth;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BNAGY4 on 15/04/2015.
 */
public class SaveDataToFileRunnable implements Runnable {

	/********* SAVING DATA TO FILE **********/
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

	public void run() {
		ArrayList<ArrayList<Double>>[] ArrayOfLists = new ArrayList[]
				{
						Global.Throttle,
						Global.Volts,
						Global.Amps
				};
		try {
			String[] variables = {
					new String(new byte[] {Global.THROTTLEID}, "UTF-8"), // these should match the array above
					new String(new byte[] {Global.VOLTID}, "UTF-8"),
					new String(new byte[] {Global.AMPID}, "UTF-8")
			};

			int i = 0;
			for (ArrayList<ArrayList<Double>> DataList : ArrayOfLists) {
				// first copy the list into a local buffer
				ArrayList<ArrayList<Double>> LocalDataList = (ArrayList<ArrayList<Double>>) DataList.clone();

				// I *think* this will clear the Global list. Will debug to double-check
				DataList.clear();

				// Write the list to file
				String string = ListToString(LocalDataList);
				WriteToFile(string, variables[i]);
				i++;
			}



		} catch (Exception e) {
			e.toString();

		}


	}

	private String ListToString(ArrayList<ArrayList<Double>> data) {
		String value = "";

		for (List<Double> dataPoint : data) {
			value += TextUtils.join(",", dataPoint);
			value += "\n";
		}
		return value;
	}

	private void WriteToFile(String data, String variable) {
		String filename = variable + ".csv";
		try {
			File f = MainActivity.getAppContext().getFileStreamPath(filename);
			FileOutputStream oStream = MainActivity.getAppContext().openFileOutput(filename, Context.MODE_PRIVATE);

			if (f.length() == 0) {
				// file is empty; write headers
				String headers = "timestamp," + variable + "\n";
				oStream.write(headers.getBytes());
			}

			// Write data
			oStream.write(data.getBytes());
			oStream.close();

		} catch (Exception e) {
			e.toString();
		}
	}
}
