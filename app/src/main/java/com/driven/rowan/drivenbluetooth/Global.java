package com.driven.rowan.drivenbluetooth;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Ben on 09/03/2015.
 */
public final class Global {
    public static BlockingQueue<byte[]> BTStreamQueue;
	public static List<List<Double>> Volts;
	public static List<List<Double>> Amps;
	public static List<List<Double>> Throttle;
	public static List<List<Double>> MotorRPM;
	public static List<List<Double>> WheelRPM;
	public static List<List<Double>> SpeedMPH;
	public static List<List<Double>> SpeedKPH;
	public static final double WHEEL_DIAMETER = 0.5; // in metres

	public static final byte STARTBYTE = 123; // ASCII Code for '{'
    public static final byte STOPBYTE = 125; // ASCII code for '}'
    public static final int PACKETLENGTH = 5; // { [id] [1] [2] }
                                              // 1   2   3   4  5

	public static final byte VOLTID = 118; // v
	public static final byte AMPID = 105; // i
	public static final byte MOTORRPMID = 114; // r
	public static final byte WHEELRPMID = 115; // s
	public static final byte THROTTLEID = 116; // t

	public static int MangledDataCount = 0;

    private Global() {
        try {
            // Initialize an empty queue
            BTStreamQueue.clear();
        } catch (Error e) {
            // Do nothing
        }
    }
}