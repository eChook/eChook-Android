package com.ben.drivenbluetooth.util;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class DrivenLocation implements 	GoogleApiClient.ConnectionCallbacks,
										GoogleApiClient.OnConnectionFailedListener,
										LocationListener,
										RaceObserver.RaceObserverListener
{
	public static GoogleApiClient GoogleApi;
	public Location CurrentLocation;
	public Location PreviousLocation;

	public PolylineOptions pathHistory = new PolylineOptions();		// polyline for drawing paths on the map
	public CircleOptions ObserverLocation = new CircleOptions();		// circle for showing the observer location
	private RaceObserver myRaceObserver = null;
	private LocationRequest mLocationRequest;
	private ArrayList<Location> InitialRaceDataPoints = new ArrayList<>();	// store the first few location points to calculate start position and bearing
	private boolean storePointsIntoInitialArray = false;	// flag to write locations to the above array or not
	private boolean CrossStartFinishLineTriggerEnabled = true;	// used for the timeout to make sure we don't get excessive triggers firing if the location is slightly erratic

	/*===================*/
	/* DRIVENLOCATION
	/*===================*/
	public DrivenLocation() {
		createLocationRequest();
		buildGoogleApiClient();
	}

	/*===================*/
	/* GOOGLE API CLIENT
	/*===================*/
	@Override
	public void onConnected(Bundle connectionHint) {
		if (Global.LocationStatus == Global.LOCATION.ENABLED) {
			startLocationUpdates();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		//Log.i(TAG, "Connection suspended");
		GoogleApi.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		PreviousLocation = CurrentLocation;
		CurrentLocation = location;
		if (storePointsIntoInitialArray && InitialRaceDataPoints.size() < 5) {
			InitialRaceDataPoints.add(location);
		}

		_updateLocations(location);

		if (CurrentLocation != null && PreviousLocation != null) {
			Global.DeltaDistance = _calculateDistanceBetween(PreviousLocation, CurrentLocation);
		}

		_addToPathHistory(location);
		if (myRaceObserver != null) {
			myRaceObserver.updateLocation(location);
		}
	}

	private void _updateLocations(Location location) {
		if (location.getAccuracy() <= Global.MinGPSAccuracy) {
			Global.Latitude = location.getLatitude();
			Global.Longitude = location.getLongitude();
			Global.Altitude = location.getAltitude();
			if (location.hasBearing()) { Global.Bearing = (double) location.getBearing(); }
			Global.SpeedGPS = (double) location.getSpeed();
			Global.GPSTime = (double) location.getTime();
			Global.GPSAccuracy = (double) location.getAccuracy();
		}
	}

	protected synchronized void buildGoogleApiClient() {
		GoogleApi = new GoogleApiClient.Builder(MainActivity.getAppContext())
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	protected void createLocationRequest() {
		this.mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(Global.LOCATION_INTERVAL);
		mLocationRequest.setFastestInterval(Global.LOCATION_FAST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	public void connect() {
		GoogleApi.connect();
	}

	public void disconnect() {
		if (GoogleApi.isConnected()) {
			GoogleApi.disconnect();
		}
	}

	/*===================*/
	/* MAIN FUNCS
	/*===================*/
	public void setMyRaceObserverLocation(Location loc, RaceObserver.ORIENTATION ori) {
		myRaceObserver = new RaceObserver(loc, ori);
		myRaceObserver.addRaceObserverListener(this);
		ObserverLocation = new CircleOptions()
				.center(new LatLng(loc.getLatitude(), loc.getLongitude()))
				.radius(5)
				.fillColor(Color.RED);
	}

	protected void startLocationUpdates() {
		try {
			LocationServices.FusedLocationApi.requestLocationUpdates(
					GoogleApi, mLocationRequest, this);
			CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApi);
			Global.Latitude = CurrentLocation.getLatitude();
			Global.Longitude = CurrentLocation.getLongitude();
		} catch (Exception e) {
			MainActivity.showError(e);
		}
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				GoogleApi, this);
	}

	private float _calculateDistanceBetween(Location location1, Location location2) {
		return location1.distanceTo(location2);
	}

	private void _calculateInitialBearing() {
		Location start = null;
		// sync not required as this function and onRaceStart() run on the same thread
		// i.e. they cannot be called at the same time
		for (Location _location : InitialRaceDataPoints) {
			if (_location.getAccuracy() <= Global.MinGPSAccuracy) {
				if (start == null) {
					start = _location;
				} else {
					Global.StartFinishLineBearing = (double) start.bearingTo(_location);
				}
			}
		}
	}

	private void _addToPathHistory(Location loc) {
		this.pathHistory.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
	}

	private void _resetPathHistory() {
		this.pathHistory = null;
		this.pathHistory = new PolylineOptions();
	}

	private void _resetLapTimer() {
		MainActivity.LapTimer.stop();
		MainActivity.LapTimer.setBase(SystemClock.elapsedRealtime());
		MainActivity.LapTimer.start();
	}

	public void update() {
		switch (Global.LocationStatus) {
			case ENABLED:
				startLocationUpdates();
				break;
			case DISABLED:
				stopLocationUpdates();
				break;
		}
	}

	public void ActivateLaunchMode() {
		Global.StartFinishLineLocation = CurrentLocation;
		if (myRaceObserver != null) {
			myRaceObserver.ActivateLaunchMode(Global.StartFinishLineLocation);
		} else {
			MainActivity.showMessage("Observer not defined - cannot activate launch mode!");
		}
	}

	public void SimulateCrossStartFinishLine() {
		if (myRaceObserver != null) {
			myRaceObserver.SimulateCrossStartFinishLine();
		} else {
			MainActivity.showMessage("Observer is not yet defined!");
		}
	}

	public float GetRaceObserverBearing_Current() {
		if (myRaceObserver != null) return myRaceObserver.getCurrentBearingToVehicle();
		return 0f;
	}

	public float GetRaceObserverBearing_SFL() {
		if (myRaceObserver != null) return myRaceObserver.getBearingToStartFinishLine();
		return 0f;
	}

	/*===================*/
	/* RACEOBSERVER IMPLEMENTATION
	/*===================*/
	@Override
	public void onCrossStartFinishLine() {
		if (CrossStartFinishLineTriggerEnabled) {
			CrossStartFinishLineTriggerEnabled = false; // disable crossing detection temporarily
			MainActivity.prevLapTime.setText(MainActivity.LapTimer.getText());
			Global.LapDataList.add(new LapData());
			Global.LapDataList.get(Global.LapDataList.size() - 2).lapTime = MainActivity.LapTimer.getText().toString(); // set previous lap time
			Global.Lap++;
			_resetLapTimer();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					CrossStartFinishLineTriggerEnabled = true;
				}
			}, 20000); // enable crossing detection after 20 seconds
		}

		_resetPathHistory();
	}

	@Override
	/* This function is fired when the car is in Launch Mode and the Throttle is pushed to 100% */
	public void onRaceStart() {
		InitialRaceDataPoints.add(CurrentLocation);
		storePointsIntoInitialArray = true;
		CrossStartFinishLineTriggerEnabled = false;	// disable crossing detection temporarily
		// After 10 seconds of the race we want to calculate the initial direction
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				_calculateInitialBearing();
				storePointsIntoInitialArray = false;
				CrossStartFinishLineTriggerEnabled = true;
			}
		}, 20000);
		Global.LapDataList.add(new LapData());
		Global.Lap++; // first lap has begun

		MainActivity.LapTimer.setBase(SystemClock.elapsedRealtime());
		MainActivity.LapTimer.start();
	}
	/*===================*/



}
