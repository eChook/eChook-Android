package com.ben.drivenbluetooth.threads;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by Ben on 09/03/2015.
 */
public class BTDataParser extends Thread {
	private static byte[] poppedData;
	private volatile boolean stopWorker = false;
	public static Handler mHandler;

	public BTDataParser() {
		// Nothing special for the constructor
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				poppedData = Global.BTStreamQueue.poll();
				if (poppedData != null) {
                /* poppedData should look like {sXY}
                 * { } are the packet container identifiers
                 * s identifies what kind of information it is
                 */

					double value; // return value

					// First check if the packet is valid
					if (poppedData.length == Global.PACKETLENGTH    // check if correct length
							&& poppedData[0] == Global.STARTBYTE    // check if starts with '{'
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

						if ((poppedData[2] & 0xff) == 255) {
							poppedData[2] = 0;
						}
						if ((poppedData[3] & 0xff) == 255) {
							poppedData[3] = 0;
						}

					/* if the first byte is greater than 127 then the value is treated as an INTEGER
					 * value = [first byte] * 100 + [second byte]
					 *
					 * if the first byte is less than 128 then the value is treated as a FLOAT
					 * value = [first byte] + [second byte] / 100
					 */

						if ((poppedData[2] & 0xff) < 128) {
							//FLOAT
							value = (double) (poppedData[2] & 0xff) + (double) (poppedData[3] & 0xff) / 100;
						} else {
							// INTEGER
							poppedData[2] -= 128;
							value = (double) (poppedData[2] & 0xff) * 100 + (double) (poppedData[3] & 0xff);
						}

						// Check the ID
						switch (poppedData[1]) {
							case Global.VOLTS_ID:
								SetVoltage(value);
								break;
							case Global.AMPS_ID:
								SetCurrent(value);
								break;
							case Global.MOTOR_RPM_ID:
								SetMotorRPM(value);
								break;
							case Global.SPEED_MPH_ID:
								SetSpeed(value);
								break;
							case Global.THR_INPUT_ID:
								SetInputThrottle(value);
								break;
							case Global.THR_ACTUAL_ID:
								SetActualThrottle(value);
								break;
							case Global.TEMP1ID:
								SetTemperature(value, 1);
								break;
							case Global.TEMP2ID:
								SetTemperature(value, 2);
								break;
							case Global.TEMP3ID:
								SetTemperature(value, 3);
								break;
							case Global.LAUNCH_MODE_ID:
								MainActivity.MainActivityHandler.post(new Runnable() {
									public void run() {
										if (Global.Longitude == 0 && Global.Latitude == 0) {
											MainActivity.showMessage(MainActivity.getAppContext(), "Location not ready - please try again in 5 seconds", Toast.LENGTH_LONG);
										} else {
											Location loc = new Location("");
											loc.setLongitude(Global.Longitude);
											loc.setLatitude(Global.Latitude);
											MainActivity.myDrivenLocation.myRaceObserver.ActivateLaunchMode(loc);
										}
									}
								});
								break;

							default:
								Global.MangledDataCount++;
								return false;
						}
						return true;
					} else {
						// data is bad
						Global.MangledDataCount++;
						return false;
					}
				}
				return false;
			}
		});
		Looper.loop();
	}

	/* DATA INPUT FUNCTIONS

		There is one for each because I presume each data type will need
		a different scaling factor / offset

		They are private because the class should decide what function to use
		and no other class needs access to them

	 */

	private void SetVoltage(double rawVolts) {
		Global.Volts = round(rawVolts, 2); // Apply conversion and offset TODO revisit volts
		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				Global.VoltsHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Volts), true, Global.maxGraphDataPoints);
			}
		});
	}

	private void SetCurrent(double rawAmps) {
		Global.Amps = round(rawAmps, 2); // Apply conversion and offset TODO revisit amps
		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				Global.AmpsHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Amps), true, Global.maxGraphDataPoints);
			}
		});
	}

	private void SetInputThrottle(double rawThrottle) {
		Global.InputThrottle = rawThrottle; // Apply conversion and offset TODO revisit throttle
		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				Global.ThrottleHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.InputThrottle), true, Global.maxGraphDataPoints);
			}
		});
	}

	private void SetActualThrottle(double rawThrottle) {
		Global.ActualThrottle = rawThrottle; // Apply conversion and offset TODO revisit throttle
		//MainActivity.ThrottleDerate();
	}

	private void SetSpeed(double rawSpeedMPH) {
		Global.SpeedMPH = rawSpeedMPH; // Apply conversion and offset TODO revisit wheelRPM
		Global.SpeedKPH = round(Global.SpeedMPH * 1.61, 1);

		if (Global.Unit == Global.UNIT.MPH) {
			MainActivity.MainActivityHandler.post(new Runnable() {
				public void run() {
					Global.SpeedHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.SpeedMPH), true, Global.maxGraphDataPoints);
				}
			});
		} else if (Global.Unit == Global.UNIT.KPH) {
			MainActivity.MainActivityHandler.post(new Runnable() {
				public void run() {
					Global.SpeedHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.SpeedKPH), true, Global.maxGraphDataPoints);
				}
			});
		}
	}

	private void SetMotorRPM(double rawMotorRPM) {
		Global.MotorRPM = rawMotorRPM; // Apply conversion and offset TODO revisit motorRPM
		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				Global.MotorRPMHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.MotorRPM), true, Global.maxGraphDataPoints);
			}
		});
	}

	private void SetTemperature(double rawTemp, int sensorId) {
		double tempC = rawTemp;

		switch (sensorId) {
			case 1:
				Global.TempC1 = rawTemp;
				break;
			case 2:
				Global.TempC2 = rawTemp;
				break;
			case 3:
				Global.TempC3 = rawTemp;
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
		Looper.myLooper().quitSafely();
	}

}
