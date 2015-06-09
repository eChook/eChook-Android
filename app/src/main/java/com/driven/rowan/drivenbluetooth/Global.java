package com.driven.rowan.drivenbluetooth;

import android.app.AlarmManager;
import android.bluetooth.BluetoothSocket;

import java.util.GregorianCalendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GLOBAL
 *
 * This class is a singleton which holds all the global variables referenced by the application
 *
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
	public static volatile Double Volts 	= 	0.0;
	public static volatile Double Amps 		= 	0.0;
	public static volatile Double Throttle 	= 	0.0;
	public static volatile Double MotorRPM 	= 	0.0;
	public static volatile Double SpeedMPH 	= 	0.0;
	public static volatile Double SpeedKPH 	= 	0.0;
	public static volatile Double TempC1 	= 	0.0;
	public static volatile Double TempC2 	= 	0.0;
	public static volatile Double TempC3 	= 	0.0;

	public static volatile Double Latitude 	=	0.0;
	public static volatile Double Longitude	=	0.0;
	public static volatile Double Altitude	=	0.0;
	public static volatile Double Bearing	=	0.0;
	public static volatile Double SpeedGPS	=	0.0;
	public static volatile Double GPSTime	=	0.0;
	public static volatile Double Accuracy	=	0.0;
	public static volatile int LocationUpdateCounter = 0;

	public static volatile int BTReconnectAttempts = 0;

	/* BLUETOOTH STATE TRACKER */
	public enum BTSTATE {NONE, CONNECTED, DISCONNECTED}
	public static volatile BTSTATE BTState = BTSTATE.NONE;

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
	public static final byte STARTBYTE 		= 	123; // ASCII Code for '{'
    public static final byte STOPBYTE 		= 	125; // ASCII code for '}'
    public static final int PACKETLENGTH 	= 	5; 	 // { [id] [1] [2] }
                                              	 // 1   2   3   4  5

	public static final byte VOLTID 		= 	118; // v
	public static final byte AMPID 			= 	105; // i
	public static final byte MOTORRPMID 	=	109; // m
	public static final byte THROTTLEID 	=	116; // t
	public static final byte SPEEDMPHID 	=	115; // s
	public static final byte TEMP1ID 		= 	97;	 // a
	public static final byte TEMP2ID 		= 	98;	 // b
	public static final byte TEMP3ID 		= 	99;  // c

    public static final int DATA_SAVE_INTERVAL = 250; // save interval in milliseconds
	public static final int BT_DATA_TIMEOUT = 2000; // Bluetooth connection timeout in milliseconds

	public static final String DATA_FILE = "arduino.csv";

	/**********************/
	/* SETTINGS VARIABLES */
	/**********************/
	public enum MODE {DEMO, RACE}
	public static MODE Mode;

	public enum UNIT {MPH, KPH}
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