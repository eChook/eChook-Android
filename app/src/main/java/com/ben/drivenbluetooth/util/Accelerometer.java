package com.ben.drivenbluetooth.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.events.SnackbarEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class Accelerometer implements SensorEventListener {

    private final SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean supportsAccelerometer = false;
    private float[] AccelerometerSensorValues;

    public Accelerometer(SensorManager sensorManager) {
        this.mSensorManager = sensorManager;
        InitializeAccelerometer();
    }

    private void InitializeAccelerometer() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            List<Sensor> gravSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
            if (gravSensors.size() > 0) {
                mAccelerometer = gravSensors.get(0);
                supportsAccelerometer = true;
            } else {
                EventBus.getDefault().post(new SnackbarEvent("Device does not support accelerometer"));
                supportsAccelerometer = false;
            }
        } else {
            EventBus.getDefault().post(new SnackbarEvent("Device does not support accelerometer"));
            supportsAccelerometer = false;
        }
    }

    private void startAccelerometerData() {
        if (supportsAccelerometer && Global.Accelerometer == Global.ACCELEROMETER.ENABLED) {
            try {
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (Exception ignored) {
            }
        }
    }

    private void stopAccelerometerData() {
        try {
            mSensorManager.unregisterListener(this);
        } catch (Exception ignored) {
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
			-------------
		 */

        AccelerometerSensorValues = lowPass(event.values.clone(), AccelerometerSensorValues);

        Global.Gx = (float) Math.round(AccelerometerSensorValues[0] * 1000) / 1000;
        Global.Gy = (float) Math.round(AccelerometerSensorValues[1] * 1000) / 1000;
        Global.Gz = (float) Math.round(AccelerometerSensorValues[2] * 1000) / 1000;
        Global.SteeringAngle = calculateAngle(Global.Gy);
    }

    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + 0.25f * (input[i] - output[i]);
        }
        return output;
    }

    @Deprecated
    public void update() {
        stopAccelerometerData();
        startAccelerometerData();
    }

    private double calculateAngle(float y) {
        return Math.acos(y / 9.81);
    }
}
