package com.ben.drivenbluetooth;

import android.bluetooth.BluetoothSocket;
import android.location.Location;

import com.ben.drivenbluetooth.util.LapData;
import com.ben.drivenbluetooth.util.RunningAverage;
import com.github.mikephil.charting.data.LineData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GLOBAL
 *
 * This class is a singleton which holds all the global variables referenced by the application
 *
 */
public final class Global {
    /* Data identifier bytes
    /* SEE https://docs.google.com/a/jaguarlandrover.com/spreadsheets/d/1894rswb_CalcgParDVzyCzok7YSILqCtenP4maTdhaY/edit?usp=sharing */
    public static final byte    STARTBYTE               = 123; // ASCII code for '{'
    public static final byte    STOPBYTE                = 125; // ASCII code for '}'
    public static final int     PACKETLENGTH            = 5;   // { [id] [1] [2] }
    public static final byte    VOLTS_ID                = 118; // v
    public static final byte    VOLTS_AUX_ID            = 119; // w
    public static final byte    AMPS_ID                 = 105; // i
    public static final byte    MOTOR_RPM_ID            = 109; // m
    public static final byte    THR_INPUT_ID            = 116; // t
    public static final byte    THR_ACTUAL_ID           = 100; // d
    public static final byte    SPEED_MPS_ID            = 115; // s
    public static final byte    TEMP1ID                 = 97;  // a
    public static final byte    TEMP2ID                 = 98;  // b
    public static final byte    TEMP3ID                 = 99;  // c
    public static final byte    LAUNCH_MODE_ID          = 76;  // L
    public static final byte    GEAR_RATIO_ID           = 114; // r
    public static final byte    CYCLE_VIEW_ID           = 67;  // C
    public static final byte    LOOP_COUNTER_ID         = 108; // l
    public static final byte    THROTTLE_MODE_ID        = 110; // n
    public static final byte    BRAKE_ID                = 66;  // B
    public static final byte    FAN_STATUS_ID           = 70;  // F
    public static final byte    FAN_DUTY_ID             = 102; // f
    public static final byte    STEERING_ID             = 122; // z

    public static final int     DATA_SAVE_INTERVAL      = 250; // save interval in milliseconds

    public static final String  DATA_FILE               = "arduino.csv";
    public static final int     BT_DATA_TIMEOUT         = 2000; // Bluetooth connection timeout in milliseconds
    public static final int     LOCATION_INTERVAL       = 2000;    // Low speed location UpdateLocationSetting interval in ms
    public static final int     LOCATION_FAST_INTERVAL  = 1000;        // High speed location UpdateLocationSetting interval in ms
    public static final float   LAP_TRIGGER_RANGE       = 20f;  // locus around the start location to trigger a lap
    public static final int     MAP_UPDATE_INTERVAL     = 5000;
    public static final String  SOCKETADDRESS           = "exantas.me";
    public static final int     SOCKETPORT              = 8081;

    public static final int     PERMISSIONS_REQUEST     = 1;

    public static String        UDPPassword = "";

