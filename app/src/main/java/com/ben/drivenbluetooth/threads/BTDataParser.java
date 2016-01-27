package com.ben.drivenbluetooth.threads;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.util.GraphData;
import com.ben.drivenbluetooth.util.RunningAverage;

public class BTDataParser extends Thread {
    public static Handler mHandler;
    private byte[] poppedData;
    private BTDataParserListener mListener;

    /* == Amp Hour Variables ==*/
    private double prevAmps = 0.0;
    private long prevAmpTime = 0;

    /* == Watt hour variables ==*/
    private RunningAverage WattHourAvg = new RunningAverage(0); // decimal format doesn't matter because we don't use the print function

    /* == Distance variables ==*/
    private long prevDistTime = 0;

    /*===================*/
	/* BTDATAPARSER
	/*===================*/
    public BTDataParser(BTDataParserListener listener) {
        setBTDataParserListener(listener);
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

                        /* First send the packet over UDP */
                        if (MainActivity.mUDPSender != null) {
                            Message packet = Message.obtain();
                            packet.obj = poppedData;
                            MainActivity.mUDPSender.PacketHandler.sendMessage(packet);
                        }

                        byte firstByte = poppedData[2];
                        byte secondByte = poppedData[3];

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

                        if ((firstByte & 0xff) == 255) {
                            firstByte = 0;
                        }
                        if ((secondByte & 0xff) == 255) {
                            secondByte = 0;
                        }

						/* if the first byte is greater than 127 then the value is treated as an INTEGER
						 * value = [first byte] * 100 + [second byte]
						 *
						 * if the first byte is less than 128 then the value is treated as a FLOAT
						 * value = [first byte] + [second byte] / 100
						 */

                        if ((firstByte & 0xff) < 128) {
                            // FLOAT
                            // value = [first byte] + [second byte] / 100
                            value = (double) (firstByte & 0xff) + (double) (secondByte & 0xff) / 100;
                        } else {
                            // INTEGER
                            // value = [first byte] * 100 + [second byte]
                            firstByte -= 128;
                            value = (double) (firstByte & 0xff) * 100 + (double) (secondByte & 0xff);
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
    private synchronized void SetVolts(final double rawVolts) {
        Global.Volts = rawVolts; // Apply conversion and offset
        if (Global.Lap > 0) {
            Global.LapDataList.get(Global.Lap - 1).AddVolts(rawVolts);
        }
        GraphData.AddVolts(rawVolts);
        MainActivity.MainActivityHandler.post(new Runnable() {
            public void run() {
                MainActivity.currentFragment.UpdateVolts();
            }
        });
    }

    private synchronized void SetAmps(final double rawAmps) {
        Global.Amps = rawAmps;
        // timekeeping for watt-hours and amp-hours
        long millis = System.currentTimeMillis();
        long dt = millis - prevAmpTime;
        if (prevAmpTime > 0) {
            IncrementAmpHours(Global.Amps, dt);
            CalculateWattHours(Global.Volts, Global.Amps, dt);
        }
        prevAmps = rawAmps;
        prevAmpTime = millis;

        Global.Amps = rawAmps; // Apply conversion and offset
        Global.AverageAmps.add(rawAmps);
        if (Global.Lap > 0) {
            Global.LapDataList.get(Global.Lap - 1).AddAmps(rawAmps);
        }
        GraphData.AddAmps(rawAmps);

		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				MainActivity.currentFragment.UpdateAmps();
			}
		});
    }

    private synchronized void SetInputThrottle(final double rawThrottle) {
        Global.InputThrottle = rawThrottle; // Apply conversion and offset
        GraphData.AddInputThrottle(rawThrottle);

		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				MainActivity.currentFragment.UpdateThrottle();
			}
		});
    }

    private synchronized void SetActualThrottle(double rawThrottle) {
        Global.ActualThrottle = rawThrottle; // Apply conversion and offset
    }

    private synchronized void SetSpeed(final double rawSpeedMPS) {
        Global.SpeedKPH = rawSpeedMPS * 3.6;

        long millis = System.currentTimeMillis();
        long dt = millis - prevDistTime;
        if (prevDistTime > 0) {
            CalculateDistanceKM(Global.SpeedKPH, dt);
        }
        prevDistTime = millis;

        Global.AverageSpeedKPH.add(Global.SpeedKPH);

        if (Global.Lap > 0) {
            Global.LapDataList.get(Global.Lap - 1).AddSpeed(Global.SpeedKPH);
        }

        GraphData.AddSpeed(Global.Unit == Global.UNIT.MPH ? Global.SpeedKPH / 1.61 : Global.SpeedKPH);

		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				MainActivity.currentFragment.UpdateSpeed();
			}
		});
    }

    private synchronized void SetMotorRPM(final double rawMotorRPM) {
        Global.MotorRPM = rawMotorRPM; // Apply conversion and offset
        if (Global.Lap > 0) {
            Global.LapDataList.get(Global.Lap - 1).AddRPM(rawMotorRPM);
        }
        GraphData.AddMotorRPM(rawMotorRPM);

		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
				MainActivity.currentFragment.UpdateMotorRPM();
			}
		});
    }

    private synchronized void SetTemperature(double rawTemp, final int sensorId) {
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

        MainActivity.MainActivityHandler.post(new Runnable() {
            public void run() {
                MainActivity.currentFragment.UpdateTemp(sensorId);
            }
        });
    }

    private synchronized void SetGearRatio(double rawRatio) {
        Global.GearRatio = rawRatio; // Apply conversion and offset
    }

    private synchronized void IncrementAmpHours(double amps, long dt_millis) {
        double ah = 0.5 * (amps + prevAmps) * dt_millis /* Amp-milliseconds */
                / 1000 / 60 / 60; /* Amp-hours */

        Global.AmpHours += ah;

        if (Global.Lap > 0) {
            Global.LapDataList.get(Global.Lap - 1).AddAmpHours(ah);
        }

		MainActivity.MainActivityHandler.post(new Runnable() {
			public void run() {
                MainActivity.currentFragment.UpdateAmpHours();
            }
		});
    }

    private synchronized void CalculateWattHours(double volts, double amps, long dt_millis) {
        double wh = volts * amps * dt_millis // watt-milliseconds
                / 1000 / 60 / 60; // watt-hours

        Global.WattHours += wh;
        WattHourAvg.add(wh);
    }

    private synchronized void CalculateDistanceKM(double speedKPH, long dt_millis) {
        double deltaKM = speedKPH * dt_millis / 1000 / 60 / 60;
        Global.DistanceKM += deltaKM;

        Global.WattHoursPerKM = WattHourAvg.getAverage() / deltaKM;

        if (Global.Lap > 0) {
            Global.LapDataList.get(Global.Lap - 1).AddDistanceKM(deltaKM);
            Global.LapDataList.get(Global.Lap - 1).AddWattHours(WattHourAvg.getAverage());
        }

        WattHourAvg.reset();

        MainActivity.MainActivityHandler.post(new Runnable() {
            public void run() {
                MainActivity.currentFragment.UpdateWattHours();
            }
        });
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

    /*===================*/
    /* INTERFACE
	/*===================*/
    public interface BTDataParserListener {
        void onCycleViewPacket();

        void onActivateLaunchModePacket();
    }
}