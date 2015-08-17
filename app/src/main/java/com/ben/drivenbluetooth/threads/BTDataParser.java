package com.ben.drivenbluetooth.threads;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.jjoe64.graphview.series.DataPoint;

public class BTDataParser extends Thread {
	public static Handler mHandler;
	private static byte[] poppedData;
	private BTDataParserListener mListener;

	/* == Amp Hour Variables ==*/
	private static double prevAmps = 0.0;
	private static long prevAmpTime = 0;

	/*===================*/
	/* BTDATAPARSER
	/*===================*/
	public BTDataParser(BTDataParserListener listener) {
		setBTDataParserListener(listener);
	}

	/*===================*/
	/* INTERFACE
	/*===================*/
	public interface BTDataParserListener {
		void onCycleViewPacket();
		void onActivateLaunchModePacket();
	}

	/*===================*/
	/* REGISTER LISTENER
	/*===================*/
	private synchronized void setBTDataParserListener(BTDataParserListener listener) {
		mListener = listener;
	}

	/*===================*/
	/* MAIN FUNCS
	/*===================*/
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
							case Global.GEAR_RATIO_ID:
								SetGearRatio(value);
								break;
							case Global.LAUNCH_MODE_ID:
								_fireActivateLaunchMode();
								break;
							case Global.CYCLE_VIEW_ID:
								_fireCycleView();
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

	/*===================*/
	/* DATA INPUT FUNCS
	/*===================*/
	private void SetVoltage(double rawVolts) {
		Global.Volts = round(rawVolts, 2); // Apply conversion and offset TODO revisit volts
		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddVolts(rawVolts);
		}
		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				Global.VoltsHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Volts), true, Global.maxGraphDataPoints);
			}
		});
	}

	private void SetCurrent(double rawAmps) {
		IncrementAmpHours(rawAmps); // amp hours first!
		Global.Amps = round(rawAmps, 2); // Apply conversion and offset TODO revisit amps
		Global.AverageAmps.add(rawAmps);
		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddAmps(rawAmps);
		}
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
		Global.AverageSpeedMPH.add(rawSpeedMPH);

		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddSpeed(rawSpeedMPH);
		}

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
		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddRPM(rawMotorRPM);
		}
		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				Global.MotorRPMHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.MotorRPM), true, Global.maxGraphDataPoints);
			}
		});
	}

	private void SetTemperature(double rawTemp, int sensorId) {
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

	private void SetGearRatio(double rawRatio) {
		Global.GearRatio = rawRatio; // Apply conversion and offset
	}

	private double round(double number, int decimalPoints) {
		double value = Math.round(number * Math.pow(10, decimalPoints));
		value = value / Math.pow(10, decimalPoints);
		return value;
	}

	private void IncrementAmpHours(double amps) {
		long millis = System.currentTimeMillis();
		if (prevAmpTime != 0) {
			double ah = 0.5 * (amps + prevAmps) * (millis - prevAmpTime) /* Amp-milliseconds */
					/ 1000 / 60 / 60; /* Amp-hours */

			Global.AmpHours += ah;

			if (Global.Lap > 0) {
				Global.LapDataList.get(Global.Lap - 1).AddAmpHours(ah);
			}
		}
		prevAmpTime = millis;
		prevAmps = amps;
	}

	/*===================*/
	/* EVENT RAISERS
	/*===================*/
	private synchronized void _fireActivateLaunchMode() {
		mListener.onActivateLaunchModePacket();
	}

	private synchronized void _fireCycleView() {
		mListener.onCycleViewPacket();
	}
}
