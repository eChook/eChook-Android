package com.driven.rowan.drivenbluetooth;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BNAGY4 on 15/04/2015.
 */
public class DataToCsvFile extends Thread {

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

    private volatile boolean stopWorker = false;

	public void run() {
        while (!this.stopWorker) {
            ArrayList[] ArrayOfLists = new ArrayList[]
                    {
						Global.Throttle,	// 1
						Global.Volts,		// 2
						Global.Amps,		// 3
						Global.MotorRPM,	// 4
						Global.SpeedMPH,	// 5
						Global.TempC1,		// 6
						Global.TempC2,		// 7
						Global.TempC3		// 8
                    };
            try {
                String[] variables = {
						// these should match the array above
                        new String(new byte[] {Global.THROTTLEID}, "UTF-8"),	// 1
                        new String(new byte[] {Global.VOLTID}, "UTF-8"),		// 2
                        new String(new byte[] {Global.AMPID}, "UTF-8"),			// 3
                        new String(new byte[] {Global.MOTORRPMID}, "UTF-8"),	// 4
                        new String(new byte[] {Global.SPEEDMPHID}, "UTF-8"),	// 5
                        new String(new byte[] {Global.TEMP1ID}, "UTF-8"),		// 6
                        new String(new byte[] {Global.TEMP2ID}, "UTF-8"),		// 7
						new String(new byte[] {Global.TEMP3ID}, "UTF-8")		// 8
                };

                int i = 0;
                for (ArrayList<ArrayList<Double>> DataList : ArrayOfLists) {
                    // first copy the list into a local buffer. Must be cloned otherwise the statement
                    // after will clear this one too (Java references...)
                    ArrayList<ArrayList<Double>> LocalDataList = (ArrayList<ArrayList<Double>>) DataList.clone();

                    // Clear the global list
                    DataList.clear();

                    // Write the list to file
                    String string = ListToString(LocalDataList);
                    WriteToFile(string, variables[i]);
                    i++;
                }
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

	private String ListToString(ArrayList<ArrayList<Double>> data) {
		String value = "";

		for (List<Double> dataPoint : data) {
			value += TextUtils.join(",", dataPoint);
			value += "\n";
		}
		return value;
	}

	private void WriteToFile(String data, String variable) {
        if (data.length() > 0) {
            String filename = variable + ".csv";
            try {
                File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                FileOutputStream oStream = new FileOutputStream(f, true);

                if (f.length() == 0) {
                    // file is empty; write headers
                    String headers = "timestamp," + variable + "\n";
                    oStream.write(headers.getBytes());
                }

                // Write data
                oStream.write(data.getBytes());
                oStream.close();
                MediaScannerConnection.scanFile(MainActivity.getAppContext(), new String[]{f.getAbsolutePath()}, null, null);

            } catch (Exception e) {
                e.toString();
            }
        }
	}

    public void cancel() {
        this.stopWorker = true;
    }
}
