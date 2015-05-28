package com.driven.rowan.drivenbluetooth;

import android.app.AlarmManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Ben on 09/03/2015.
 */
public final class Global {

	/**********************/
	/**********************/
	/* ALL VARIABLES IN   */
	/* THIS CLASS SHOULD  */
	/* BE DECLARED STATIC */
	/**********************/
	/**********************/

	/**********************/
	/* THREADED VARIABLES */
	/**********************/
	// use the volatile keyword for thread safety
    public static volatile BlockingQueue<byte[]> BTStreamQueue = 	new LinkedBlockingQueue<>();
	public static volatile ArrayList<ArrayList<Double>> Volts = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> Amps = 		new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> Throttle = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> MotorRPM = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> WheelRPM = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> SpeedMPH = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> SpeedKPH = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> TempC1 = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> TempC2 = 	new ArrayList<>();
	public static volatile ArrayList<ArrayList<Double>> TempC3 = 	new ArrayList<>();

	public static volatile Object BTReconnectLock = new Object();

	/**********************/
	/* VARIABLES          */
	/**********************/
	public static BluetoothSocket BTSocket;
	public static int MangledDataCount = 0;
	public static GregorianCalendar RaceStartTime;
	public static AlarmManager AlarmManager;

	/**********************/
	/* CONSTANTS          */
	/**********************/
	public static final byte STARTBYTE 	= 	123; // ASCII Code for '{'
    public static final byte STOPBYTE = 	125; // ASCII code for '}'
    public static final int PACKETLENGTH = 	5; 	 // { [id] [1] [2] }
                                              	 // 1   2   3   4  5

	public static final byte VOLTID = 		118; // v
	public static final byte AMPID = 		105; // i
	public static final byte MOTORRPMID =	114; // r
	public static final byte WHEELRPMID =	115; // s
	public static final byte THROTTLEID =	116; // t
	public static final byte SPEEDMPHID =	117; // u
	public static final byte TEMP1ID = 		97;	 // a
	public static final byte TEMP2ID = 		98;	 // b
	public static final byte TEMP3ID = 		99;  // c

    public static final int DATA_SAVE_INTERVAL = 5000; // save interval in milliseconds
	public static final int BT_DATA_TIMEOUT = 2000; // Bluetooth connection timeout in milliseconds

	/**********************/
	/* SETTINGS VARIABLES */
	/**********************/
	public static enum MODE {DEMO, RACE}
	public static MODE Mode;

	public static enum UNIT {MPH, KPH}
	public static UNIT Unit;

	/**********************/
	/* CONSTRUCTOR        */
	/**********************/
    private Global() {
        try {
            // Initialize an empty queue
            BTStreamQueue.clear();

        } catch (Error e) {
            // Do nothing
        }
    }
}