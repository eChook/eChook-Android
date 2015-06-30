package com.ben.drivenbluetooth.util;

import android.location.Location;
import android.widget.Toast;

import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.threads.RaceStartMonitor;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


public class RaceObserver implements RaceStartMonitor.ThrottleListener{
	private Location myLocation;

	private float bearingToStartFinishLine;
	private float currentBearingToVehicle;
	private float previousBearingToVehicle;

	private List<RaceObserverListener> _listeners = new ArrayList<>();

	private RaceStartMonitor myRaceStartMonitor = new RaceStartMonitor(this);

	public enum ORIENTATION {CLOCKWISE, ANTICLOCKWISE}
	private ORIENTATION Orientation;

	public interface RaceObserverListener {
		void onCrossStartFinishLine();
		void onRaceStart();
	}

	public RaceObserver(Location location, ORIENTATION orientation) {
		myLocation = location;
		Orientation = orientation;
	}

	public void updateLocation(Location newLocation) {
		previousBearingToVehicle = currentBearingToVehicle;
		currentBearingToVehicle = newLocation.getBearing();
		CheckIfCrossStartFinishLine();
	}

	public void ActivateLaunchMode(Location startLineLocation) {
		try {
			bearingToStartFinishLine = myLocation.bearingTo(startLineLocation);
			if (!myRaceStartMonitor.isAlive()) {
				myRaceStartMonitor.start();
				MainActivity.showMessage(MainActivity.getAppContext(), "Launch Mode Active", Toast.LENGTH_LONG);
			} else {
				MainActivity.showMessage(MainActivity.getAppContext(), "Launch Mode already actived!", Toast.LENGTH_LONG);
			}
		} catch (Exception e) {
			MainActivity.showMessage(MainActivity.getAppContext(), e.getMessage(), Toast.LENGTH_LONG);
		}
	}

	private void CheckIfCrossStartFinishLine() {
		switch (Orientation) {
			case CLOCKWISE:
				if (previousBearingToVehicle < bearingToStartFinishLine && currentBearingToVehicle > bearingToStartFinishLine) {
					_fireCrossStartFinishLine();
				}
				break;
			case ANTICLOCKWISE:
				if (previousBearingToVehicle > bearingToStartFinishLine && currentBearingToVehicle < bearingToStartFinishLine) {
					_fireCrossStartFinishLine();
				}
				break;
		}
	}

	@Override
	public void onThrottleMax() {
		_fireRaceStart();
	}

	public synchronized void addLapObserverListener(RaceObserverListener lol) {
		_listeners.add(lol);
	}

	public synchronized void removeLapObserverListener(RaceObserverListener lol) {
		_listeners.remove(lol);
	}

	private synchronized void _fireCrossStartFinishLine() {
		for (Object _listener : _listeners) {
			((RaceObserverListener) _listener).onCrossStartFinishLine();
		}
	}

	private synchronized void _fireRaceStart() {
		for (Object _listener : _listeners) {
			((RaceObserverListener) _listener).onRaceStart();
		}
	}
}
