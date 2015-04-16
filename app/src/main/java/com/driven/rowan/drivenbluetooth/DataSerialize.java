package com.driven.rowan.drivenbluetooth;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by BNAGY4 on 08/04/2015.
 */
public class DataSerialize implements Runnable {

	public void run() {

	}

	private void AppendToFile(String filename, List<List<String>> list) {


		try {
			FileOutputStream fos = new FileOutputStream(filename, true);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter osw = new OutputStreamWriter(fos);

			for (List<String> dataPoint : list) {
				osw.write(GetDataPointForSerialization(dataPoint));
			}

			osw.close();
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.toString();
		}

	}

	private String GetDataPointForSerialization(List<String> dataPoint) {
		String value; // return value

		value = dataPoint.get(0) + "," + dataPoint.get(1);

		return value;
	}
}
