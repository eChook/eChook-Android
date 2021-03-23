package com.ben.drivenbluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.ben.drivenbluetooth.events.ArduinoEvent;
import com.ben.drivenbluetooth.events.NewLapEvent;
import com.ben.drivenbluetooth.events.PreferenceEvent;
import com.ben.drivenbluetooth.events.UpdateUIEvent;
import com.ben.drivenbluetooth.fragments.AllDataFragment;
import com.ben.drivenbluetooth.fragments.LapHistoryFragment;
import com.ben.drivenbluetooth.fragments.RaceMapFragment;
import com.ben.drivenbluetooth.fragments.SettingsFragment;
import com.ben.drivenbluetooth.fragments.SimpleDataFragment;
import com.ben.drivenbluetooth.threads.BTDataParser;
import com.ben.drivenbluetooth.threads.BTStreamReader;
import com.ben.drivenbluetooth.threads.DataToCsvFile;
import com.ben.drivenbluetooth.threads.RandomGenerator;
import com.ben.drivenbluetooth.threads.TelemetrySender;
import com.ben.drivenbluetooth.util.Accelerometer;
import com.ben.drivenbluetooth.util.BluetoothManager;
import com.ben.drivenbluetooth.util.CyclingArrayList;
import com.ben.drivenbluetooth.util.DrivenSettings;
import com.ben.drivenbluetooth.util.LapData;
import com.ben.drivenbluetooth.util.LocationMonitor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;


