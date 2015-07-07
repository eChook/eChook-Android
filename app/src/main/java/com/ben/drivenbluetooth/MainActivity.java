package com.ben.drivenbluetooth;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.ben.drivenbluetooth.fragments.FourGraphsBars;
import com.ben.drivenbluetooth.fragments.MainMapFragment;
import com.ben.drivenbluetooth.fragments.SettingsFragment;
import com.ben.drivenbluetooth.fragments.SixGraphsBars;
import com.ben.drivenbluetooth.threads.BTDataParser;
import com.ben.drivenbluetooth.threads.BTStreamReader;
import com.ben.drivenbluetooth.threads.DataToCsvFile;
import com.ben.drivenbluetooth.threads.RandomGenerator;
import com.ben.drivenbluetooth.util.Accelerometer;
import com.ben.drivenbluetooth.util.BluetoothManager;
import com.ben.drivenbluetooth.util.CyclingArrayList;
import com.ben.drivenbluetooth.util.DrivenLocation;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity
		extends 	Activity
		implements BTDataParser.BTDataParserListener
{

	public static TextView myMode;

	public static TextView myDataFileSize;
	public static TextView myDataFileName;

	public static TextView myBTState;
	public static TextView myLogging;

	public static Button stopButton;

	public static Button LaunchModeButton;
	public static Button RaceStartButton;

	public static Chronometer LapTimer;
	public static TextView prevLapTime;
	public static TextView LapNumber;

	public static RandomGenerator Gen = new RandomGenerator();
	public BTDataParser Parser = new BTDataParser(this); // can't be static because of (this)
	public static DataToCsvFile DataSaver = new DataToCsvFile();
	public static BTStreamReader StreamReader; // initialize below

	private static Timer UIUpdateTimer; // don't initialize because it should be done below

	public static final Handler MainActivityHandler = new Handler();

	public static DrivenLocation myDrivenLocation; // must be initialized below or else null object ref error

	public static Accelerometer myAccelerometer;

	private static Context context;
	public static final BluetoothManager myBluetoothManager = new BluetoothManager();

	private static final CyclingArrayList<Fragment> FragmentList = new CyclingArrayList<>();

	/**************************************************/
	/**************** MAINACTIVITY ONCREATE ***********/
	/**************************************************/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		/************** INITIALIZE UI ELEMENTS ************/

		/* OTHERS */
		myMode 			= (TextView) findViewById(R.id.txt_Mode);

		myDataFileName	= (TextView) findViewById(R.id.txtDataFileName);
		myDataFileSize	= (TextView) findViewById(R.id.txtDataFileSize);

		myBTState		= (TextView) findViewById(R.id.txtBTState);
		myLogging		= (TextView) findViewById(R.id.txtLogging);

		LapTimer		= (Chronometer) findViewById(R.id.LapTimer);
		prevLapTime		= (TextView) findViewById(R.id.previousLapTime);
		LapNumber		= (TextView) findViewById(R.id.lap);

		StartUIUpdater(0, Global.SLOW_UI_UPDATE_INTERVAL);

		InitializeGlobalSettings();

		/**************** CONTEXT *************************/
		context = getApplicationContext();

		/******************* ACCELEROMETER ****************/
		myAccelerometer = new Accelerometer((SensorManager) getSystemService(Context.SENSOR_SERVICE));

		myDrivenLocation = new DrivenLocation(); // must be initialized here or else null object ref error

		UpdateDataFileInfo();

		MainActivity.myLogging.setText("NO");
		MainActivity.myLogging.setTextColor(Color.RED);

		InitializeFragmentList();
		CycleView();
		StartDataParser();
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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void InitializeFragmentList() {
		FragmentList.add(new MainMapFragment());
		FragmentList.add(new SixGraphsBars());
		FragmentList.add(new FourGraphsBars());
	}

	public void CycleView() {
		try {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			Fragment fragment = FragmentList.cycle();
			fragmentTransaction.replace(R.id.CenterView, fragment);
			fragmentTransaction.commit();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void ActivateLaunchMode() {
		// reset the lap timer & lap counters
		Global.Lap = 0;
		LapTimer.stop();
		LapTimer.setBase(SystemClock.elapsedRealtime());
		myDrivenLocation.ActivateLaunchMode();
		showMessage("Launch Mode Activated - waiting for throttle input (minimum 20% required)", Toast.LENGTH_LONG);
	}

	/**************************************************/
	/**************** TOASTER          ****************/
	/**************************************************/

	public void showMessage(String theMsg) {
		Toast msg = Toast.makeText(getBaseContext(),
				theMsg, (Toast.LENGTH_LONG));
		msg.show();
	}

	public static void showMessage(String string, int length) {
		final Toast msg = Toast.makeText(context, string, Toast.LENGTH_LONG);
		msg.show();
	}

	public static void showError(Exception e) {
		showMessage(e.getMessage(),Toast.LENGTH_LONG);
	}

	/**************************************************/
	/**************** SETTINGS         ****************/
	/**************************************************/

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
			Global.LocationStatus = Global.LOCATION.values()[location];

			Global.BTDeviceName = prefs.getString("prefBTDeviceName", "");
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
			StopStreamReader();
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

	public void Cycle(View v) {
		CycleView();
	}

	public void LaunchMode(View v) {
		ActivateLaunchMode();
	}

	public void RaceStart(View v) {
		myDrivenLocation.SimulateRaceStart();
	}

	public void CrossFinishLine(View v) {
		myDrivenLocation.SimulateCrossStartFinishLine();
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

	private void StartDataSaver() {
		try {
			if (!DataSaver.isAlive() && DataSaver.getState() != Thread.State.NEW) {
				DataSaver = new DataToCsvFile();
			}
			DataSaver.start();
			MainActivity.myLogging.setText("LOGGING");
			MainActivity.myLogging.setTextColor(Color.GREEN);
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	private void StartStreamReader() {
		try {
			if (StreamReader == null) {
				StreamReader = new BTStreamReader();
			} else if (!StreamReader.isAlive() && StreamReader.getState() != Thread.State.NEW) {
				StreamReader = new BTStreamReader();
			}
			StreamReader.start();
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	private void StartDataParser() {
		try {
			if (Parser == null) {
				Parser = new BTDataParser(this);
			} else if (!Parser.isAlive() && Parser.getState() != Thread.State.NEW) {
				Parser = new BTDataParser(this);
			}
			Parser.start();
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	private void StartRandomGenerator() {
		try {
			if (Gen == null) {
				Gen = new RandomGenerator();
			} else if (!Gen.isAlive() && Gen.getState() != Thread.State.NEW) {
				Gen = new RandomGenerator();
			}
			Gen.start();
		} catch (Exception e) {
			showMessage(e.getMessage());
		}
	}

	private void StartUIUpdater(int delay, int uiUpdateInterval) {
		TimerTask UIUpdateTask = new TimerTask() {
			public void run() {
				MainActivityHandler.post(new UIUpdateRunnable());
			}
		};
		UIUpdateTimer = new Timer();
		UIUpdateTimer.schedule(UIUpdateTask, delay, uiUpdateInterval);
	}

	private void StopDataSaver() {
		if (DataSaver != null && DataSaver.getState() != Thread.State.TERMINATED) {
			DataSaver.cancel();
		}
	}

	private void StopRandomGenerator() {
		if (Gen != null && Gen.getState() != Thread.State.TERMINATED) {
			Gen.cancel();
		}
	}

	private void StopStreamReader() {
		if (StreamReader != null && StreamReader.getState() != Thread.State.TERMINATED) {
			StreamReader.cancel();
		}
	}

	/**************************************************/
	/**************** OTHER SHIT          *************/
	/**************************************************/

	public static Context getAppContext() {
		return MainActivity.context;
	}

	private static void UpdateDataFileInfo() {
		File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
		MediaScannerConnection.scanFile(MainActivity.getAppContext(), new String[]{f.getAbsolutePath()}, null, null);
		Global.DataFileLength = f.length();
		myDataFileName.setText(Global.DATA_FILE);
	}

	/**************************************************/
	/**************** BTDATAPARSER IMPLEMENTATION *****/
	/**************************************************/

	@Override
	public void onCycleViewPacket() {
		CycleView();
	}

	@Override
	public void onActivateLaunchModePacket() {
		ActivateLaunchMode();
	}
}
