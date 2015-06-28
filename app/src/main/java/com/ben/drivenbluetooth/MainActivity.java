package com.ben.drivenbluetooth;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.ben.drivenbluetooth.fragments.FourGraphsBars;
import com.ben.drivenbluetooth.fragments.GraphViewFragment;
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

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity
		extends 	Activity
		implements 	GraphViewFragment.OnFragmentInteractionListener,
					SixGraphsBars.OnFragmentInteractionListener,
					FourGraphsBars.OnFragmentInteractionListener,
					MainMapFragment.OnFragmentInteractionListener
{

	/************* UI ELEMENTS ***************/
	public static TextView myLabel;
	public static TextView myMode;

	public static TextView myDataFileSize;
	public static TextView myDataFileName;

	public static TextView myBTState;
	public static TextView myLogging;

	public static Button openBTButton;
	public static Button startButton;
	public static Button stopButton;
	public static Button closeBTButton;
	public static Button settingsButton;

	private enum UISTATE {SIX_GRAPHS_BARS, FOUR_GRAPHS_BARS}
	private UISTATE UIState = UISTATE.SIX_GRAPHS_BARS;

	public static RandomGenerator Gen = new RandomGenerator();
	public static BTDataParser Parser = new BTDataParser();
	public static DataToCsvFile DataSaver = new DataToCsvFile();
	public static BTStreamReader StreamReader; // initialize below

	private static Timer UIUpdateTimer; // don't initialize because it should be done below
	private static TimerTask UIUpdateTask; // initialized later on

	public static final Handler MainActivityHandler = new Handler();

	public static DrivenLocation myDrivenLocation;

	public static Accelerometer myAccelerometer;

	private static final int RESULT_SETTINGS = 2;
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

		/************** INITIALIZE UI ELEMENTS ************/
		InitializeButtons();
		//InitializeDataFields();
		//InitializeDataBars();
		//InitializeGraphs();

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
		myAccelerometer.update();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myAccelerometer.stopAccelerometerData();
	}

	private void InitializeFragmentList() {
		FragmentList.add(new MainMapFragment());
		FragmentList.add(new SixGraphsBars());
		FragmentList.add(new FourGraphsBars());
	}

	private void InitializeButtons() {
		openBTButton 	= (Button) findViewById(R.id.open);
		startButton 	= (Button) findViewById(R.id.start);
		stopButton 		= (Button) findViewById(R.id.stop);
		closeBTButton	= (Button) findViewById(R.id.close);
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

	public void Cycle(View v) {
		CycleView();
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
				Parser = new BTDataParser();
			} else if (!Parser.isAlive() && Parser.getState() != Thread.State.NEW) {
				Parser = new BTDataParser();
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
		UIUpdateTask = new TimerTask() {
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

	private void StopDataParser() {
		if (Parser != null && Parser.getState() != Thread.State.TERMINATED) {
			Parser.cancel();
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
