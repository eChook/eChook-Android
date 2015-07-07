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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

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
	private Double minLocationAccuracy = 20.0; // minimum location accuracy for calculating initial bearing
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

		Global.Latitude = location.getLatitude();
		Global.Longitude = location.getLongitude();
		Global.Altitude = location.getAltitude();

		if (location.hasBearing()) {
			Global.Bearing = (double) location.getBearing();
		}

		Global.SpeedGPS = (double) location.getSpeed();
		Global.GPSTime = (double) location.getTime();
		Global.Accuracy = (double) location.getAccuracy();

		if (CurrentLocation != null && PreviousLocation != null) {
			Global.DeltaDistance = _calculateDistanceBetween(PreviousLocation, CurrentLocation);
		}

		_addToPathHistory(location);
		if (myRaceObserver != null) {
			myRaceObserver.updateLocation(location);
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
			if (_location.getAccuracy() <= minLocationAccuracy) {
				if (start == null) {
					start = _location;
				} else {
					Global.StartFinishLineBearing = (double) start.bearingTo(_location);
				}
			}
		}
	}

	private void _addToPathHistory(Location loc) {
		if (pathHistory.getPoints().size() >= 20) {
			this.pathHistory = null;
			this.pathHistory = new PolylineOptions();
		}
		this.pathHistory.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
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
		myRaceObserver.ActivateLaunchMode(Global.StartFinishLineLocation);
	}

	public void SimulateRaceStart() {
		myRaceObserver.onThrottleMax();
	}

	public void SimulateCrossStartFinishLine() {
		myRaceObserver.SimulateCrossStartFinishLine();
	}

	/*===================*/
	/* RACEOBSERVER IMPLEMENTATION
	/*===================*/
	@Override
	public void onCrossStartFinishLine() {
		if (CrossStartFinishLineTriggerEnabled) {
			CrossStartFinishLineTriggerEnabled = false; // disable crossing detection temporarily
			MainActivity.prevLapTime.setText(MainActivity.LapTimer.getText());
			Global.Lap++;
			MainActivity.LapTimer.stop();
			MainActivity.LapTimer.setBase(SystemClock.elapsedRealtime());
			MainActivity.LapTimer.start();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					CrossStartFinishLineTriggerEnabled = true;
				}
			}, 20000);
		}
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
		Global.Lap++; // first lap has begun

		MainActivity.LapTimer.setBase(SystemClock.elapsedRealtime());
		MainActivity.LapTimer.start();
	}
	/*===================*/



}
