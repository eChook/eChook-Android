package com.ben.drivenbluetooth.util;

import android.location.Location;
import android.os.Bundle;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
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
	public String mLastUpdateTime;
	public Boolean mRequestingLocationUpdates = true;
	private LocationRequest mLocationRequest;
	private ArrayList<Location> InitialRaceDataPoints = new ArrayList<>();
	public PolylineOptions pathHistory = new PolylineOptions();
	public RaceObserver myRaceObserver;

	public DrivenLocation() {
		createLocationRequest();
		buildGoogleApiClient();
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

	@Override
	public void onConnected(Bundle connectionHint) {
		if (Global.Location == Global.LOCATION.ENABLED) {
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

	protected void startLocationUpdates() {
		try {
			LocationServices.FusedLocationApi.requestLocationUpdates(
					GoogleApi, mLocationRequest, this);
			CurrentLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApi);
			Global.Latitude = CurrentLocation.getLatitude();
			Global.Longitude = CurrentLocation.getLongitude();
		} catch (Exception e) {
			e.toString();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		PreviousLocation = CurrentLocation;
		CurrentLocation = location;
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
			Global.DeltaDistance = calculateDistanceBetween(PreviousLocation, CurrentLocation);
		}

		mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
		Global.LocationUpdateCounter++;

		addToPathHistory(location);
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(
				GoogleApi, this);
	}

	protected float calculateDistanceBetween(Location location1, Location location2) {
		return location1.distanceTo(location2);
	}

	private void addToPathHistory(Location loc) {
		if (pathHistory.getPoints().size() >= 20) {
			this.pathHistory = null;
			this.pathHistory = new PolylineOptions();
		}
		this.pathHistory.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
	}

	public void update() {
		switch (Global.Location) {
			case ENABLED:
				startLocationUpdates();
				break;
			case DISABLED:
				stopLocationUpdates();
				break;
		}
	}

	@Override
	public void onCrossStartFinishLine() {

	}

	@Override
	public void onRaceStart() {

	}
}
