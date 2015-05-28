package com.driven.rowan.drivenbluetooth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    /************** UI ELEMENTS ***************/
    public static TextView myLabel;
	public static TextView myMode;
	public static TextView SMSZone;

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

	static Button openBTButton;
	static Button startButton;
	static Button stopButton;
	static Button closeBTButton;
	static Button saveButton;

	/************** THREADS ***************/
	public static RandomGenerator Gen 			= new RandomGenerator();
	public static BTDataParser Parser 			= new BTDataParser();
    public static DataToCsvFile DataSaver 		= new DataToCsvFile();
	public static BTStreamReader StreamReader; // initialize below

    /************** UI UPDATER ***************/
	private Timer UIUpdateTimer; // don't initialize because it should be done below
    private TimerTask UIUpdateTask; // initialized later on

    /************** UI THREAD HANDLER ***************/
	public static Handler MainActivityHandler = new Handler();

    /************** OTHER ***************/
	private static final int RESULT_SETTINGS = 2;
    private static Context context;
    static final BluetoothManager myBluetoothManager = new BluetoothManager();
    public static BluetoothDisconnectedRunnable BTReconnect = new BluetoothDisconnectedRunnable(); // This must be initialized in the main thread because reasons
	private PendingIntent pendingIntent;

	String TAG = "DrivenBluetooth";

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		/* BUTTONS */
		Button openBTButton 	= (Button) findViewById(R.id.open);
		Button startButton 		= (Button) findViewById(R.id.start);
		Button stopButton 		= (Button) findViewById(R.id.stop);
		Button closeBTButton 	= (Button) findViewById(R.id.close);

		/* LABELS */
		myLabel 				= (TextView) findViewById(R.id.label);
		myMode 					= (TextView) findViewById(R.id.txt_Mode);

		/* DATA FIELDS */
		Throttle 				= (EditText) findViewById(R.id.throttle);
		Current 				= (EditText) findViewById(R.id.current);
		Voltage 				= (EditText) findViewById(R.id.voltage);
		Temp1 					= (EditText) findViewById(R.id.temp1);
		RPM 					= (EditText) findViewById(R.id.rpm);
		Speed 					= (EditText) findViewById(R.id.speed);

		RaceStartTime			= (EditText) findViewById(R.id.raceStartTime);
		SMSZone					= (TextView) findViewById(R.id.smsBox);

		/* FILL BARS */
		ThrottleBar				= (DataBar) findViewById(R.id.ThrottleBar);
		CurrentBar				= (DataBar) findViewById(R.id.CurrentBar);
		VoltageBar				= (DataBar) findViewById(R.id.VoltageBar);
		T1Bar					= (DataBar) findViewById(R.id.T1Bar);
		RPMBar					= (DataBar) findViewById(R.id.RPMBar);
		SpeedBar				= (DataBar) findViewById(R.id.SpeedBar);


		/************** INITIALIZE SETTINGS ***************/
		this.InitializeGlobalSettings();

		/**************** CONTEXT *************************/
		MainActivity.context = getApplicationContext();

		/**************** ALARM MANAGER *******************/
		Global.AlarmManager = (AlarmManager) MainActivity.getAppContext().getSystemService(Context.ALARM_SERVICE);

		/**************************************************/
		/**************** BUTTON LISTENERS ****************/
		/**************************************************/

		// Open BT Button
		openBTButton.setOnClickListener(new OpenBT());

		// Close BT Button
		closeBTButton.setOnClickListener(new CloseBT());

		// Start Button
		startButton.setOnClickListener(new StartAllThreads());

		// Stop Button
		stopButton.setOnClickListener(new CancelAllThreads());

		/**************** TIMEPICKER ***********************/
		RaceStartTime.setOnClickListener(new SetTimeDialog(RaceStartTime));
	}

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
        if (id == R.id.action_settings) {
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

	public static Context getAppContext() {
		return MainActivity.context;
	}

	/**************************************************/
	/**************** BUTTON LISTENERS ****************/
	/**************************************************/
	public class OpenBT implements View.OnClickListener {
		public void onClick(View v) {
			try {
				myBluetoothManager.findBT();
				myBluetoothManager.openBT();
			} catch (Exception e) {
				showMessage(e.getMessage().toString());
			}
		}
	}

	public class CloseBT implements View.OnClickListener {
		public void onClick(View v) {
			try {
				myBluetoothManager.closeBT();
			} catch (Exception e) {
				showMessage(e.getMessage().toString());
			}
		}
	}

	public class CancelAllThreads implements View.OnClickListener {
		public void onClick(View v) {
			try {
				if (Gen != null && Gen.getState() != Thread.State.TERMINATED) { Gen.cancel(); }
				if (StreamReader != null && StreamReader.getState() != Thread.State.TERMINATED) { StreamReader.cancel(); }
				if (Parser != null && Parser.getState() != Thread.State.TERMINATED) { Parser.cancel(); }
				if (DataSaver != null && DataSaver.getState() != Thread.State.TERMINATED) { DataSaver.cancel(); }

				UIUpdateTimer.cancel();
				UIUpdateTimer.purge();
				myLabel.setText("Stopped logging");
			} catch (Exception e) {
				showMessage(e.getMessage().toString());
			}
		}

	}

	public class StartAllThreads implements View.OnClickListener {
		public void onClick(View v) {
			try {
				if (Global.Mode == Global.MODE.DEMO) {
					if (Gen == null) { Gen = new RandomGenerator(); }
					if (Gen.getState() != Thread.State.NEW) { Gen = new RandomGenerator(); }
					Gen.start();

					if (Parser.getState() != Thread.State.NEW) { Parser = new BTDataParser(); }
					Parser.start();

					if (DataSaver.getState() != Thread.State.NEW) { DataSaver = new DataToCsvFile(); }
					DataSaver.start();

					// UI Updater
					UIUpdateTask = new TimerTask() {
						public void run() {MainActivityHandler.post(new UIUpdateRunnable());
						}
					};
					UIUpdateTimer = new Timer();
					UIUpdateTimer.schedule(UIUpdateTask, 250, 250);

					myLabel.setText("Now Logging - press 'Stop' to cancel");

				} else if (Global.Mode == Global.MODE.RACE && Global.BTSocket != null) {
					if (StreamReader == null) { StreamReader = new BTStreamReader(); }
					if (StreamReader.getState() != Thread.State.NEW) { StreamReader = new BTStreamReader(); }
					StreamReader.start();

					if (Parser.getState() != Thread.State.NEW) { Parser = new BTDataParser(); }
					Parser.start();

					if (DataSaver.getState() != Thread.State.NEW) { DataSaver = new DataToCsvFile(); }
					DataSaver.start();

					// UI Updater
					UIUpdateTask = new TimerTask() {
						public void run() {	MainActivityHandler.post(new UIUpdateRunnable()); }
					};
					UIUpdateTimer = new Timer();
					UIUpdateTimer.schedule(UIUpdateTask, 250, 250);

					myLabel.setText("Now Logging - press 'Stop' to cancel");
				}
			} catch (Exception e) {
				showMessage(e.getMessage().toString());
			}
		}
	}

	public class SetTimeDialog implements View.OnClickListener {
		EditText editText;

		public SetTimeDialog(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Calendar mcurrentTime = Calendar.getInstance();
			int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
			int minute = mcurrentTime.get(Calendar.MINUTE);
			TimePickerDialog mTimePicker;
			mTimePicker = new TimePickerDialog(MainActivity.this, t, hour, minute, true);//Yes 24 hour time
			mTimePicker.setTitle("Select Time");
			mTimePicker.show();
		}

		private TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// %02d sets the string length to 2 and pads with zeros if necessary
				editText.setText( String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
				Calendar mcurrentTime = Calendar.getInstance();
				int year = mcurrentTime.get(Calendar.YEAR);
				int month = mcurrentTime.get(Calendar.MONTH);
				int day = mcurrentTime.get(Calendar.DAY_OF_MONTH);
				// Race countdown timer starts 10 seconds before so minute - 1, 50 seconds
				Global.RaceStartTime = new GregorianCalendar(year, month, day, hourOfDay, minute - 1, 50);
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
}
