package com.driven.rowan.drivenbluetooth;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity
		extends Activity
		implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, SensorEventListener {

	/************* UI ELEMENTS ***************/
	public static TextView myLabel;
	public static TextView myMode;
	public static TextView SMSZone;
	public static TextView myLongitude;
	public static TextView myLatitude;

	public static TextView myGx;
	public static TextView myGy;
	public static TextView myGz;

	public static EditText Throttle;
	public static EditText Current;
	public static EditText Voltage;
	public static EditText Temp1;
	public static EditText RPM;
	public static EditText Speed;

	public static EditText RaceStartTime;

	public static DataBar ThrottleBar;
	public static DataBar CurrentBar;
	public static DataBar VoltageBar;
	public static DataBar T1Bar;
	public static DataBar RPMBar;
	public static DataBar SpeedBar;

	public static Button openBTButton;
	public static Button startButton;
	public static Button stopButton;
	public static Button closeBTButton;

	/************* THREADS ******************/
	public static RandomGenerator Gen = new RandomGenerator();
	public static BTDataParser Parser = new BTDataParser();
	public static DataToCsvFile DataSaver = new DataToCsvFile();
	public static BTStreamReader StreamReader; // initialize below

	/************* UI UPDATER ***************/
	private Timer UIUpdateTimer; // don't initialize because it should be done below
	private TimerTask UIUpdateTask; // initialized later on

	/************* UI THREAD HANDLER ********/
	public static Handler MainActivityHandler = new Handler();

	/***** GOOGLE LOCATION API **************/
	public static GoogleApiClient GoogleApi;
	public Location CurrentLocation;
	public Location PreviousLocation;
	public String mLastUpdateTime;
	public Boolean mRequestingLocationUpdates = true;
	private LocationRequest mLocationRequest;

	/******** SENSOR MANAGER ****************/
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private boolean supportsAccelerometer = false;
	private float filterX[] = new float[3];
	private float filterY[] = new float[3];
	private float filterZ[] = new float[3];

	/************* OTHER ***************/
	private static final int RESULT_SETTINGS = 2;
	private static Context context;
	static final BluetoothManager myBluetoothManager = new BluetoothManager();
	private PendingIntent pendingIntent;

	String TAG = "DrivenBluetooth";

	/**************************************************/
	/**************** MAINACTIVITY ONCREATE ***********/
	/**************************************************/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		/* BUTTONS */
		openBTButton 	= (Button) findViewById(R.id.open);
		startButton 	= (Button) findViewById(R.id.start);
		stopButton 		= (Button) findViewById(R.id.stop);
		closeBTButton	= (Button) findViewById(R.id.close);

		/* LABELS */
		myLabel 		= (TextView) findViewById(R.id.label);
		myMode 			= (TextView) findViewById(R.id.txt_Mode);

		myLongitude 	= (TextView) findViewById(R.id.txtLongitude);
		myLatitude 		= (TextView) findViewById(R.id.txtLatitude);

		/* ACCELEROMETER VALUES */
		myGx			= (TextView) findViewById(R.id.txtGx);
		myGy			= (TextView) findViewById(R.id.txtGy);
		myGz			= (TextView) findViewById(R.id.txtGz);

		/* DATA FIELDS */
		Throttle 		= (EditText) findViewById(R.id.throttle);
		Current 		= (EditText) findViewById(R.id.current);
		Voltage 		= (EditText) findViewById(R.id.voltage);
		Temp1 			= (EditText) findViewById(R.id.temp1);
		RPM 			= (EditText) findViewById(R.id.rpm);
		Speed 			= (EditText) findViewById(R.id.speed);

		RaceStartTime 	= (EditText) findViewById(R.id.raceStartTime);
		//SMSZone 		= (TextView) findViewById(R.id.smsBox);

		/* FILL BARS */
		ThrottleBar 	= (DataBar) findViewById(R.id.ThrottleBar);
		CurrentBar 		= (DataBar) findViewById(R.id.CurrentBar);
		VoltageBar 		= (DataBar) findViewById(R.id.VoltageBar);
		T1Bar 			= (DataBar) findViewById(R.id.T1Bar);
		RPMBar 			= (DataBar) findViewById(R.id.RPMBar);
		SpeedBar 		= (DataBar) findViewById(R.id.SpeedBar);


		/************** INITIALIZE SETTINGS ***************/
		this.InitializeGlobalSettings();

		/**************** CONTEXT *************************/
		MainActivity.context = getApplicationContext();

		/**************** ALARM MANAGER *******************/
		Global.AlarmManager = (AlarmManager) MainActivity.getAppContext().getSystemService(Context.ALARM_SERVICE);

		/*************** GOOGLE LOCATION API **************/
		createLocationRequest();
		buildGoogleApiClient();

		/********* SENSOR MANAGER ****************/
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		InitializeAccelerometer();

		/**************** TIMEPICKER ***********************/
		RaceStartTime.setOnClickListener(new SetTimeDialog(RaceStartTime));

		/**************** UI UPDATER ***********************/
		UIUpdateTask = new TimerTask() {
			public void run() {
				MainActivityHandler.post(new UIUpdateRunnable());
			}
		};
		UIUpdateTimer = new Timer();
		UIUpdateTimer.schedule(UIUpdateTask, 0, Global.UI_UPDATE_INTERVAL);
	}

	/**************************************************/
	/**************** TOASTER          ****************/
	/**************************************************/

	public void showMessage(String theMsg) {
		Toast msg = Toast.makeText(getBaseContext(),
				theMsg, (Toast.LENGTH_LONG));
		msg.show();
	}

	public static void showMessage(Context context, String string, int length) {
		final Toast msg = Toast.makeText(context, string, length);
		msg.show();

		if (length < 2000) {
			MainActivityHandler.postDelayed(new Runnable() {
				public void run() {
					msg.cancel();
				}
			}, length);
		}
	}

	/**************************************************/
	/**************** SETTINGS         ****************/
	/**************************************************/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.btnSettings) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void InitializeGlobalSettings() {
		// Initialize the settings variables
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			int mode = Integer.valueOf(prefs.getString("prefMode", ""));
			Global.Mode = Global.MODE.values()[mode];
			myMode.setText(Global.MODE.values()[mode].name());

			int units = Integer.valueOf(prefs.getString("prefSpeedUnits", ""));
			Global.Unit = Global.UNIT.values()[units];
		} catch (Exception e) {
			showMessage("Could not retrieve settings");
		}
	}

	/**************************************************/
	/**************** BUTTON LISTENERS ****************/
	/**************************************************/

	public void OpenBT(View v) {
		try {
			myBluetoothManager.findBT();
			myBluetoothManager.openBT();
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	public void CloseBT(View v) {
		try {
			myBluetoothManager.closeBT();
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	public void StartAllThreads(View v) {
		try {
			if (Global.Mode == Global.MODE.DEMO) {
				if (Gen == null) {
					Gen = new RandomGenerator();
				}
				if (Gen.getState() != Thread.State.NEW) {
					Gen = new RandomGenerator();
				}
				Gen.start();

				if (Parser.getState() != Thread.State.NEW) {
					Parser = new BTDataParser();
				}
				Parser.start();

				if (DataSaver.getState() != Thread.State.NEW) {
					DataSaver = new DataToCsvFile();
				}
				DataSaver.start();

				// UI Updater
				UIUpdateTask = new TimerTask() {
					public void run() {
						MainActivityHandler.post(new UIUpdateRunnable());
					}
				};
				UIUpdateTimer = new Timer();
				UIUpdateTimer.schedule(UIUpdateTask, 250, 250);

				myLabel.setText("Now Logging - press 'Stop' to cancel");

			} else if (Global.Mode == Global.MODE.RACE && Global.BTSocket != null) {
				if (StreamReader == null) {
					StreamReader = new BTStreamReader();
				}
				if (StreamReader.getState() != Thread.State.NEW) {
					StreamReader = new BTStreamReader();
				}
				StreamReader.start();

				if (Parser.getState() != Thread.State.NEW) {
					Parser = new BTDataParser();
				}
				Parser.start();

				if (DataSaver.getState() != Thread.State.NEW) {
					DataSaver = new DataToCsvFile();
				}
				DataSaver.start();

			}
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	public void CancelAllThreads(View v) {
		try {
			if (Gen != null && Gen.getState() != Thread.State.TERMINATED) {
				Gen.cancel();
			}
			if (StreamReader != null && StreamReader.getState() != Thread.State.TERMINATED) {
				StreamReader.cancel();
			}
			if (Parser != null && Parser.getState() != Thread.State.TERMINATED) {
				Parser.cancel();
			}
			if (DataSaver != null && DataSaver.getState() != Thread.State.TERMINATED) {
				DataSaver.cancel();
			}

			myLabel.setText("Stopped logging");

			// scan for the data file to ensure it can be viewed from a computer
			File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
			MediaScannerConnection.scanFile(MainActivity.getAppContext(), new String[]{f.getAbsolutePath()}, null, null);

		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	public void LaunchSettings(View v) {
		Intent i = new Intent(this, SettingsActivity.class);
		startActivityForResult(i, RESULT_SETTINGS);
	}

	public class SetTimeDialog implements View.OnClickListener {
		EditText editText;

		public SetTimeDialog(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void onClick(View v) {
			Calendar mcurrentTime = Calendar.getInstance();
			int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
			int minute = mcurrentTime.get(Calendar.MINUTE);
			TimePickerDialog mTimePicker;
			mTimePicker = new TimePickerDialog(MainActivity.this, t, hour, minute, true);//true for 24 hour time
			mTimePicker.setTitle("Select Time");
			mTimePicker.show();
		}

		private TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// %02d sets the string length to 2 and pads with zeros if necessary
				editText.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
				Calendar mcurrentTime = Calendar.getInstance();
				int year = mcurrentTime.get(Calendar.YEAR);
				int month = mcurrentTime.get(Calendar.MONTH);
				int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);
				// Race countdown timer starts 10 seconds before so minute - 1, 50 seconds
				Global.RaceStartTime = new GregorianCalendar(year, month, day, hourOfDay, minute, 0);
				setRaceNotifier();
			}
		};

		private void setRaceNotifier() {
			Intent myIntent = new Intent(MainActivity.this, RaceNotifier.class);
			pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
			Global.AlarmManager.set(AlarmManager.RTC, Global.RaceStartTime.getTimeInMillis(), pendingIntent);
		}
	}

	public static class RaceNotifier extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Race will start in 10 seconds

		}
	}

	/**************************************************/
	/**************** LOCATION STUFF      *************/
	/**************************************************/

	protected synchronized void buildGoogleApiClient() {
		GoogleApi = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	protected void createLocationRequest() {
		this.mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(Global.LOCATION_INTERVAL);
		mLocationRequest.setFastestInterval(Global.LOCATION_FAST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleApi.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (GoogleApi.isConnected()) {
			GoogleApi.disconnect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (mRequestingLocationUpdates) {
			startLocationUpdates();
		}
		CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApi);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		GoogleApi.connect();
	}

	protected void startLocationUpdates() {
		try {
			LocationServices.FusedLocationApi.requestLocationUpdates(
					GoogleApi, mLocationRequest, this);
		} catch (Exception e) {
			e.toString();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		PreviousLocation = CurrentLocation;
		CurrentLocation = location;
		Global.Latitude = location.getLatitude();
		Global.Longitude = location.getLongitude();
		Global.Altitude = location.getAltitude();
		Global.Bearing = (double) location.getBearing();
		Global.SpeedGPS = (double) location.getSpeed();
		Global.GPSTime = (double) location.getTime();
		Global.Accuracy = (double) location.getAccuracy();

		Global.DeltaDistance = calculateDistanceBetween(PreviousLocation, CurrentLocation);

		mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		Global.LocationUpdateCounter++;
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				GoogleApi, this);
	}

	protected float calculateDistanceBetween(Location location1, Location location2) {
		return location1.distanceTo(location2);
	}

	/**************************************************/
	/**************** SENSORS             *************/
	/**************************************************/

	public void InitializeAccelerometer() {
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
			List<Sensor> gravSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
			if (gravSensors.size() > 0) {
				mAccelerometer = gravSensors.get(0);
				supportsAccelerometer = true;
			} else {
				showMessage("Device does not support accelerometer");
				supportsAccelerometer = false;
			}
		} else {
			showMessage("Device does not support accelerometer");
			supportsAccelerometer = false;
		}
	}
	public void startAccelerometerData() {
		if (supportsAccelerometer) {
			try {
				mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			} catch (Exception e) {
				showMessage(e.toString());
			}
		}
	}

	public void stopAccelerometerData() {
		try {
			mSensorManager.unregisterListener(this);
		} catch (Exception e) {
			showMessage(e.toString());
		}
	}

	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do something here if sensor accuracy changes.
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		// The linear accelerometer returns 3 values
		// smoothing using a filter


		/*

													^ -y to top of screen
			-------------	^ to top of screen		|   / +z into the screen
			|			|	| y is negative 		|  /
			|			|							| /
			|			|							--------> -x to right of screen
			|			|
			|			|
			|			|
			|			|
			-------------








		 */
		final float alpha = 0.9f;

		Global.Gx = event.values[0] * alpha + Global.Gx * (1.0f - alpha);
		Global.Gy = event.values[1] * alpha + Global.Gy * (1.0f - alpha);
		Global.Gz = event.values[2] * alpha + Global.Gz * (1.0f - alpha);

		Global.Gx = (float)Math.round(event.values[0] * alpha + Global.Gx * (1.0f - alpha) * 1000) / 1000;
		Global.Gy = (float)Math.round(event.values[1] * alpha + Global.Gy * (1.0f - alpha) * 1000) / 1000;
		Global.Gz = (float)Math.round(event.values[2] * alpha + Global.Gz * (1.0f - alpha) * 1000) / 1000;
	}

	@Override
	protected void onResume() {
		super.onResume();
		startAccelerometerData();

	}

	@Override
	protected void onPause() {
		super.onPause();
		stopAccelerometerData();
	}

	/**************************************************/
	/**************** OTHER SHIT          *************/
	/**************************************************/

	public static Context getAppContext() {
		return MainActivity.context;
	}
}
