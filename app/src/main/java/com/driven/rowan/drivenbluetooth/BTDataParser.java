package com.driven.rowan.drivenbluetooth;

import java.util.ArrayList;

/**
 * Created by Ben on 09/03/2015.
 */
public class BTDataParser extends Thread {
	private byte[] poppedData;
	private volatile boolean stopWorker = false;

	public BTDataParser() {
		// Nothing special for the constructor
	}

	@Override
	public void run() {
		this.stopWorker = false;
		while (!this.stopWorker) {
			poppedData = Global.BTStreamQueue.poll();
			if (poppedData != null) {
                /* poppedData should look like {sXY}
                 * { } are the packet container identifiers
                 * s identifies what kind of information it is
                 */

				double value; // return value

				// First check if the packet is valid
				if (poppedData.length == Global.PACKETLENGTH	// check if correct length
						&& poppedData[0] == Global.STARTBYTE	// check if starts with '{'
						&& poppedData[Global.PACKETLENGTH - 1] == Global.STOPBYTE) { // check if ends with '}'
					// data is good
					// Now for the hard part
					// if the byte is 255 / 0xFF / 11111111 then the value is interpreted as zero
					// because you can't send null bytes over Bluetooth.
					// A side-effect of this is that we can't send the value "255"
					// a byte in Java is -128 to 127 so we must convert to an int by doing & 0xff

					/* Explanation :
					 * & is a bitwise AND operation
					 * (byte) 1111111 is interpreted by Java as -128
					 * 11111111 & 0xff converts the value to an integer, which is then interpreted as
					 * (int) 255, thus enabling the proper comparison
					 */
					if ((poppedData[2] & 0xff) == 255) { poppedData[2] = 0; }
					if ((poppedData[3] & 0xff) == 255) { poppedData[3] = 0; }

					/* if the first byte is greater than 127 then the value is treated as an INTEGER
					 * value = [first byte] * 100 + [second byte]
					 *
					 * if the first byte is less than 128 then the value is treated as a FLOAT
					 * value = [first byte] + [second byte] / 100
					 */

					if (poppedData[2] < 128) {
						//FLOAT
						value = (double)poppedData[2] + (double)poppedData[3] / 100;
					} else {
						// INTEGER
						value = (double)poppedData[2] * 100 + (double)poppedData[3];
					}

					// Check the ID
					switch (poppedData[1]) {
						case Global.VOLTID: 	AddVoltage(value); 			break;
						case Global.AMPID:		AddCurrent(value); 			break;
						case Global.MOTORRPMID:	AddMotorRPM(value);			break;
						case Global.WHEELRPMID:	AddSpeed(value);			break;
						case Global.THROTTLEID:	AddThrottle(value);			break;
						case Global.TEMP1ID:	AddTemperature(value, 1); 	break;
						case Global.TEMP2ID:	AddTemperature(value, 2); 	break;
						case Global.TEMP3ID:	AddTemperature(value, 3); 	break;

						default:				Global.MangledDataCount++;	break;
					}
				} else {
					// data is bad
					Global.MangledDataCount++;
				}
			}
		}
	}

	/* DATA INPUT FUNCTIONS

		There is one for each because I presume each data type will need
		a different scaling factor / offset

		They are private because the class should decide what function to use
		and no other class needs access to them

	 */

	private void AddVoltage(double rawVolts) {
		double volts = round(rawVolts, 2); // Apply conversion and offset TODO revisit volts
		double timestamp = System.currentTimeMillis();

		ArrayList<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(volts);

		Global.Volts.add(dataPoint);
	}

	private void AddCurrent(double rawAmps) {
		double amps = round(rawAmps, 2); // Apply conversion and offset TODO revisit amps
		double timestamp = System.currentTimeMillis();

		ArrayList<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(amps);

		Global.Amps.add(dataPoint);
	}

	private void AddThrottle(double rawThrottle) {
		double throttle = rawThrottle; // Apply conversion and offset TODO revisit throttle
		double timestamp = System.currentTimeMillis();

		ArrayList<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(throttle);

		Global.Throttle.add(dataPoint);
	}

	private void AddSpeed(double rawSpeedMPH) {

		double speedMPH = rawSpeedMPH; // Apply conversion and offset TODO revisit wheelRPM
		double timestamp = System.currentTimeMillis(); // Get current timestamp in milliseconds since 1 Jan 1970

		ArrayList<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(speedMPH);
		Global.SpeedMPH.add(dataPoint);

		double speedKPH = round(speedMPH * 1.61, 1);

		dataPoint.set(1, speedKPH);
		Global.SpeedKPH.add(dataPoint);
	}

	private void AddMotorRPM(double rawMotorRPM) {
		double motorRPM = rawMotorRPM; // Apply conversion and offset TODO revisit motorRPM
		double timestamp = System.currentTimeMillis();

		ArrayList<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(motorRPM);
		Global.MotorRPM.add(dataPoint);
	}

	private void AddTemperature(double rawTemp, int sensorId) {
		double tempC = rawTemp;
		double timestamp = System.currentTimeMillis();
		ArrayList<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(tempC);

		switch (sensorId) {
			case 1:
				Global.TempC1.add(dataPoint);
				break;
			case 2:
				Global.TempC2.add(dataPoint);
				break;
			case 3:
				Global.TempC3.add(dataPoint);
				break;
			default:
				break;
		}
	}

	private double round(double number, int decimalPoints) {
		double value = Math.round(number * Math.pow(10, decimalPoints));
		value = value / Math.pow(10, decimalPoints);
		return value;
	}

	public void cancel() {
		this.stopWorker = true;
	}

}