    public static final BlockingQueue<byte[]> BTStreamQueue = 	new LinkedBlockingQueue<>();
	public static volatile Double Volts 			    = 0.0;
    public static volatile Double VoltsAux			    = 0.0;
	public static volatile Double Amps 				    = 0.0;
	public static volatile Double InputThrottle 	    = 0.0;
	public static volatile Double ActualThrottle 	    = 0.0;
	public static volatile Double MotorRPM 			    = 0.0;
	public static volatile Double SpeedMPS              = 0.0;
	public static volatile Double TempC1 			    = 0.0;
	public static volatile Double TempC2 			    = 0.0;
	public static volatile Double TempC3 			    = 0.0;
	public static volatile Double GearRatio			    = 0.0;
	public static volatile Double AmpHours 			    = 0.0;
    public static volatile Double WattHours             = 0.0;
    public static volatile Double WattHoursPerMeter     = 0.0;
    public static volatile Double DistanceMeters        = 0.0;
    public static volatile Double SlopeGradient         = 0.0;
    public static long RaceStartTime                    = 0L;
    public static volatile Double SteeringAngle         = 0.0;
    public static volatile Double PerformanceMetric     = 0.0;
    public static volatile int Brake                    = 0;
    public static volatile int FanStatus                = 0;
    public static volatile Double FanDuty               = 0.0;
    public static final ArrayList<LapData> LapDataList  = new ArrayList<>();
	public static final RunningAverage AverageAmps 		= new RunningAverage(2); // 2 = number of decimal places
    public static final RunningAverage AverageSpeedMPS  = new RunningAverage(1);
    public static final	int MAX_GRAPH_DATA_POINTS       = 4 * 50; // 50 seconds of history on graphs assuming Arduino spews every 250ms


	public static final LineData ThrottleHistory	    = new LineData();
	public static final LineData AmpsHistory		    = new LineData();
    public static final LineData VoltsHistory		    = new LineData();
    public static final LineData MotorRPMHistory	    = new LineData();
    public static final LineData SpeedHistory		    = new LineData();
    public static final LineData TempC1History		    = new LineData();


	public static volatile Location StartFinishLineLocation;
	public static volatile Double StartFinishLineBearing = 0.0;
    public static volatile float BearingFromObserverToCar = 0f;
	public static volatile Double Latitude 		        = 0.0;
	public static volatile Double Longitude		        = 0.0;
	public static volatile Double Altitude		        = 0.0;
	public static volatile Double Bearing		        = 0.0;
	public static volatile Double SpeedGPS		        = 0.0;
	public static volatile Double GPSTime		        = 0.0;
	public static volatile Double GPSAccuracy 	        = 0.0;
	public static final float MinGPSAccuracy	        = 50.0f;
	public static volatile float DeltaDistance	        = 0;	// difference between current and previous location in meters
	public static volatile int Lap	                    = 0;
	public static String CarName                        = "";
	public static volatile float Gx                     = 0;	// Acceleration minus gravity in the x direction
	public static volatile float Gy                     = 0;	// Acceleration minus gravity in the y direction
	public static volatile float Gz                     = 0;	// Acceleration minus gravity in the z direction
	public static volatile int BTReconnectAttempts      = 0;
	public static volatile long DataFileLength          = 0;
	public static volatile BTSTATE BTState              = BTSTATE.DISCONNECTED;
	public static volatile BluetoothSocket BTSocket;
	public static int MangledDataCount                  = 0;
    public static volatile int Gear                     = 0;
    public static volatile int IdealGear                = 0;

    public static MODE Mode;
	public static UNIT Unit;
	public static LOCATION LocationStatus;
	public static ACCELEROMETER Accelerometer;
    public static THROTTLEMODE ThrottleMode;

	public static String BTDeviceName;
	public static boolean EnableGraphs                  = true;
	public static Double BatteryCapacityAh;
    public static boolean UDPEnabled                    = false;
    public static List<String> BTDeviceNames = new ArrayList<String>(248); //248 is the maximum number of devices it is possible to bond


    public static int MotorTeeth                        = 0;
    public static int[] WheelTeeth;

    /**********************/
    private Global() {
        try {
            // Initialize an empty queue
            BTStreamQueue.clear();

        } catch (Error e) {
            // Do nothing
        }
    }

    public enum BTSTATE {DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING}

    public enum MODE {DEMO, RACE}

    public enum UNIT {MPH, KPH, MPS, FFF, KNOT}

    public enum LOCATION {DISABLED, ENABLED} // REMINDER: KEEP THESE CONSISTENT WITH ARRAYS.XML!

    public enum ACCELEROMETER {DISABLED, ENABLED}

    public enum THROTTLEMODE {THROTTLE, CURRENT}
}