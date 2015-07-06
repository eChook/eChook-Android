package com.ben.drivenbluetooth.util;

import android.location.Location;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.threads.RaceStartMonitor;

import java.util.ArrayList;
import java.util.List;


public class RaceObserver implements RaceStartMonitor.ThrottleListener{
	private Location myLocation;

	private float bearingToStartFinishLine;
	private float currentBearingToVehicle;
	private float previousBearingToVehicle;
	private boolean raceStarted = false;

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
		currentBearingToVehicle = newLocation.getBearing();
		CheckIfCrossStartFinishLine();
	}

	public Location getMyLocation() {
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
			myRaceStartMonitor.start();
			MainActivity.showMessage("Launch Mode Active", Toast.LENGTH_LONG);
		} catch (Exception e) {
			MainActivity.showError(e);
		}
	}

	private void CheckIfCrossStartFinishLine() {
		if (CheckIfCrossStartFinishLine_Observer()) _fireCrossStartFinishLine();
	}

	private boolean CheckIfCrossStartFinishLine_Observer() {
		boolean retVal = false;
		if (raceStarted) {
			switch (Orientation) {
				case CLOCKWISE:
					if (previousBearingToVehicle <= bearingToStartFinishLine && currentBearingToVehicle >= bearingToStartFinishLine) {
						retVal = true;
					}
					break;
				case ANTICLOCKWISE:
					if (previousBearingToVehicle >= bearingToStartFinishLine && currentBearingToVehicle <= bearingToStartFinishLine) {
						retVal = true;
					}
					break;
			}
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

	/*===================*/
	/* RACESTARTMONITOR IMPLEMENTATION
	/*===================*/
	@Override
	public void onThrottleMax() {
		_fireRaceStart();
		raceStarted = true;
	}

	/*===================*/
	/* LISTENER REGISTERING/DEREGISTERING
	/*===================*/
	public synchronized void addLapObserverListener(RaceObserverListener lol) {
		_listeners.add(lol);
	}

	public synchronized void removeLapObserverListener(RaceObserverListener lol) {
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
