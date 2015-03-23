package com.driven.rowan.drivenbluetooth;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ben on 09/03/2015.
 */
public class BTDataParser implements Runnable {
	private byte[] poppedData;
	private volatile boolean stopWorker = false;

	public BTDataParser() {
		// Nothing special for the constructor
	}

	@Override
	public void run() {
		this.stopWorker = false;
		while (!Thread.currentThread().isInterrupted() && !this.stopWorker) {
			poppedData = Global.BTStreamQueue.poll();
			if (poppedData != null) {
                /* poppedData should look like {sXY}
                 * { } are the packet container identifiers
                 * s identifies what kind of information it is:
                 * 		s = wheel RPM or speed
                 * 		i = current
                 *		v = volts
                 *		t = throttle
                 *		r = motor RPM
                 */

				double value; // return value

				// First check if the packet is valid
				if (poppedData.length == Global.PACKETLENGTH	// check if correct length
						&& poppedData[0] == Global.STARTBYTE	// check if starts with '{'
						&& poppedData[Global.PACKETLENGTH - 1] == Global.STOPBYTE) { // check if ends with '}'
					// data is good
					// Now for the hard part

					// if the first byte is greater than 127 then the value is treated as an INTEGER
					// value = [first byte] * 100 + [second byte]

					// if the first byte is less than 128 then the value is treated as a FLOAT
					// value = [first byte] + [second byte] / 100

					if (poppedData[2] < 128) {
						//FLOAT
						value = (double) poppedData[2] + poppedData[3] / 100;
					} else {
						// INTEGER
						value = (double) poppedData[2] * 100 + poppedData[3];
					}

					// Check the ID
					switch (poppedData[1]) {
						case Global.VOLTID:
							AddVoltage(value);
							break;

						case Global.AMPID:
							AddCurrent(value);
							break;

						case Global.MOTORRPMID:
							AddMotorRPM(value);
							break;

						case Global.WHEELRPMID:
							AddSpeed(value);
							break;

						case Global.THROTTLEID:
							AddThrottle(value);
							break;

						default:
							// unrecognised id
							Global.MangledDataCount++;
							break;
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
		double volts = rawVolts; // Apply conversion and offset TODO revisit volts
		double timestamp = System.currentTimeMillis();

		List<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(volts);

		Global.Volts.add(dataPoint);
	}

	private void AddCurrent(double rawAmps) {
		double amps = rawAmps; // Apply conversion and offset TODO revisit amps
		double timestamp = System.currentTimeMillis();

		List<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(amps);

		Global.Amps.add(dataPoint);
	}

	private void AddThrottle(double rawThrottle) {
		double throttle = rawThrottle; // Apply conversion and offset TODO revisit throttle
		double timestamp = System.currentTimeMillis();

		List<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(throttle);

		Global.Throttle.add(dataPoint);
	}

	private void AddSpeed(double rawWheelRPM) {

		double wheelRPM = rawWheelRPM; // Apply conversion and offset TODO revisit wheelRPM
		double timestamp = System.currentTimeMillis(); // Get current timestamp in milliseconds since 1 Jan 1970

		List<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(wheelRPM);
		Global.WheelRPM.add(dataPoint);

		/* CONVERSIONS */
		// Speed = wheelRPM * PI * Wheel Diameter * (60 mins / 1000 metres)
		double speedKPH = wheelRPM * (60 * Math.PI * Global.WHEEL_DIAMETER) / 1000; // km/h
		double speedMPH = speedKPH * 1.61; // mph

		dataPoint.set(1, speedKPH);
		Global.SpeedKPH.add(dataPoint);

		dataPoint.set(1, speedMPH);
		Global.SpeedMPH.add(dataPoint);
	}

	private void AddMotorRPM(double rawMotorRPM) {
		double motorRPM = rawMotorRPM; // Apply conversion and offset TODO revisit motorRPM
		double timestamp = System.currentTimeMillis();

		List<Double> dataPoint = new ArrayList<>();

		dataPoint.add(timestamp);
		dataPoint.add(motorRPM);
		Global.MotorRPM.add(dataPoint);
	}

	public void stop() {
		this.stopWorker = true;
	}

}
