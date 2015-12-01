package com.ben.drivenbluetooth;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.ben.drivenbluetooth.fragments.FourGraphsBars;
import com.ben.drivenbluetooth.fragments.LapHistoryFragment;
import com.ben.drivenbluetooth.fragments.RaceMapFragment;
import com.ben.drivenbluetooth.fragments.SettingsFragment;
import com.ben.drivenbluetooth.fragments.SixGraphsBars;
import com.ben.drivenbluetooth.threads.BTDataParser;
import com.ben.drivenbluetooth.threads.BTStreamReader;
import com.ben.drivenbluetooth.threads.DataToCsvFile;
import com.ben.drivenbluetooth.threads.RandomGenerator;
import com.ben.drivenbluetooth.threads.UDPSender;
import com.ben.drivenbluetooth.util.Accelerometer;
import com.ben.drivenbluetooth.util.BluetoothManager;
import com.ben.drivenbluetooth.util.CyclingArrayList;
import com.ben.drivenbluetooth.util.DrivenLocation;
import com.ben.drivenbluetooth.util.DrivenSettings;
import com.ben.drivenbluetooth.util.GraphData;
import com.ben.drivenbluetooth.util.NetworkMonitor;
import com.ben.drivenbluetooth.util.UIUpdateRunnable;
import com.ben.drivenbluetooth.util.UpdateFragment;

