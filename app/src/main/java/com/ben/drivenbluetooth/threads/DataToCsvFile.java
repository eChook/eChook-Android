package com.ben.drivenbluetooth.threads;

import android.os.Environment;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.ben.drivenbluetooth.events.UpdateUIEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataToCsvFile extends Thread {

    private String[] ArrayOfVariables;
    private String[] variable_identifiers;
    private File f;
    private FileOutputStream oStream;

    private volatile boolean stopWorker = false;

    public DataToCsvFile() {
        try {
            this.variable_identifiers = new String[]{
                    // these should match ArrayOfVariables
                    "Input throttle (%)",
                    "Actual throttle (%)",
                    "Volts (V)",
                    "Aux volts (V)",
                    "Amps (A)",
                    "Amp hours (Ah)",
                    "Motor speed (RPM)",
                    "Speed (m/s)",
                    "Distance (m)",
                    "Temp1 (C)",
                    "Temp2 (C)",
                    "Gear ratio",
                    "Gear",
                    "Ideal gear",
                    "Efficiency (Wh/km)",
                    "Steering angle (deg)",
                    "Brake",
                    "Fan status",
                    "Fan duty (%)",

                    "Latitude (deg)",
                    "Longitude (deg)",
                    "Altitude (m)",
                    "Bearing (deg)",
                    "SpeedGPS (m/s)",
                    "GPSTime",			    // milliseconds since epoch (UTC)
                    "GPSAccuracy (m)",		// radius of 68% confidence
                    "Lap",
                    "Vehicle name",
                    "Mode",
                    "Bluetooth",

                    "SFLBearing (deg)",
                    "ObserverBearing (deg)",
                    "Slope (deg)",
                    "PerformanceMetric",	    // Arduino performance metric - higher is better
                    "Mangled data",             // mangled data count (Bluetooth)

                    "Custom 0",
                    "Custom 1",
                    "Custom 2",
                    "Custom 3",
                    "Custom 4",
                    "Custom 5",
                    "Custom 6",
                    "Custom 7",
                    "Custom 8",
                    "Custom 9"
            };
        } catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e));
            e.printStackTrace();
        }
    }

    public void run() {
        try {

            // open the file
            this.f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);
            this.oStream = new FileOutputStream(f, true);

            while (!this.stopWorker) {
                try {
                    this.ArrayOfVariables = new String[] {
                            String.format("%.0f", Global.InputThrottle),
                            String.format("%.0f", Global.ActualThrottle),
                            String.format("%.2f", Global.Volts),
                            String.format("%.2f", Global.VoltsAux),
                            String.format("%.2f", Global.Amps),
                            String.format("%.2f", Global.AmpHours),
                            String.format("%.0f", Global.MotorRPM),
                            String.format("%.1f", Global.SpeedMPS),
                            String.format("%.3f", Global.DistanceMeters),
                            String.format("%.1f", Global.TempC1),
                            String.format("%.1f", Global.TempC2),
                            String.format("%.3f", Global.GearRatio),
                            String.format("%d", Global.Gear),
                            String.format("%d", Global.IdealGear),
                            String.format("%.2f", Global.WattHoursPerMeter * 1000),
                            String.format("%.0f", Global.SteeringAngle),
                            String.format("%d", Global.Brake),
                            String.format("%d", Global.FanStatus),
                            String.format("%.0f", Global.FanDuty),

                            String.format("%.6f", Global.Latitude),
                            String.format("%.6f", Global.Longitude),
                            String.format("%.1f", Global.Altitude),
                            String.format("%.1f", Global.Bearing),
                            String.format("%.1f", Global.SpeedGPS),
                            String.format("%.1f", Global.GPSTime),
                            String.format("%.1f", Global.GPSAccuracy),
                            String.format("%d", Global.Lap),
                            Global.CarName,
                            Global.Mode.toString(),
                            Global.BTState.toString(),

                            String.format("%.0f", Global.StartFinishLineBearing),
                            String.format("%.0f", Global.BearingFromObserverToCar),
                            String.format("%.1f", Global.SlopeGradient),
                            String.format("%.1f", Global.PerformanceMetric),
                            String.format("%d", Global.MangledDataCount),

                            String.format("%.3f", Global.Custom0),
                            String.format("%.3f", Global.Custom1),
                            String.format("%.3f", Global.Custom2),
                            String.format("%.3f", Global.Custom3),
                            String.format("%.3f", Global.Custom4),
                            String.format("%.3f", Global.Custom5),
                            String.format("%.3f", Global.Custom6),
                            String.format("%.3f", Global.Custom7),
                            String.format("%.3f", Global.Custom8),
                            String.format("%.3f", Global.Custom9)
                    };

                    WriteToFile(GetLatestDataAsString());

                } catch (Exception e) {
                    // something failed with writing to file
                    EventBus.getDefault().post(new SnackbarEvent(e));
                    e.printStackTrace();
                }

                try { // for some reason this needs to be in a try/catch block
                    Thread.sleep(Global.DATA_SAVE_INTERVAL);
                } catch (Exception e) {
                    EventBus.getDefault().post(new SnackbarEvent(e));
                    e.printStackTrace();
                }
            }

            // if we reach here then the thread is stopping

            try {
                oStream.close();
                EventBus.getDefault().post(new UpdateUIEvent(UpdateUIEvent.EventType.DataFile));
            } catch (IOException e) {
                EventBus.getDefault().post(new SnackbarEvent(e));
                e.printStackTrace();
            }

        } catch (Exception e) {
            // something failed with opening the file
            EventBus.getDefault().post(new SnackbarEvent(e));
            e.printStackTrace();
        }
    }

    /**
     * Returns a string representation of the sensor variables stored
     * in the Global class with the current timestamp
     * @return a string formatted as "timestamp,x,y,z,..."
     */
    private String GetLatestDataAsString() {
        StringBuilder data_string = new StringBuilder(String.valueOf(System.currentTimeMillis()) + ",");

        for (String value : this.ArrayOfVariables) {
            data_string.append(value).append(",");
        }

        //remove last comma
        data_string = new StringBuilder(data_string.substring(0, data_string.length() - 1));

        // add newline
        data_string.append("\n");

        return data_string.toString();
    }

    private void WriteToFile(String data) {
        if (data.length() > 0) {
            try {

                if (f.length() == 0) {
                    // file is empty; write headers

					/* 	|	timestamp	|	t 	| 	v	|	i	|  ...	|
						|				|		|		|		|		|
					*/
                    StringBuilder headers = new StringBuilder("timestamp,");
                    for (int i = 0; i <= this.variable_identifiers.length - 1; i++) {
                        headers.append(variable_identifiers[i]).append(",");
                    }
                    // remove the last comma
                    headers = new StringBuilder(headers.substring(0, headers.length() - 1));

                    // add newline
                    headers.append("\n");

                    // write to file
                    oStream.write(headers.toString().getBytes());

                    resetValues();
                }

                // Write data
                oStream.write(data.getBytes());

            } catch (Exception e) {
                EventBus.getDefault().post(new SnackbarEvent(e));
                e.printStackTrace();
                try {
                    oStream.close();
                } catch (Exception ex) {
                    EventBus.getDefault().post(new SnackbarEvent(e));
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetValues() {
        // This function holds the references to each value which needs to be reset when they are
        // written to file, e.g. accelerometer, delta distances
        Global.DeltaDistance = 0;
    }

    public void cancel() {
        this.stopWorker = true;
        try {
            if (oStream != null) {
                oStream.close();
            }
        } catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e));
            e.printStackTrace();
        }
    }
}
