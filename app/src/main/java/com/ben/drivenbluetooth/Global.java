package com.ben.drivenbluetooth;

import android.bluetooth.BluetoothSocket;
import android.location.Location;

import com.ben.drivenbluetooth.util.LapData;
import com.ben.drivenbluetooth.util.RunningAverage;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
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
	public static volatile Double Volts 			= 0.0;
	public static volatile Double Amps 				= 0.0;
	public static volatile Double InputThrottle 	= 0.0;
	public static volatile Double ActualThrottle 	= 0.0;
	public static volatile Double MotorRPM 			= 0.0;
	public static volatile Double SpeedMPH 			= 0.0;
	public static volatile Double SpeedKPH 			= 0.0;
	public static volatile Double TempC1 			= 0.0;
	public static volatile Double TempC2 			= 0.0;
	public static volatile Double TempC3 			= 0.0;
	public static volatile Double GearRatio			= 0.0;
	public static volatile Double AmpHours 			= 0.0;

	public static volatile ArrayList<LapData> LapDataList = new ArrayList<>();

	public static volatile RunningAverage AverageAmps 		= new RunningAverage(2); // 2 = number of decimal places
	public static volatile RunningAverage AverageSpeedMPH 	= new RunningAverage(1);

	public static 			int		maxGraphDataPoints	=	120;
	public static volatile 	LineData ThrottleHistory	=	new LineData();
	public static volatile 	LineData AmpsHistory		=	new LineData();
	public static volatile 	LineData VoltsHistory		=	new LineData();
	public static volatile 	LineData MotorRPMHistory	=	new LineData();
	public static volatile 	LineData SpeedHistory		=	new LineData();
	public static volatile 	LineData TempC1History		=	new LineData();

	public static volatile Location StartFinishLineLocation;
	public static volatile Double StartFinishLineBearing;

	public static volatile Double Latitude 		=	0.0;
	public static volatile Double Longitude		=	0.0;
	public static volatile Double Altitude		=	0.0;
	public static volatile Double Bearing		=	0.0;
	public static volatile Double SpeedGPS		=	0.0;
	public static volatile Double GPSTime		=	0.0;
	public static volatile Double GPSAccuracy 	=	0.0;
	public static		   float MinGPSAccuracy	=	20.0f;
	public static volatile float DeltaDistance	=	0;	// difference between current and previous location in meters

	public static volatile int Lap	= 0;

	public static String CarName = "";

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

	/**********************/
	/* CONSTANTS          */
	/**********************/
	public static final byte STARTBYTE 		= 	123; // ASCII Code for '{'
    public static final byte STOPBYTE 		= 	125; // ASCII code for '}'
    public static final int PACKETLENGTH 	= 	5; 	 // { [id] [1] [2] }
                                              	 // 1   2   3   4  5

	public static final byte VOLTS_ID				= 	118;	// v
	public static final byte AMPS_ID				= 	105;	// i
	public static final byte MOTOR_RPM_ID			=	109;	// m
	public static final byte THR_INPUT_ID			=	116;	// t
	public static final byte THR_ACTUAL_ID			=	100;	// d
	public static final byte SPEED_MPH_ID			=	115;	// s
	public static final byte TEMP1ID 				= 	97;		// a
	public static final byte TEMP2ID 				= 	98;		// b
	public static final byte TEMP3ID 				= 	99; 	// c
	public static final byte LAUNCH_MODE_ID			=	76;		// L
	public static final byte GEAR_RATIO_ID			=	114;	// r
	public static final byte CYCLE_VIEW_ID			=	67;		// C

	public static final int 	FAST_UI_UPDATE_INTERVAL = 100; // UI update interval in milliseconds
	public static final int		SLOW_UI_UPDATE_INTERVAL	= 500;
    public static final int 	DATA_SAVE_INTERVAL 		= 250; // save interval in milliseconds
	public static final String 	DATA_FILE 				= "arduino.csv";

	public static final int BT_DATA_TIMEOUT = 5000; // Bluetooth connection timeout in milliseconds

	public static final int LOCATION_INTERVAL 		= 2000; 	// Low speed location update interval in ms
	public static final int LOCATION_FAST_INTERVAL 	= 1000;		// High speed location update interval in ms

	public static final float LAP_TRIGGER_RANGE = 20f;  // locus around the start location to trigger a lap

	public static final int MAP_UPDATE_INTERVAL = 5000;

	/**********************/
	/* SETTINGS VARIABLES */
	/**********************/
	public enum MODE {DEMO, RACE}
	public static MODE Mode;

	public enum UNIT {MPH, KPH}
	public static UNIT Unit;

	public enum LOCATION {DISABLED, ENABLED} // REMINDER: KEEP THESE CONSISTENT WITH ARRAYS.XML!
	public static LOCATION LocationStatus;

	public enum ACCELEROMETER {DISABLED, ENABLED}
	public static ACCELEROMETER Accelerometer;

	public static String BTDeviceName;

	public static Double BatteryCapacityAh;

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