import java.io.File;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity
		extends 	Activity
		implements	BTDataParser.BTDataParserListener,
					BluetoothManager.BluetoothEvents {

	public static TextView myMode;

	public static TextView myDataFileSize;
	public static TextView myDataFileName;

	public static TextView myBTCarName;

	public static TextView myBTState;
	public static TextView myLogging;

	public static Chronometer LapTimer;
	public static TextView prevLapTime;
	public static TextView LapNumber;

	public static RandomGenerator mRandomGenerator = new RandomGenerator();
	public BTDataParser mBTDataParser = new BTDataParser(this); // can't be static because of (this)
	public static DataToCsvFile mDataToCSVFile = new DataToCsvFile();
	public static BTStreamReader mBTStreamReader; // initialize below
	public static UDPSender mUDPSender; // initialize below
	public static NetworkMonitor mNetworkMonitor = new NetworkMonitor();

	private static Timer UIUpdateTimer; // don't initialize because it should be done below

	public static final Handler MainActivityHandler = new Handler();

	public static DrivenLocation myDrivenLocation; // must be initialized below or else null object ref error

	public static Accelerometer myAccelerometer;

	private static Context context;
	public static final BluetoothManager myBluetoothManager = new BluetoothManager();

	private static final CyclingArrayList<UpdateFragment> FragmentList = new CyclingArrayList<>();
	public static UpdateFragment currentFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();

		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		myMode = (TextView) findViewById(R.id.txt_Mode);

		myDataFileName = (TextView) findViewById(R.id.txtDataFileName);
		myDataFileSize = (TextView) findViewById(R.id.txtDataFileSize);

		myBTCarName = (TextView) findViewById(R.id.txtBTCarName);

		myBTState = (TextView) findViewById(R.id.txtBTState);
		myLogging = (TextView) findViewById(R.id.txtLogging);

		LapTimer = (Chronometer) findViewById(R.id.LapTimer);
		prevLapTime = (TextView) findViewById(R.id.previousLapTime);
		LapNumber = (TextView) findViewById(R.id.lap);

		StartUIUpdater();

		DrivenSettings.InitializeSettings();

		/******************* ACCELEROMETER ****************/
		myAccelerometer = new Accelerometer((SensorManager) getSystemService(Context.SENSOR_SERVICE));

		myDrivenLocation = new DrivenLocation(); // must be initialized here or else null object ref error
		mUDPSender = new UDPSender();

		UpdateDataFileInfo();

		MainActivity.myLogging.setText("NO");
		MainActivity.myLogging.setTextColor(Color.RED);

		InitializeFragmentList();
		CycleView();
		StartDataParser();
        StartUDPSender();

		myBluetoothManager.setBluetoothEventsListener(this);

		GraphData.InitializeGraphDataSets();

		InitializeLongClickStart();
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

	@Override
	protected void onDestroy() {
		StopUIUpdater();
		ForceStop(myDataFileName);
		myMode			= null;
		myDataFileName	= null;
		myDataFileSize	= null;
		myBTCarName		= null;
		myBTState		= null;
		myLogging		= null;
		LapTimer		= null;
		prevLapTime		= null;
		LapNumber		= null;


		myAccelerometer = null;
		myDrivenLocation = null;
		myBluetoothManager.unregisterListeners();

		FragmentList.clear();

		super.onDestroy();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			this.getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	private void InitializeFragmentList() {
		FragmentList.add(new RaceMapFragment());
		FragmentList.add(new SixGraphsBars());
		FragmentList.add(new FourGraphsBars());
		FragmentList.add(new LapHistoryFragment());
	}

	private void InitializeLongClickStart() {
		// We can't do this in XML so must do it programatically
		ImageButton startButton = (ImageButton) findViewById(R.id.start);
		startButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				StartWithForcedLogging(v);
				return true;
			}
		});
	}

	public void CycleView() {
		try {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			currentFragment = FragmentList.cycle();
			fragmentTransaction.replace(R.id.CenterView, currentFragment);
			fragmentTransaction.commit();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public void CycleViewReverse() {
		try {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			Fragment fragment = FragmentList.reverseCycle();
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
	}

	/**************************************************/
	/**************** TOASTER          ****************/
	/**
	 * **********************************************
	 */

	public static void showMessage(String theMsg) {

		if (context != null) {
			Toast msg = Toast.makeText(context, theMsg, (Toast.LENGTH_SHORT));
			msg.show();
		}
	}

	public static void showMessage(String string, int length) {
		final Toast msg = Toast.makeText(context, string, Toast.LENGTH_LONG);
		msg.show();
	}

	public static void showError(Exception e) {
		showMessage(e.getMessage(), Toast.LENGTH_SHORT);
	}

	/**************************************************/
	/**************** BUTTON LISTENERS ****************/
	/**
	 * **********************************************
	 */

	public void OpenBT(View v) {
		if (!Objects.equals(Global.BTDeviceName, "")) { // fucking Java string comparators...
			try {
				myBluetoothManager.findBT();
				myBluetoothManager.openBT(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			showMessage("Error: Bluetooth device name not given in Settings. Please go to Settings and enter the device name");
		}
	}

	public void CloseBT(View v) {
		try {
			StopStreamReader();
			myBluetoothManager.closeBT();
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}

	public void Stop(View v) {
		try {
			StopRandomGenerator();
			StopDataLogger();

			MainActivity.myLogging.setText("NO");
			MainActivity.myLogging.setTextColor(Color.RED);

			UpdateDataFileInfo();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void StartWithForcedLogging(View v) {
		try {
			if (Global.Mode == Global.MODE.DEMO) {
				StartDataLogger();
				StartDemoMode();
			} else if (Global.Mode == Global.MODE.RACE) {
				StartRaceMode();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /** For testing purposes only. Stops all the threads immediately. Called when the user double-taps the data file name in the top-left corner of the app*/
	public void ForceStop(View v) {
		try {
			StopRandomGenerator();
			StopDataLogger();
			StopStreamReader();

			MainActivity.myLogging.setText("NO");
			MainActivity.myLogging.setTextColor(Color.RED);

			UpdateDataFileInfo();

			UIUpdateTimer.purge();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /** Called when the user taps the cogwheel in the app. Launches the settings fragment */
	public void LaunchSettings(View v) {
		SettingsFragment settingsFragment = new SettingsFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.graphview_overlay, settingsFragment)
				.addToBackStack(null)
				.commit();
	}

    /** Called when the user taps "cycle" in the app. Cycles the view between the fragments contained in FragmentList */
	public void Cycle(View v) {
		byte[] cyclepacket = new byte[5];
		cyclepacket[0] = Global.STARTBYTE;
		cyclepacket[1] = Global.CYCLE_VIEW_ID;
		cyclepacket[2] = 0;
		cyclepacket[3] = 0;
		cyclepacket[4] = Global.STOPBYTE;
		Global.BTStreamQueue.add(cyclepacket);
		BTDataParser.mHandler.sendEmptyMessage(0);
	}

    /** Called when the user taps "LM" in the app. Enables Launch Mode */
	public void LaunchMode(View v) {
		byte[] launchpacket = new byte[5];
		launchpacket[0] = Global.STARTBYTE;
		launchpacket[1] = Global.LAUNCH_MODE_ID;
		launchpacket[2] = 0;
		launchpacket[3] = 0;
		launchpacket[4] = Global.STOPBYTE;
		Global.BTStreamQueue.add(launchpacket);
		BTDataParser.mHandler.sendEmptyMessage(0);
	}

    /** For testing purposes. Simulates a race start by setting throttle to 100% */
	@Deprecated
	public void RaceStart(View v) {
		Global.InputThrottle = 100d;
	}

    /** For testing purposes. Simulates crossing the finish line */
	@Deprecated
	public void CrossFinishLine(View v) {
		myDrivenLocation.SimulateCrossStartFinishLine();
	}

    /** This function is called when the user taps "DEMO/RACE" in the top right corner of the app. Changes between race and demo mode */
	public static void QuickChangeMode(View v) {
		DrivenSettings.QuickChangeMode();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				CycleView();
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				CycleViewReverse();
				return true;

			default:
				return super.onKeyDown(keyCode, event);
		}
	}

	/* ======= */
	/* THREADS */
    /* ======= */

    /** Starts the stream reader and data logger threads */
	private void StartRaceMode() {
		StartStreamReader();
		StartDataLogger();
	}

    /** Starts the random generator */
	private void StartDemoMode() {
		StartRandomGenerator();
	}

    /** Starts the data logger thread (if not already running). Re-initializes the thread if needed */
	private void StartDataLogger() {
		try {
			if (mDataToCSVFile == null) {
				mDataToCSVFile = new DataToCsvFile();
				mDataToCSVFile.start();
			} else if (!mDataToCSVFile.isAlive()) {
				if (mDataToCSVFile.getState() != Thread.State.NEW) {
					mDataToCSVFile = new DataToCsvFile();
				}
				mDataToCSVFile.start();
				MainActivity.myLogging.setText("LOGGING");
				MainActivity.myLogging.setTextColor(Color.GREEN);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /** Starts the Bluetooth stream reader thread (if not already running). Re-initializes the thread if needed */
	private void StartStreamReader() {
		try {
			if (mBTStreamReader == null) {
				mBTStreamReader = new BTStreamReader();
				mBTStreamReader.start();
			} else if (!mBTStreamReader.isAlive()) {
				if (mBTStreamReader.getState() != Thread.State.NEW) {
					mBTStreamReader = new BTStreamReader();
				}
				mBTStreamReader.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /** Starts the Bluetooth parser thread (if not already running). Re-initializes the thread if needed */
	private void StartDataParser() {
		try {
			if (mBTDataParser == null) {
				mBTDataParser = new BTDataParser(this);
				mBTDataParser.start();
			} else if (!mBTDataParser.isAlive()) {
				if (mBTDataParser.getState() != Thread.State.NEW) {
					mBTDataParser = new BTDataParser(this);
				}
				mBTDataParser.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /** Starts the UDP sender thread (if not already running). Re-initializes the thread if needed */
    private void StartUDPSender() {
        try {
            if (mUDPSender == null) {
                mUDPSender = new UDPSender();
                mUDPSender.start();
            } else if (!mUDPSender.isAlive()) {
                if (mUDPSender.getState() != Thread.State.NEW) {
                    mUDPSender = new UDPSender();
                }
                mUDPSender.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Starts the random generator thread (if not already running). Re-initializes the thread if needed */
	private void StartRandomGenerator() {
		try {
			if (mRandomGenerator == null) {
				mRandomGenerator = new RandomGenerator();
				mRandomGenerator.start();
			} else if (!mRandomGenerator.isAlive()) {
				if (mRandomGenerator.getState() != Thread.State.NEW) {
					mRandomGenerator = new RandomGenerator();
				}
				mRandomGenerator.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /** Starts the UI updater thread (if not already running). Re-initializes the thread if needed */
	private void StartUIUpdater() {
		TimerTask UIUpdateTask = new TimerTask() {
			public void run() {
				MainActivityHandler.post(new UIUpdateRunnable());
			}
		};
		UIUpdateTimer = new Timer();
		UIUpdateTimer.schedule(UIUpdateTask, 0, Global.SLOW_UI_UPDATE_INTERVAL);
	}

    /** Stops the UI updater thread (if running) */
	private void StopUIUpdater() {
		UIUpdateTimer.cancel();
		UIUpdateTimer.purge();
	}

    /** Stops the data logger thread (if running) */
	private void StopDataLogger() {
		if (mDataToCSVFile != null && mDataToCSVFile.getState() != Thread.State.TERMINATED) {
			mDataToCSVFile.cancel();
		}
	}

    /** Stops the random generator thread (if running) */
	private void StopRandomGenerator() {
		if (mRandomGenerator != null && mRandomGenerator.getState() != Thread.State.TERMINATED) {
			mRandomGenerator.cancel();
		}
	}

    /** Stops the Bluetooth stream reader thread (if running) */
	private void StopStreamReader() {
		if (mBTStreamReader != null && mBTStreamReader.getState() != Thread.State.TERMINATED) {
			mBTStreamReader.cancel();
		}
	}

    /* ========== */
	/* OTHER SHIT */
    /* ========== */

    /** Returns the AppContext from a static context
     *
     * @return The application context
     */
	public static Context getAppContext() {
		return MainActivity.context;
	}

    /** Updates the TextView in the top-left corner of the app with the csv file name and size */
	private static void UpdateDataFileInfo() {
		File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
		MediaScannerConnection.scanFile(MainActivity.getAppContext(), new String[]{f.getAbsolutePath()}, null, null);
		Global.DataFileLength = f.length();
		myDataFileName.setText(Global.DATA_FILE);
	}

	/* =========================== */
	/* BTDATAPARSER IMPLEMENTATION */
    /* =========================== */

    /** This function is triggered by the Bluetooth data parser when it receives a {Cxx} packet.
     *  The driver will not be able to interact with the app as they will be wearing gloves and the phone will be in a waterproof case.
     *  A pushbutton in the cockpit is monitored by the Arduino for presses. If it detects the button, it will send a {Cxx} packet over Bluetooth
     */
	@Override
	public void onCycleViewPacket() {
		MainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                CycleView();
            }
        });
	}

    /** This function is triggered by the Bluetooth data parser when it receives a {Lxx} packet.
     *  The driver will not be able to interact with the app as they will be wearing gloves and the phone will be in a waterproof case.
     *  A pushbutton in the cockpit is monitored by the Arduino for presses. If it detects the button, it will send a {Lxx} packet over Bluetooth
     */
	@Override
	public void onActivateLaunchModePacket() {
		MainActivityHandler.post(new Runnable() {
			@Override
			public void run() {
				ActivateLaunchMode();
			}
		});
	}

    /* =============================== */
	/* BLUETOOTHMANAGER IMPLEMENTATION */
    /* =============================== */

    /** This function is triggered by BluetoothManager when a successful connection has been established with the Arduino. It receives a BluetoothSocket as the argument
     *
     * @param BTSocket  The BluetoothSocket which holds the connection to the Arduino
     */
	@Override
	public void onBluetoothConnected(final BluetoothSocket BTSocket) {
		Global.BTSocket = BTSocket;
		Global.BTState = Global.BTSTATE.CONNECTED;

		MainActivityHandler.post(new Runnable() {
			@Override
			public void run() {
				showMessage(Global.BTDeviceName + " successfully connected");
				StartRaceMode();
			}
		});
	}

    /** This function is triggered by BluetoothManager when an unsuccessful connection occurs */
	@Override
	public void onFailConnection() {
		MainActivityHandler.post(new Runnable() {
			@Override
			public void run() {
				showMessage("Could not connect to " + Global.BTDeviceName + ". Please try again");
				Global.BTState = Global.BTSTATE.DISCONNECTED;
			}
		});
	}

    /** This function is triggered by BluetoothManager when the Bluetooth is disabled on the users phone. This must be handled by MainActivity because here is the only place where another Intent can be launched     */
	@Override
	public void onBluetoothDisabled() {
		Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBluetooth, 0);
	}
}
