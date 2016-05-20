package com.ben.echookcompanion.util;

import android.location.Location;
import android.widget.Toast;

import com.ben.echookcompanion.Global;
import com.ben.echookcompanion.MainActivity;
import com.ben.echookcompanion.threads.RaceStartMonitor;

import java.util.ArrayList;
import java.util.List;


public class RaceObserver implements RaceStartMonitor.ThrottleListener{
	private Location myLocation;

	private float bearingToStartFinishLine;
	private float currentBearingToVehicle;
	private float previousBearingToVehicle;
	private volatile boolean raceStarted = false;

	private List<RaceObserverListener> _listeners = new ArrayList<>();

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
			MainActivity.showMessage("Launch Mode Active - waiting for throttle input (minimum 20%)", Toast.LENGTH_LONG);
		} catch (Exception e) {
			MainActivity.showError(e);
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
			if (Global.StartFinishLineLocation.bearingTo(location) <= Global.StartFinishLineBearing + 45
					&& Global.StartFinishLineLocation.bearingTo(location) >= Global.StartFinishLineBearing - 45) {
				return true;
			}
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
		MainActivity.showMessage("Race start detected - lap timing has begun", Toast.LENGTH_LONG);
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
