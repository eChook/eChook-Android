package com.ben.drivenbluetooth;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.jjoe64.graphview.GraphView;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity
		extends 	Activity
		implements 	GraphViewFragment.OnFragmentInteractionListener,
					SixGraphsBars.OnFragmentInteractionListener
{

	/************* UI ELEMENTS ***************/
	public static TextView myLabel;
	public static TextView myMode;

	public static TextView myDataFileSize;
	public static TextView myDataFileName;

	public static TextView myBTState;
	public static TextView myLogging;

	public static TextView Throttle;
	public static TextView Current;
	public static TextView Voltage;
	public static TextView Temp1;
	public static TextView RPM;
	public static TextView Speed;

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
	public static Button settingsButton;

	public static GraphView myThrottleGraph;
	public static GraphView myVoltsGraph;
	public static GraphView myAmpsGraph;
	public static GraphView myMotorRPMGraph;
	public static GraphView myTempC1Graph;
	public static GraphView mySpeedGraph;

	private enum UISTATE {SIX_GRAPHS_BARS, FOUR_GRAPHS_BARS}
	private UISTATE UIState = UISTATE.SIX_GRAPHS_BARS;

	/************* THREADS ******************/
	public static RandomGenerator Gen = new RandomGenerator();
	public static BTDataParser Parser = new BTDataParser();
	public static DataToCsvFile DataSaver = new DataToCsvFile();
	public static BTStreamReader StreamReader; // initialize below

	/************* UI UPDATER ***************/
	private static Timer UIUpdateTimer; // don't initialize because it should be done below
	private static TimerTask UIUpdateTask; // initialized later on

	/************* UI THREAD HANDLER ********/
	public static Handler MainActivityHandler = new Handler();

	/***** LOCATION CLASS *******************/
	public static DrivenLocation myDrivenLocation;

	/******** SENSOR MANAGER ****************/
	public static Accelerometer myAccelerometer;

	/************* OTHER ***************/
	private static final int RESULT_SETTINGS = 2;
	private static Context context;
	static final BluetoothManager myBluetoothManager = new BluetoothManager();


	/**************************************************/
	/**************** MAINACTIVITY ONCREATE ***********/
	/**************************************************/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		RelativeLayout RL = (RelativeLayout) findViewById(R.id.CenterView);
		View child = getLayoutInflater().inflate(R.layout.six_graphs_bars, null);
		RL.addView(child);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		/************** INITIALIZE UI ELEMENTS ************/
		InitializeButtons();
		InitializeDataFields();
		InitializeDataBars();
		InitializeGraphs();

		/* OTHERS */
		myLabel 		= (TextView) findViewById(R.id.label);
		myMode 			= (TextView) findViewById(R.id.txt_Mode);

		myDataFileName	= (TextView) findViewById(R.id.txtDataFileName);
		myDataFileSize	= (TextView) findViewById(R.id.txtDataFileSize);

		myBTState		= (TextView) findViewById(R.id.txtBTState);
		myLogging		= (TextView) findViewById(R.id.txtLogging);

		StartUIUpdater(0, Global.UI_UPDATE_INTERVAL);

		InitializeGlobalSettings();

		/**************** CONTEXT *************************/
		MainActivity.context = getApplicationContext();

		/**************** ALARM MANAGER *******************/
		Global.AlarmManager = (AlarmManager) MainActivity.getAppContext().getSystemService(Context.ALARM_SERVICE);

		/********* INITIALIZE LOCATION CLASS **************/
		myDrivenLocation = new DrivenLocation();

		/******************* ACCELEROMETER ****************/
		myAccelerometer = new Accelerometer((SensorManager) getSystemService(Context.SENSOR_SERVICE));

		/**************** TIMEPICKER **********************/
		//RaceStartTime.setOnClickListener(new SetTimeDialog(RaceStartTime));

		UpdateDataFileInfo();

		MainActivity.myLogging.setText("NO");
		MainActivity.myLogging.setTextColor(Color.RED);

		Parser.start();
	}

	@Override
	protected void onStart() {
		super.onStart();
		myDrivenLocation.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		myDrivenLocation.disconnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		myAccelerometer.update();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myAccelerometer.stopAccelerometerData();
	}

	private void InitializeDataFields() {
		Throttle 		= (TextView) findViewById(R.id.throttle);
		Current 		= (TextView) findViewById(R.id.current);
		Voltage 		= (TextView) findViewById(R.id.voltage);
		Temp1 			= (TextView) findViewById(R.id.temp1);
		RPM 			= (TextView) findViewById(R.id.rpm);
		Speed 			= (TextView) findViewById(R.id.speed);
	}

	private void InitializeGraphs() {
		myThrottleGraph	= (GraphView) findViewById(R.id.throttleGraph);
		myThrottleGraph.addSeries(Global.ThrottleHistory);
		myThrottleGraph.getViewport().setYAxisBoundsManual(true);
		myThrottleGraph.getViewport().setMinY(0.0);
		myThrottleGraph.getViewport().setMaxY(100.0);
		myThrottleGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		myVoltsGraph = (GraphView) findViewById(R.id.voltsGraph);
		//myVoltsGraph.addSeries(Global.VoltsHistory);
		myVoltsGraph.getViewport().setYAxisBoundsManual(true);
		myVoltsGraph.getViewport().setMinY(0.0);
		myVoltsGraph.getViewport().setMaxY(28.0);
		myVoltsGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		myAmpsGraph = (GraphView) findViewById(R.id.ampsGraph);
		//myAmpsGraph.addSeries(Global.AmpsHistory);
		myAmpsGraph.getViewport().setYAxisBoundsManual(true);
		myAmpsGraph.getViewport().setMinY(0.0);
		myAmpsGraph.getViewport().setMaxY(40.0);
		myAmpsGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		myTempC1Graph = (GraphView) findViewById(R.id.T1Graph);
		//myTempC1Graph.addSeries(Global.TempC1History);
		myTempC1Graph.getViewport().setYAxisBoundsManual(true);
		myTempC1Graph.getViewport().setMinY(0.0);
		myTempC1Graph.getViewport().setMaxY(100.0);
		myTempC1Graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		myMotorRPMGraph = (GraphView) findViewById(R.id.RPMGraph);
		//myMotorRPMGraph.addSeries(Global.MotorRPMHistory);
		myMotorRPMGraph.getViewport().setYAxisBoundsManual(true);
		myMotorRPMGraph.getViewport().setMinY(0.0);
		myMotorRPMGraph.getViewport().setMaxY(2500);
		myMotorRPMGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		mySpeedGraph = (GraphView) findViewById(R.id.SpeedGraph);
		//mySpeedGraph.addSeries(Global.SpeedHistory);
		mySpeedGraph.getViewport().setYAxisBoundsManual(true);
		mySpeedGraph.getViewport().setMinY(0.0);
		if (Global.Unit == Global.UNIT.MPH) { mySpeedGraph.getViewport().setMaxY(50.0); }
		if (Global.Unit == Global.UNIT.KPH) { mySpeedGraph.getViewport().setMaxY(80.0); }
		mySpeedGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
	}

	private void InitializeDataBars() {
		ThrottleBar 	= (DataBar) findViewById(R.id.ThrottleBar);
		CurrentBar 		= (DataBar) findViewById(R.id.CurrentBar);
		VoltageBar 		= (DataBar) findViewById(R.id.VoltageBar);
		T1Bar 			= (DataBar) findViewById(R.id.T1Bar);
		RPMBar 			= (DataBar) findViewById(R.id.RPMBar);
		SpeedBar 		= (DataBar) findViewById(R.id.SpeedBar);
	}

	private void InitializeButtons() {
		openBTButton 	= (Button) findViewById(R.id.open);
		startButton 	= (Button) findViewById(R.id.start);
		stopButton 		= (Button) findViewById(R.id.stop);
		closeBTButton	= (Button) findViewById(R.id.close);
	}

	public void CycleView(View v) {
		try {
			int state = UIState.ordinal();
			if (++state >= UISTATE.values().length) {
				state = 0;
			}
			UIState = UISTATE.values()[state];
			RelativeLayout RL = (RelativeLayout) findViewById(R.id.CenterView);
			RL.removeAllViews();
			View child = getLayoutInflater().inflate(R.layout.four_graphs_bars, null);
			RL.addView(child);
		} catch (Exception e) {
			e.getMessage();
		}
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
		PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			int mode = Integer.valueOf(prefs.getString("prefMode", ""));
			Global.Mode = Global.MODE.values()[mode];
			myMode.setText(Global.MODE.values()[mode].name());

			int units = Integer.valueOf(prefs.getString("prefSpeedUnits", ""));
			Global.Unit = Global.UNIT.values()[units];

			int location = Integer.valueOf(prefs.getString("prefLocation", ""));
			Global.Location = Global.LOCATION.values()[location];
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
			StartStreamReader();
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

	public void Start(View v) {
		try {
			if (Global.Mode == Global.MODE.DEMO) {
				StartDemoMode();
			} else if (Global.Mode == Global.MODE.RACE) {
				StartRaceMode();
			}
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	public void Stop(View v) {
		try {
			StopRandomGenerator();
			StopDataSaver();

			myLabel.setText("Stopped logging");
			MainActivity.myLogging.setText("NO");
			MainActivity.myLogging.setTextColor(Color.RED);

			UpdateDataFileInfo();

		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	public void ForceStop(View v) {
		try {
			StopRandomGenerator();
			StopDataSaver();
			StopStreamReader();

			myLabel.setText("Stopped logging");
			MainActivity.myLogging.setText("NO");
			MainActivity.myLogging.setTextColor(Color.RED);

			UpdateDataFileInfo();

			UIUpdateTimer.purge();

		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	public void LaunchSettings(View v) {
		SettingsFragment settingsFragment = new SettingsFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.graphview_overlay, settingsFragment)
				.addToBackStack(null)
				.commit();
	}

	public void ViewGraph(View v) {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.add(R.id.graphview_overlay, new GraphViewFragment())
				.addToBackStack(null)
				.commit();
	}

	/**************************************************/
	/**************** THREADS          ****************/
	/**************************************************/

	private void StartRaceMode() {
		StartStreamReader();
		StartDataSaver();
	}

	private void StartDemoMode() {
		StartRandomGenerator();
	}

	private static void StartDataSaver() {
		if (!DataSaver.isAlive() && DataSaver.getState() != Thread.State.NEW) {
			DataSaver = new DataToCsvFile();
		}
		DataSaver.start();
		MainActivity.myLogging.setText("LOGGING");
		MainActivity.myLogging.setTextColor(Color.GREEN);
	}

	private static void StartStreamReader() {
		if (StreamReader == null) {
			StreamReader = new BTStreamReader();
		} else if (!StreamReader.isAlive() && StreamReader.getState() != Thread.State.NEW) {
			StreamReader = new BTStreamReader();
		}
		StreamReader.start();
	}

	private static void StartRandomGenerator() {
		if (Gen == null) {
			Gen = new RandomGenerator();
		} else if (!Gen.isAlive() && Gen.getState() != Thread.State.NEW) {
			Gen = new RandomGenerator();
		}
		Gen.start();
	}

	private static void StartUIUpdater(int delay, int uiUpdateInterval) {
		UIUpdateTask = new TimerTask() {
			public void run() {
				MainActivityHandler.post(new UIUpdateRunnable());
			}
		};
		UIUpdateTimer = new Timer();
		UIUpdateTimer.schedule(UIUpdateTask, delay, uiUpdateInterval);
	}

	private static void StopDataSaver() {
		if (DataSaver != null && DataSaver.getState() != Thread.State.TERMINATED) {
			DataSaver.cancel();
		}
	}

	private static void StopRandomGenerator() {
		if (Gen != null && Gen.getState() != Thread.State.TERMINATED) {
			Gen.cancel();
		}
	}

	private static void StopStreamReader() {
		if (StreamReader != null && StreamReader.getState() != Thread.State.TERMINATED) {
			StreamReader.cancel();
		}
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
			PendingIntent pendingIntent;
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
	/**************** OTHER SHIT          *************/
	/**************************************************/

	public static Context getAppContext() {
		return MainActivity.context;
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

	private static void UpdateDataFileInfo() {
		File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
		MediaScannerConnection.scanFile(MainActivity.getAppContext(), new String[]{f.getAbsolutePath()}, null, null);
		Global.DataFileLength = f.length();
		myDataFileName.setText(Global.DATA_FILE);
	}
}
