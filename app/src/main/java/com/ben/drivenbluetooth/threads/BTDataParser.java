package com.ben.drivenbluetooth.threads;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.util.GraphData;

import org.acra.ACRA;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class BTDataParser extends Thread {
	public static Handler mHandler;
	private static byte[] poppedData;
	private BTDataParserListener mListener;

	/* == Amp Hour Variables ==*/
	private static double prevAmps = 0.0;
	private static long prevAmpTime = 0;

	private static Socket mTCPSocket;
	private static OutputStream mTCPSocketOS;
	private static boolean mTCPSocketValid = false;

	private static DatagramSocket mUDPSocket;
	private static InetAddress IPAddress;
	private static boolean mUDPSocketValid = false;
	private static int socketCounter = 0;

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
					 * We have defined 255 (0xFF) to be zero because we can't send null bytes over Bluetooth
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
								SetVolts(value);
								break;
							case Global.AMPS_ID:
								SetAmps(value);
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

                        if (MainActivity.NodeJS != null) {
                            Message packet = Message.obtain();
                            packet.obj = poppedData;
                            MainActivity.NodeJS.mHandler.sendMessage(packet);
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
	private void SetVolts(final double rawVolts) {
		Global.Volts = round(rawVolts, 2); // Apply conversion and offset
		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddVolts(rawVolts);
		}
		GraphData.AddVolts(rawVolts);
	}

	private void SetAmps(final double rawAmps) {
		IncrementAmpHours(rawAmps); // amp hours first!
		Global.Amps = round(rawAmps, 2); // Apply conversion and offset
		Global.AverageAmps.add(rawAmps);
		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddAmps(rawAmps);
		}
		GraphData.AddAmps(rawAmps);
	}

	private void SetInputThrottle(final double rawThrottle) {
		Global.InputThrottle = rawThrottle; // Apply conversion and offset
		GraphData.AddInputThrottle(rawThrottle);
	}

	private void SetActualThrottle(double rawThrottle) {
		Global.ActualThrottle = rawThrottle; // Apply conversion and offset
	}

	private void SetSpeed(final double rawSpeedMPH) {
		Global.SpeedMPH = rawSpeedMPH; // Apply conversion and offset
		Global.SpeedKPH = round(Global.SpeedMPH * 1.61, 1);
		Global.AverageSpeedMPH.add(rawSpeedMPH);

		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddSpeed(rawSpeedMPH);
		}

		GraphData.AddSpeed(rawSpeedMPH);
	}

	private void SetMotorRPM(final double rawMotorRPM) {
		Global.MotorRPM = rawMotorRPM; // Apply conversion and offset
		if (Global.Lap > 0) {
			Global.LapDataList.get(Global.Lap - 1).AddRPM(rawMotorRPM);
		}
		GraphData.AddMotorRPM(rawMotorRPM);
	}

	private void SetTemperature(double rawTemp, int sensorId) {
		switch (sensorId) {
			case 1:
				Global.TempC1 = rawTemp;
				GraphData.AddTemperature(rawTemp, sensorId);
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
