package com.ben.drivenbluetooth.util;

import android.location.Location;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.ben.drivenbluetooth.threads.RaceStartMonitor;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class RaceObserver implements RaceStartMonitor.ThrottleListener{
	private final Location myLocation;

	private float bearingToStartFinishLine;
	private float currentBearingToVehicle;
	private float previousBearingToVehicle;
	private volatile boolean raceStarted = false;

	private final List<RaceObserverListener> _listeners = new ArrayList<>();

	private RaceStartMonitor myRaceStartMonitor = new RaceStartMonitor(this);

	public enum ORIENTATION {CLOCKWISE, ANTICLOCKWISE}
	private ORIENTATION Orientation;

	/*===================*/
	/* INTERFACE
	/*===================*/
	public interface RaceObserverListener {
		void onCrossStartFinishLine();
		void onRaceStart();
	}

	/*===================*/
	/* RACEOBSERVER
	/*===================*/
	public RaceObserver(Location location, ORIENTATION orientation) {
		myLocation = location;
		Orientation = orientation;
	}

	/*===================*/
	/* MAIN FUNCS
	/*===================*/
	public void updateLocation(Location newLocation) {
		previousBearingToVehicle = currentBearingToVehicle;
		currentBearingToVehicle = myLocation.bearingTo(newLocation);
        Global.BearingFromObserverToCar = currentBearingToVehicle;
		CheckIfCrossStartFinishLine();
	}

	public Location getLocation() {
		return myLocation;
	}

	public void ActivateLaunchMode(Location startLineLocation) {
		try {
			bearingToStartFinishLine = myLocation.bearingTo(startLineLocation);
			if (myRaceStartMonitor.isAlive()) {
				myRaceStartMonitor.cancel();
				myRaceStartMonitor.join(); // wait for it to finish...
				myRaceStartMonitor = new RaceStartMonitor(this);
			}

            if (myRaceStartMonitor.getState() == Thread.State.TERMINATED) {
                myRaceStartMonitor = new RaceStartMonitor(this);
            }

			myRaceStartMonitor.start();
			EventBus.getDefault().post(new SnackbarEvent("Launch Mode Active - waiting for throttle input (minimum 20%)"));
		} catch (Exception e) {
			EventBus.getDefault().post(new SnackbarEvent(e));
e.printStackTrace();
		}
	}

	public void SimulateCrossStartFinishLine() {
		switch (Orientation) {
			case CLOCKWISE:
				previousBearingToVehicle = bearingToStartFinishLine - 1;
				currentBearingToVehicle = bearingToStartFinishLine + 1;
				break;
			case ANTICLOCKWISE:
				previousBearingToVehicle = bearingToStartFinishLine + 1;
				currentBearingToVehicle = bearingToStartFinishLine - 1;
				break;
		}
		CheckIfCrossStartFinishLine();
	}

	private void CheckIfCrossStartFinishLine() {
		if (CheckIfCrossStartFinishLine_Observer()) _fireCrossStartFinishLine();
	}

	private boolean CheckIfCrossStartFinishLine_Observer() {
		boolean retVal = false;
		if (raceStarted) switch (Orientation) {
			case CLOCKWISE:
				retVal = previousBearingToVehicle <= bearingToStartFinishLine && currentBearingToVehicle >= bearingToStartFinishLine;
				break;
			case ANTICLOCKWISE:
				retVal = previousBearingToVehicle >= bearingToStartFinishLine && currentBearingToVehicle <= bearingToStartFinishLine;
				break;
		}
		return retVal;
	}

	private boolean CheckIfCrossStartFinishLine_StartVector(Location location) {
		if (location.distanceTo(Global.StartFinishLineLocation) <= Global.LAP_TRIGGER_RANGE) {
			// we are in range! check to see if the bearing adds up
			return Global.StartFinishLineLocation.bearingTo(location) <= Global.StartFinishLineBearing + 45
					&& Global.StartFinishLineLocation.bearingTo(location) >= Global.StartFinishLineBearing - 45;
		}
		return false;
	}

	public float getCurrentBearingToVehicle() {
		return currentBearingToVehicle;
	}

	public float getBearingToStartFinishLine() {
		return bearingToStartFinishLine;
	}

    public void setOrientation(ORIENTATION or) {
        Orientation = or;
    }

    public ORIENTATION getOrientation() {
        return Orientation;
    }

	/*===================*/
	/* RACESTARTMONITOR IMPLEMENTATION
	/*===================*/
	@Override
	public void onThrottleMax() {
		_fireRaceStart();
		raceStarted = true;
		EventBus.getDefault().post(new SnackbarEvent("Race start detected - lap timing has begun"));
	}

	/*===================*/
	/* LISTENER REGISTERING/DEREGISTERING
	/*===================*/
	public synchronized void addRaceObserverListener(RaceObserverListener lol) {
		_listeners.add(lol);
	}

	public synchronized void removeRaceObserverListener(RaceObserverListener lol) {
		_listeners.remove(lol);
	}

	/*===================*/
	/* EVENT RAISERS
	/*===================*/
	private synchronized void _fireCrossStartFinishLine() {
		for (RaceObserverListener _listener : _listeners) {
			_listener.onCrossStartFinishLine();
		}
	}

	private synchronized void _fireRaceStart() {
		for (RaceObserverListener _listener : _listeners) {
			_listener.onRaceStart();
		}
	}
}
