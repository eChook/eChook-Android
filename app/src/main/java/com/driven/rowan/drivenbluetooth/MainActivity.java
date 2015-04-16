package com.driven.rowan.drivenbluetooth;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    static TextView myLabel;
    static TextView myTextbox;
    static TextView myIncoming;
	static TextView myMode;
    static EditText mySpeed;
    static EditText myCurrent;
    static EditText myVoltage;

	static TextView mySpeedDataCount;
	static TextView myVoltsDataCount;
	static TextView myAmpsDataCount;

	private static Context context;

	static final BluetoothManager myBluetoothManager = new BluetoothManager();

	// Threads
	public static RandomGenerator Gen = new RandomGenerator();
	public static BTDataParser Parser = new BTDataParser();
	public static BTStreamReader StreamReader; // initialize below

	// This must be initialized in the main thread because reasons
	public static BluetoothDisconnectedRunnable BTReconnect = new BluetoothDisconnectedRunnable();

	// UI Update Timer
	private Timer UIUpdateTimer; // don't initialize because it should be done below

	// UI Update Handler
	public static Handler MainActivityHandler = new Handler();

	// UI Update TimerTask
	private TimerTask UIUpdateTask; // initialized later on

	private static final int RESULT_SETTINGS = 2;

	String TAG = "DBDebug";

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Button openBTButton = (Button) findViewById(R.id.open);
		Button startButton = (Button) findViewById(R.id.start);
		Button stopButton = (Button) findViewById(R.id.stop);
		Button closeBTButton = (Button) findViewById(R.id.close);
		Button saveButton = (Button) findViewById(R.id.save);
		myLabel = (TextView) findViewById(R.id.label);
		myTextbox = (TextView) findViewById(R.id.entry);
		myIncoming = (TextView) findViewById(R.id.incoming);
		mySpeed = (EditText) findViewById(R.id.speed);
		myCurrent = (EditText) findViewById(R.id.current);
		myVoltage = (EditText) findViewById(R.id.voltage);
		myMode = (TextView) findViewById(R.id.txt_Mode);

		mySpeedDataCount = (TextView) findViewById(R.id.SpeedDataCount);
		myVoltsDataCount = (TextView) findViewById(R.id.VoltsDataCount);
		myAmpsDataCount = (TextView) findViewById(R.id.AmpsDataCount);

		/************** INITIALIZE SETTINGS ***************/
		this.InitializeGlobalSettings();

		/**************** CONTEXT *************************/
		MainActivity.context = getApplicationContext();

		/**************************************************/
		/**************** BUTTON LISTENERS ****************/
		/**************************************************/

		// Open BT Button
		openBTButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					myBluetoothManager.findBT();
					myBluetoothManager.openBT();
				} catch (Exception e) {
					showMessage(e.getMessage().toString());
				}
			}
		});

		// Close BT Button
		closeBTButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					myBluetoothManager.closeBT();
				} catch (Exception e) {
					showMessage(e.getMessage().toString());
				}
			}
		});

		// Start Button
		startButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if (Global.Mode == Global.MODE.DEMO) {
						if (Gen.getState() != Thread.State.RUNNABLE) {
							if (Gen.getState() == Thread.State.TERMINATED) { Gen = new RandomGenerator(); }
							Gen.start();
						}

						if (Parser.getState() != Thread.State.RUNNABLE) {
							if (Parser.getState() == Thread.State.TERMINATED) { Parser = new BTDataParser(); }
							Parser.start();
						}

						// UI Updater
						UIUpdateTask = new TimerTask() {
							public void run() {MainActivityHandler.post(new UIUpdateRunnable());
							}
						};
						UIUpdateTimer = new Timer();
						UIUpdateTimer.schedule(UIUpdateTask, 250, 250);

						myLabel.setText("Now Logging - press 'Stop' to cancel");

					} else if (Global.Mode == Global.MODE.RACE && Global.BTSocket != null) {
						if (StreamReader == null) { StreamReader = new BTStreamReader();}
						if (StreamReader.getState() != Thread.State.RUNNABLE) {
							if (StreamReader.getState() == Thread.State.TERMINATED) { StreamReader = new BTStreamReader(); }
							StreamReader.start();
						}

						if (Parser.getState() != Thread.State.RUNNABLE) {
							if (Parser.getState() == Thread.State.TERMINATED) { Parser = new BTDataParser(); }
							Parser.start();
						}

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
		});

		// Stop Button
		stopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if (Gen.getState() == Thread.State.RUNNABLE) { Gen.cancel(); }
					if (StreamReader != null && StreamReader.getState() == Thread.State.RUNNABLE) { StreamReader.cancel(); }				if (Parser.getState() == Thread.State.RUNNABLE) { Parser.cancel(); }

					UIUpdateTimer.cancel();
					UIUpdateTimer.purge();
					myLabel.setText("Stopped logging");
				} catch (Exception e) {
					showMessage(e.getMessage().toString());
				}
			}
		});

		// Save Button
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					MainActivityHandler.post(new SaveDataToFileRunnable());
				} catch (Exception e) {
					showMessage(e.getMessage().toString());
				}
			}
		});
	}


    public void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(),
                theMsg, (Toast.LENGTH_LONG));
        msg.show();
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

			int units = Integer.valueOf(prefs.getString("prefSpeedUnits", ""));
			Global.Unit = Global.UNIT.values()[units];
		} catch (Exception e) {
			showMessage("Could not retrieve settings");
		}
	}

	public static Context getAppContext() {
		return MainActivity.context;
	}
}
