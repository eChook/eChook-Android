package com.ben.drivenbluetooth;

import android.app.AlarmManager;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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

	public static volatile int		StartBearing	=	0;
	public static volatile Double	StartLatitude	=	0.0;
	public static volatile Double	StartLongitude	=	0.0;

	public static 			int							maxGraphDataPoints	=	15 * 1000 / Global.UI_UPDATE_INTERVAL;
	public static 			float 						GraphTimeStamp 		=	0.0f;
	public static volatile 	LineGraphSeries<DataPoint> 	ThrottleHistory		=	new LineGraphSeries<>();
	public static volatile 	LineGraphSeries<DataPoint> 	AmpsHistory			=	new LineGraphSeries<>();
	public static volatile 	LineGraphSeries<DataPoint> 	VoltsHistory		=	new LineGraphSeries<>();
	public static volatile 	LineGraphSeries<DataPoint> 	MotorRPMHistory		=	new LineGraphSeries<>();
	public static volatile 	LineGraphSeries<DataPoint> 	SpeedHistory		=	new LineGraphSeries<>();
	public static volatile 	LineGraphSeries<DataPoint> 	TempC1History		=	new LineGraphSeries<>();

	public static volatile Double Latitude 		=	0.0;
	public static volatile Double Longitude		=	0.0;
	public static volatile Double Altitude		=	0.0;
	public static volatile Double Bearing		=	0.0;
	public static volatile Double SpeedGPS		=	0.0;
	public static volatile Double GPSTime		=	0.0;
	public static volatile Double Accuracy		=	0.0;
	public static volatile float DeltaDistance	=	0;	// difference between current and previous location in meters
	public static volatile int LocationUpdateCounter = 0;

	public static volatile float Gx = 0;	// Acceleration minus gravity in the x direction
	public static volatile float Gy = 0;	// Acceleration minus gravity in the y direction
	public static volatile float Gz = 0;	// Acceleration minus gravity in the z direction

	public static volatile int BTReconnectAttempts = 0;

	public static volatile long DataFileLength = 0;

	/* BLUETOOTH STATE TRACKER */
	public enum BTSTATE {DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING}
	public static volatile BTSTATE BTState = BTSTATE.DISCONNECTED;

	/**********************/
	/* VARIABLES          */
	/**********************/
	public static BluetoothSocket BTSocket;
	public static int MangledDataCount = 0;
	public static GregorianCalendar RaceStartTime;
	public static AlarmManager AlarmManager;

	/**********************/
	/* SENSORS            */
	/**********************/
	public static Sensor Gravity;

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

	public static final int 	UI_UPDATE_INTERVAL	= 100; // UI update interval in milliseconds
    public static final int 	DATA_SAVE_INTERVAL 	= 250; // save interval in milliseconds
	public static final String 	DATA_FILE 			= "arduino.csv";

	public static final int BT_DATA_TIMEOUT = 2000; // Bluetooth connection timeout in milliseconds

	public static final int LOCATION_INTERVAL 		= 10000; 	// Low speed location update interval in ms
	public static final int LOCATION_FAST_INTERVAL 	= 5000;		// High speed location update interval in ms

	public static final Double DEG_LATITUDE_KM	= 111.23063;	// 1 degree of latitude in KM (roughly) in the UK (between 51-53 degrees North)
	public static final Double DEG_LONGITUDE_KM	=  59.54930;	// 1 degree of longitude in KM (roughly) in the UK (between 51-53 degrees North)

	/**********************/
	/* SETTINGS VARIABLES */
	/**********************/
	public enum MODE {DEMO, RACE}
	public static MODE Mode;

	public enum UNIT {MPH, KPH}
	public static UNIT Unit;

	public enum LOCATION {DISABLED, ENABLED} // REMINDER: KEEP THESE CONSISTENT WITH ARRAYS.XML!
	public static LOCATION Location;

	public enum ACCELEROMETER {DISABLED, ENABLED}
	public static ACCELEROMETER Accelerometer;

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