public class MainActivity
        extends AppCompatActivity
        implements BluetoothManager.BluetoothEvents {

    public static final Handler MainActivityHandler = new Handler();
    public static final BluetoothManager myBluetoothManager = new BluetoothManager();
    private static final CyclingArrayList<Fragment> FragmentList = new CyclingArrayList<>();
    public static TelemetrySender mTelemetrySender;     // initialize below
    public static Accelerometer myAccelerometer;
    private static RandomGenerator mRandomGenerator = new RandomGenerator();
    private static BTDataParser mBTDataParser = new BTDataParser();     // can't be static because of (this)
    private static DataToCsvFile mDataToCSVFile = new DataToCsvFile();
    private static BTStreamReader mBTStreamReader;     // initialize below
    public TextView myMode;
    public TextView myGear;
    public ImageView myShiftIndicator;
    public LocationMonitor myLocationMonitor;     // must be initialized below or else null object ref error
    private TextView myDataFileSize;
    private TextView myDataFileName;
    private TextView myBTCarName;
    private ImageView myBTState;
    private ImageView myLogging;
    private FloatingActionButton myBluetoothButton;
    private FloatingActionButton myLoggingToggleButton;
    private Chronometer LapTimer;
    private TextView prevLapTime;
    private TextView LapNumber;
    private Context context;
    private View SnackbarPosition;
    private NewLapEvent e;

    /* ========= */
    /* LIFECYCLE */
    /* ========= */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

//        GraphData.InitializeGraphDataSets(context);

        setContentView(R.layout.activity_main_v2); // sets the main screen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        myMode = findViewById(R.id.txt_Mode);

        myDataFileName = findViewById(R.id.txtDataFileName);
        myDataFileSize = findViewById(R.id.txtDataFileSize);

        myBTCarName = findViewById(R.id.txtBTCarName);

        myBTState = findViewById(R.id.btStatusSymbol);
        myLogging = findViewById(R.id.logStatusSymbol);

        myBluetoothButton = findViewById(R.id.open);
        myLoggingToggleButton = findViewById(R.id.start);


        LapTimer = findViewById(R.id.LapTimer);
        prevLapTime = findViewById(R.id.previousLapTime);
        LapNumber = findViewById(R.id.lap);

        myGear = findViewById(R.id.txtGear);
        myShiftIndicator = findViewById(R.id.imgShiftIndicator);

        SnackbarPosition = findViewById(R.id.snackbarPosition);

        UpdateLoggingStatus(false);

        DrivenSettings.InitializeSettings(context);

        myLocationMonitor = new LocationMonitor(LapTimer, prevLapTime, context);


        InitializeLongClickStart();
        InitializeLongClickMode();
        InitializeLongClickLap();

        InitializeFragmentList();
        CycleView();

        StartDataParser();

        RequestAllPermissions();

        UpdateBTStatus();
        UpdateBTCarName();
        UpdateGear(0);

        //Finally - attempt to connect to Bluetooth device
        OpenBTSilent();
    }


    @Override
    protected void onStart() {
        super.onStart();
        myLocationMonitor.connect();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            myLocationMonitor.disconnect();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        //StopUIUpdater();
        ForceStop();
        myMode = null;
        myDataFileName = null;
        myDataFileSize = null;
        myBTCarName = null;
        myBTState = null;
        myLogging = null;
        LapTimer = null;
        prevLapTime = null;
        LapNumber = null;


        myAccelerometer = null;
        myLocationMonitor = null;
        myBluetoothManager.unregisterListeners();

        FragmentList.clear();

        super.onDestroy();
        EventBus.getDefault().unregister(this);
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

            //will need to restart/reconnect to bluetooth, and relevant threads
            if (Global.dweetEnabled || Global.eChookLiveEnabled) {
                StartUDPSender();
            }
            mTelemetrySender.restart();

//            myBluetoothManager.reconnectBT(); //TODO -understand if needed


        } else //window has lost focus - lets stop tracking/logging/uploading
        {
            if (Global.BTState != Global.BTSTATE.DISCONNECTED) {
                try {
                    StopStreamReader();
                    myBluetoothManager.closeBT();
                    mDataToCSVFile.cancel();
                    mTelemetrySender.Disable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            mDataToCSVFile.cancel();
//            mTelemetrySender.Disable();

        }

    }

    private void RequestAllPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean permissionRequestRequired = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionRequestRequired = true;
            }
        }

        if (permissionRequestRequired) {
            ActivityCompat.requestPermissions(this, permissions, Global.PERMISSIONS_REQUEST);
        } else {
            InitializeAllTheThings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Global.PERMISSIONS_REQUEST) {
            boolean allPermissionsGranted = true;

            // check grantResult
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            // if a permission was not granted, ask user to do it again
            // TODO implement recursive permissions loop with dialog box

            if (allPermissionsGranted) {
                // thanks bro, initialize all the things
                InitializeAllTheThings();
            } else {
                // douche. now you can't play.
                System.exit(0);
            }
        } else {
            showMessage("Weird permissions requestCode received: " + requestCode);
        }
    }

    private void InitializeAllTheThings() {
        StartUDPSender();

        myAccelerometer = new Accelerometer((SensorManager) getSystemService(Context.SENSOR_SERVICE));

        UpdateDataFileInfo();

        myBluetoothManager.setBluetoothEventsListener(this);
    }

    private void InitializeFragmentList() {
        RaceMapFragment rmf = new RaceMapFragment();
        rmf.initialize(myLocationMonitor);
        FragmentList.add(new SimpleDataFragment());
        FragmentList.add(new AllDataFragment());
//        FragmentList.add(new FourGraphsBars());
        FragmentList.add(rmf);
        FragmentList.add(new LapHistoryFragment());
    }

    private void InitializeLongClickStart() {
        // We can't do this in XML so must do it programatically
        FloatingActionButton startButton = findViewById(R.id.start);
        startButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                StartWithForcedLogging();
                return true;
            }
        });
    }

    private void InitializeLongClickMode() {
        // We can't do this in XML so must do it programatically
        TextView modeText = findViewById(R.id.txt_Mode);
        modeText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                QuickChangeMode(v);
                return true;
            }
        });
    }

    private void InitializeLongClickLap() {
        // We can't do this in XML so must do it programatically
        TextView modeText = findViewById(R.id.lap);
        modeText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                lapIncrement();
                return true;
            }
        });
    }

    //DEBUG PURPOSES
    void lapIncrement(){
        // add new lap
        Global.LapDataList.add(new LapData());
        Global.Lap += 1;

        // Update the lap text
        EventBus.getDefault().post(new NewLapEvent());
    }

    /* ======= */
    /* TOASTER */
    /* ======= */

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void showError(SnackbarEvent e) {
//        showMessage(e.message);
//    }

    public void showMessage(String string) {
        showSnackbar(string);
    }

    private void showSnackbar(String msg) {
        try {
            Snackbar sb = Snackbar.make(SnackbarPosition, msg, com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG);
            sb.show();
        } catch (Exception ignored) {
        }
    }

    @Deprecated
    public void showLapSummary(String message, int duration) {
        final AlertDialog lapSummary = new AlertDialog.Builder(getAppContext())
                .setMessage(message)
                .setTitle("Lap summary")
                .create();

        lapSummary.show();
        MainActivityHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lapSummary.hide();
                lapSummary.dismiss();
            }
        }, duration);

    }

    /* ================ */
    /* BUTTON LISTENERS */
    /* ================ */

    public void ToggleBT(View v) {
        if (Global.BTState != Global.BTSTATE.DISCONNECTED) { //Disconnect BT
            try {
                CloseBT();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { //Connect BT
            OpenBT();
        }
    }

    public void OpenBT() {
        if (!Objects.equals(Global.BTDeviceName, "")) { // annoying Java string comparators...
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

    public void OpenBTSilent() {
        if (!Objects.equals(Global.BTDeviceName, "")) { // annoying Java string comparators...
            try {
                myBluetoothManager.findBT();
                myBluetoothManager.openBT(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void CloseBT() {
        try {
            StopStreamReader();
            myBluetoothManager.closeBT();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleLogging(View v) {
        if (Global.isLogging) {
            Stop();
        } else {
            Start();
        }
    }

    public void Start() {
        try {
            if (Global.Mode == Global.MODE.TEST) {
                StartDemoMode();
            } else if (Global.Mode == Global.MODE.RACE) {
                StartRaceMode(); //TODO Need to check if a device is connected before trying to start a stream.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Stop() {
        try {
            StopRandomGenerator();
            StopDataLogger();

            UpdateDataFileInfo();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void StartWithForcedLogging() {
        try {
            if (Global.Mode == Global.MODE.TEST) {
                StartDataLogger();
                StartDemoMode();
            } else if (Global.Mode == Global.MODE.RACE) {
                StartRaceMode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * For testing purposes only. Stops all the threads immediately. Called when the user double-taps the data file name in the top-left corner of the app
     */
    private void ForceStop() {
        try {
            StopRandomGenerator();
        } catch (Exception ignored) {
        }
        try {
            StopDataLogger();
        } catch (Exception ignored) {
        }
        try {
            StopStreamReader();
        } catch (Exception ignored) {
        }
        UpdateDataFileInfo();
    }

    /**
     * Called when the user taps the cogwheel in the app. Launches the settings fragment
     */
    public void LaunchSettings(View v) {
        SettingsFragment settingsFragment = new SettingsFragment();

        Log.d("MainActivity", "Launching Settings");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.overlay, settingsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Called when the user taps "cycle" in the app. Cycles the view between the fragments contained in FragmentList
     */
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

    /**
     * Called when the user taps "LM" in the app. Enables Launch Mode
     */
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

    /**
     * For testing purposes. Simulates a race start by setting throttle to 100%
     */
    @Deprecated
    public void RaceStart(View v) {
        Global.InputThrottle = 100d;
    }

    /**
     * For testing purposes. Simulates crossing the finish line
     */
    public void CrossFinishLine(View v) {
        myLocationMonitor.SimulateCrossStartFinishLine();
    }

    /**
     * This function is called when the user taps "DEMO/RACE" in the top right corner of the app. Changes between race and demo mode
     */
    public void QuickChangeMode(View v) {
        DrivenSettings.QuickChangeMode(context);
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

    /**
     * Starts the stream reader and data logger threads
     */
    private void StartRaceMode() {
        StartStreamReader();
        StartDataLogger();
    }

    /**
     * Starts the random generator
     */
    private void StartDemoMode() {
        StartRandomGenerator();
        StartDataLogger();
    }

    /**
     * Starts the data logger thread (if not already running). Re-initializes the thread if needed
     */
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
                Global.isLogging = true;
                UpdateLoggingStatus(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the Bluetooth stream reader thread (if not already running). Re-initializes the thread if needed
     */
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

    /**
     * Starts the Bluetooth parser thread (if not already running). Re-initializes the thread if needed
     */
    private void StartDataParser() {
        try {
            if (mBTDataParser == null) {
                mBTDataParser = new BTDataParser();
                mBTDataParser.start();
            } else if (!mBTDataParser.isAlive()) {
                if (mBTDataParser.getState() != Thread.State.NEW) {
                    mBTDataParser = new BTDataParser();
                }
                mBTDataParser.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the UDP sender thread (if not already running). Re-initializes the thread if needed
     */
    private void StartUDPSender() {
        try {
            if (mTelemetrySender == null) {
                mTelemetrySender = new TelemetrySender();
                mTelemetrySender.start();
            } else if (!mTelemetrySender.isAlive()) {
                if (mTelemetrySender.getState() != Thread.State.NEW) {
                    mTelemetrySender = new TelemetrySender();
                }
                mTelemetrySender.start();

            }
            Log.d("UDP", "UDP Sender Started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the random generator thread (if not already running). Re-initializes the thread if needed
     */
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

    /**
     * Stops the data logger thread (if running)
     */
    private void StopDataLogger() {
        assert mDataToCSVFile != null;
        if (mDataToCSVFile.getState() != Thread.State.TERMINATED || mDataToCSVFile.getState() != Thread.State.NEW) {
            mDataToCSVFile.cancel();
        }
        Global.isLogging = false;
        UpdateLoggingStatus(false);
    }

    /**
     * Stops the random generator thread (if running)
     */
    private void StopRandomGenerator() {
        if (mRandomGenerator != null && mRandomGenerator.getState() != Thread.State.TERMINATED) {
            mRandomGenerator.cancel();
        }
    }

    /**
     * Stops the Bluetooth stream reader thread (if running)
     */
    private void StopStreamReader() {
        if (mBTStreamReader != null && mBTStreamReader.getState() != Thread.State.TERMINATED) {
            mBTStreamReader.cancel();
        }
    }

    /* =========== */
    /* OTHER STUFF */
    /* =========== */

    private void CycleView() {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.CenterView, FragmentList.cycle());
            fragmentTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void CycleViewReverse() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.CenterView, FragmentList.reverseCycle());
        fragmentTransaction.commit();
    }

    private void ActivateLaunchMode() {
        // reset some key variables
        // lap counters
        Global.Lap = 0;

        // Amp hours
        Global.AmpHours = 0d;

        // Lap data if any exists
        Global.LapDataList.clear();

        LapTimer.stop();
        LapTimer.setBase(SystemClock.elapsedRealtime());
        myLocationMonitor.ActivateLaunchMode();
    }

    /**
     * Returns the AppContext from a static context
     *
     * @return The application context
     */
    public Context getAppContext() {
        return context;
    }

    /**
     * Updates the TextView in the top-left corner of the app with the csv file name and size
     */
    @SuppressLint("SetTextI18n")
    private void UpdateDataFileInfo() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
        MediaScannerConnection.scanFile(context, new String[]{f.getAbsolutePath()}, null, null);
        Global.DataFileLength = f.length();
        if (Global.DataFileLength < 1024) {
            myDataFileSize.setText(Global.DataFileLength + " B");
        } else if (Global.DataFileLength < 1048576) {
            myDataFileSize.setText(String.format(Locale.ENGLISH, "%.2f", (float) Global.DataFileLength / 1024.0) + " KB");
        } else {
            myDataFileSize.setText(String.format(Locale.ENGLISH, "%.2f", (float) Global.DataFileLength / 1048576) + " MB");
        }
        myDataFileName.setText(Global.DATA_FILE);
    }

    /**
     * Updates the TextView in the top-right corner of the app with the current Bluetooth connection status
     */
    private void UpdateBTStatus() {
        MainActivityHandler.post(new Runnable() {
            public void run() {
                switch (Global.BTState) {
                    case DISCONNECTED:
                        myBTState.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_cancel_black_24dp, null));
                        myBTState.setColorFilter(ContextCompat.getColor(context, R.color.negative));
                        //Button Updates
                        myBluetoothButton.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_bluetooth_disabled_black_24dp, null));
                        myBluetoothButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                        break;
                    case CONNECTING:
                        myBTState.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_group_work_black_24dp, null));
                        myBTState.setColorFilter(ContextCompat.getColor(context, R.color.neutral));
                        //Button Updates
                        myBluetoothButton.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_bluetooth_disabled_black_24dp, null));
                        myBluetoothButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.amber)));
                        break;
                    case CONNECTED:
                        myBTState.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_check_circle_black_24dp, null));
                        myBTState.setColorFilter(ContextCompat.getColor(context, R.color.positive));
                        //Button Updates
                        myBluetoothButton.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_bluetooth_connected_black_24dp, null));
                        myBluetoothButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                        break;
                    case RECONNECTING:
                        myBTState.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_warning_black_24dp, null));
                        myBTState.setColorFilter(ContextCompat.getColor(context, R.color.neutral));
                        //Button Updates
                        myBluetoothButton.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_bluetooth_connected_black_24dp, null));
                        myBluetoothButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.amber)));
                        break;
                }
            }
        });
    }

    private void UpdateLoggingStatus(boolean logging) {
        if (logging) {
            myLogging.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_check_circle_black_24dp, null));
            myLogging.setColorFilter(ContextCompat.getColor(context, R.color.positive));
            myLoggingToggleButton.setBackgroundTintList(ColorStateList.valueOf((getResources().getColor(R.color.pause))));
            myLoggingToggleButton.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_stop_black_24dp, null));
        } else {
            myLogging.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_cancel_black_24dp, null));
            myLogging.setColorFilter(ContextCompat.getColor(context, R.color.negative));
            myLoggingToggleButton.setBackgroundTintList(ColorStateList.valueOf((getResources().getColor(R.color.play))));
            myLoggingToggleButton.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_play_arrow_black_24dp, null));
        }
    }

    /**
     * Updates the TextView at the bottom of the UI showing the lap number
     */
    @SuppressLint("SetTextI18n")
    public void UpdateLap() {
        LapNumber.setText("L" + Global.Lap);
    }

    /**
     * Updates the TextView at the top of the UI showing the BT device name
     */
    public void UpdateBTCarName() {
        MainActivityHandler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                myBTCarName.setText(Global.BTDeviceName + " :: " + Global.CarName);
            }
        });
    }


    public void UpdateGear(final int shift) {
        MainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Global.Gear <= 0) {
                    myGear.setText("?");
                    myShiftIndicator.setVisibility(View.INVISIBLE);
                } else {
                    myGear.setText(String.valueOf(Global.Gear));
                    if (shift == 1) {
                        // shift up indicator
                        myShiftIndicator.setVisibility(View.VISIBLE);
                        myShiftIndicator.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.shift_up, null));
                    } else if (shift == -1) {
                        // shift down indicator
                        myShiftIndicator.setVisibility(View.VISIBLE);
                        //MainActivity.myShiftIndicator.setImageDrawable(ResourcesCompat.getDrawable(getAppContext().getResources(), R.drawable.ic_down_circular_xxl, null));
                    } else {
                        // hide shift indicator
                        myShiftIndicator.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    /**
     * This function is triggered by the Bluetooth data parser when it receives a {Cxx} packet.
     * The driver will not be able to interact with the app as they will be wearing gloves and the phone will be in a waterproof case.
     * A pushbutton in the cockpit is monitored by the Arduino for presses. If it detects the button, it will send a {Cxx} packet over Bluetooth
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArduinoEvent(ArduinoEvent e) {
        switch (e.eventType) {
            case CycleView:
                CycleView();
                break;
            case LaunchMode:
                ActivateLaunchMode();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPreferenceEvent(PreferenceEvent e) {
        switch (e.eventType) {

            case ModeChange:
                myMode.setText(Global.Mode.toString());
                break;
            case BTDeviceNameChange:
                UpdateBTStatus();
                myBTCarName.setText(Global.BTDeviceName + " :: " + Global.CarName);
                break;
            case CarNameChange:
                UpdateBTCarName();
                myBTCarName.setText(Global.BTDeviceName + " :: " + Global.CarName);
                break;
            case UDPChange:
                //TODO - restart telemetry senders here if any enabled.
                break;
            case LocationChange:
                break;
            case DataFileSettingChange:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationEvent(NewLapEvent e) {
        this.e = e;
        UpdateLap();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateUIEvent(UpdateUIEvent e) {
        if (e.eventType == UpdateUIEvent.EventType.DataFile) {
            UpdateDataFileInfo();
        }
    }


    /* =============================== */
    /* BLUETOOTH MANAGER IMPLEMENTATION */
    /* =============================== */

    /**
     * This function is triggered by BluetoothManager when a successful connection has been established with the Arduino. It receives a BluetoothSocket as the argument
     *
     * @param BTSocket The BluetoothSocket which holds the connection to the Arduino
     */
    @Override
    public void onBluetoothConnected(final BluetoothSocket BTSocket) {
        Global.BTSocket = BTSocket;
        Global.BTState = Global.BTSTATE.CONNECTED;
        UpdateBTStatus();

        MainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                showMessage(Global.BTDeviceName + " successfully connected");
                StartRaceMode();
            }
        });
    }

    @Override
    public void onBluetoothConnecting() {
        MainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                showMessage("Connecting to " + Global.BTDeviceName + "...");
                Global.BTState = Global.BTSTATE.CONNECTING;
                UpdateBTStatus();
            }
        });
    }

    /**
     * This function is triggered by BluetoothManager when an unsuccessful connection occurs
     */
    @Override
    public void onFailConnection() {
        MainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                showMessage("Could not connect to BT device " + Global.BTDeviceName);
                Global.BTState = Global.BTSTATE.DISCONNECTED;
                UpdateBTStatus();
            }
        });
    }

    @Override
    public void onBluetoothDisconnected() {
        MainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                showMessage(Global.BTDeviceName + " disconnected");
                Global.BTState = Global.BTSTATE.DISCONNECTED;
                UpdateBTStatus();
                StopDataLogger();
                UpdateDataFileInfo();
            }
        });
    }

    /**
     * This function is triggered by BluetoothManager when the Bluetooth is disabled on the users phone. This must be handled by MainActivity because here is the only place where another Intent can be launched
     */
    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, 0);
    }

    /**
     * Called when BluetoothManager is in a reconnecting loop
     */
    @Override
    public void onBluetoothReconnecting() {
        Global.BTState = Global.BTSTATE.RECONNECTING;
        UpdateBTStatus();
    }
}
