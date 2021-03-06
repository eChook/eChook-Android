package com.ben.drivenbluetooth.util;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.widget.Chronometer;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.events.LocationEvent;
import com.ben.drivenbluetooth.events.PreferenceEvent;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class DrivenLocation implements GoogleApiClient.ConnectionCallbacks,
								GoogleApiClient.OnConnectionFailedListener,
								LocationListener,
								RaceObserver.RaceObserverListener
{
private static GoogleApiClient GoogleApi;
private Location currentLocation;

private Chronometer mTimer;
private TextView mPrevLapTime;
private TextView mLapNumber;

private PolylineOptions pathHistory = new PolylineOptions();      // polyline for drawing paths on the map
public CircleOptions ObserverLocation = new CircleOptions();   // circle for showing the observer location
private RaceObserver myRaceObserver = null;
private LocationRequest mLocationRequest;
private final ArrayList<Location> InitialRaceDataPoints = new ArrayList<>();  // store the first few location points to calculate start position and bearing
private boolean storePointsIntoInitialArray = false;  // flag to write locations to the above array or not
private boolean CrossStartFinishLineTriggerEnabled = true;  // used for the timeout to make sure we don't get excessive triggers firing if the location is slightly erratic

private Context context;

/*===================*/
/* DRIVENLOCATION
   /*===================*/
public DrivenLocation(Chronometer timer, TextView prevLapTime, Context ctx) {
								context = ctx;
								mTimer = timer;
								mPrevLapTime = prevLapTime;
								createLocationRequest();
								buildGoogleApiClient();
								EventBus.getDefault().register(this);
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
public void onConnectionFailed(@NonNull ConnectionResult result) {
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

@Subscribe
public void onPreferenceEvent(PreferenceEvent e) {
								if (e.eventType == PreferenceEvent.EventType.LocationChange) {
																startLocationUpdates();
								}
}

@Override
public void onLocationChanged(Location location) {
								Location previousLocation = currentLocation;
								currentLocation = location;
								if (storePointsIntoInitialArray && InitialRaceDataPoints.size() < 5) {
																InitialRaceDataPoints.add(location);
								}

								_updateLocations(location);
								Global.SlopeGradient = CalculateSlopeGradientInDegrees(previousLocation, currentLocation);
								// Notify UDPsender that we have a new location
								Message msg = new Message();
								msg.obj = location;

//		if (MainActivity.mTelemetrySender != null) {
//			MainActivity.mTelemetrySender.LocationHandler.sendMessage(msg);
//		}

								if (currentLocation != null && previousLocation != null) {
																Global.DeltaDistance = calculateDistanceBetween(previousLocation, currentLocation);
								}

								addToPathHistory(location);
								if (myRaceObserver != null) {
																myRaceObserver.updateLocation(location);
								}
}

@NonNull
private Double CalculateSlopeGradientInDegrees(Location previousLocation, Location currentLocation) {
								// calculate distance between points
								double deltaDistance = previousLocation.distanceTo(currentLocation);

								// calculate altitude difference
								double deltaAltitude = currentLocation.getAltitude() - previousLocation.getAltitude();

								// calculate slope
								// slope = arctan(deltaAltitude / deltaDistance)
								return Math.toDegrees(Math.atan(deltaAltitude / deltaDistance));
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

private synchronized void buildGoogleApiClient() {
								if (context != null) {
																GoogleApi = new GoogleApiClient.Builder(context)
																												.addConnectionCallbacks(this)
																												.addOnConnectionFailedListener(this)
																												.addApi(LocationServices.API)
																												.build();
								}
}

private void createLocationRequest() {
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

private void startLocationUpdates() {
								try {
																LocationServices.FusedLocationApi.requestLocationUpdates(GoogleApi, mLocationRequest, this);
																currentLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApi);
																Global.Latitude = currentLocation.getLatitude();
																Global.Longitude = currentLocation.getLongitude();
								} catch (SecurityException e) {
																// this should never happen, but if it does...
																EventBus.getDefault().post(new SnackbarEvent(e));
																e.printStackTrace();
																EventBus.getDefault().post(new SnackbarEvent("Permission denied to allow location services"));
								} catch (Exception e) {
																EventBus.getDefault().post(new SnackbarEvent(e));
																e.printStackTrace();
								}
}

private void stopLocationUpdates() {
								LocationServices.FusedLocationApi.removeLocationUpdates(GoogleApi, this);
}

private float calculateDistanceBetween(Location location1, Location location2) {
								return location1.distanceTo(location2);
}

private void calculateInitialBearing() {
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

private void calculateTrackOrientation() {
								// Linear regression time!
								// We need to calculate the bearing to each location point and throw them into an array
								Location observerLocation = myRaceObserver.getLocation();
								SimpleRegression simpleRegression = new SimpleRegression(true); // true makes intercept non-zero
								for (int i = 0; i < InitialRaceDataPoints.size(); i++) {
																float bearing = observerLocation.bearingTo(InitialRaceDataPoints.get(i));
																simpleRegression.addData(new double[][] {
																																{i, bearing}
																								});
								}

								// get slope of line
								if (simpleRegression.getSlope() > 0) {
																// the bearing is INCREASING which means the vehicle is traveling clockwise
																myRaceObserver.setOrientation(RaceObserver.ORIENTATION.CLOCKWISE);
								} else {
																// the bearing is DECREASING which means the vehicle is traveling counterclockwise
																myRaceObserver.setOrientation(RaceObserver.ORIENTATION.ANTICLOCKWISE);
								}

								MainActivity.MainActivityHandler.post(new Runnable() {
																								@Override
																								public void run() {

																								}
																});
}

private void addToPathHistory(Location loc) {
								this.pathHistory.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
}

private void resetPathHistory() {
								this.pathHistory = null;
								this.pathHistory = new PolylineOptions();
}

private void resetLapTimer() {
								mTimer.stop();
								mTimer.setBase(SystemClock.elapsedRealtime());
								mTimer.start();
}

@Subscribe(threadMode = ThreadMode.MAIN)
public void UpdateLocationSetting(PreferenceEvent e) {
								if (e.eventType == PreferenceEvent.EventType.LocationChange) {
																switch (Global.LocationStatus) {
																case ENABLED:
																								startLocationUpdates();
																								break;
																case DISABLED:
																								stopLocationUpdates();
																								break;
																}
								}
}

public void ActivateLaunchMode() {
								if (Global.LocationStatus == Global.LOCATION.ENABLED) {
																if (currentLocation != null) {
																								Global.StartFinishLineLocation = currentLocation;
																								if (myRaceObserver != null) {
																																myRaceObserver.ActivateLaunchMode(Global.StartFinishLineLocation);
																								} else {
																																EventBus.getDefault().post(new SnackbarEvent("Observer not defined - cannot activate launch mode!"));
																								}
																} else {
																								EventBus.getDefault().post(new SnackbarEvent("Could not obtain your location. Please try again"));
																}
								} else {
																EventBus.getDefault().post(new SnackbarEvent("Location updates are not enabled on your device. Please go to settings and enable it!"));
								}
}

public void SimulateCrossStartFinishLine() {
								if (myRaceObserver != null) {
																myRaceObserver.SimulateCrossStartFinishLine();
								} else {
																EventBus.getDefault().post(new SnackbarEvent("Observer is not yet defined!"));
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
																// disable crossing detection temporarily. Effectively a 'debounce' in case of dodgy readings
																CrossStartFinishLineTriggerEnabled = false;

																// UpdateLocationSetting the laptimer and counter tooltips
																mPrevLapTime.setText(mTimer.getText());

																// update lap data
																if (Global.Lap > 0) {
																								Global.LapDataList.get(Global.Lap - 1).setLapTime(SystemClock.elapsedRealtime() - mTimer.getBase()); // set previous lap time
																}

																// get delta time between last two laps
																long deltaMillis = 0;
																if (Global.Lap > 1) {
																								deltaMillis = Global.LapDataList.get(Global.Lap - 1).getLapTimeMillis() - Global.LapDataList.get(Global.Lap - 2).getLapTimeMillis();
																}

																// add new lap
																Global.LapDataList.add(new LapData());
																Global.Lap++;

																// Update the lap text
																EventBus.getDefault().post(new LocationEvent(LocationEvent.EventType.NewLap));

																// reset the lap timer
																resetLapTimer();

																// re-enable cross detection after 20 seconds
																new Handler().postDelayed(new Runnable() {
																																@Override
																																public void run() {
																																								CrossStartFinishLineTriggerEnabled = true;
																																}
																								}, 20000);

																// reset the path history on the map (black trail)
																resetPathHistory();

																// show lap summary message
																if (Global.Lap > 1) {
																								EventBus.getDefault().post(new SnackbarEvent(
																																																											String.format("Lap %s - %s (%+02.3fs)",
																																																																									Global.Lap - 1,
																																																																									Global.LapDataList.get(Global.Lap - 2).getLapTimeString(),
																																																																									(float) deltaMillis / 1000.0)));
																}
								}
}

@Override
/* This function is fired when the car is in Launch Mode and the Throttle is pushed to 100% */
public void onRaceStart() {
								Global.RaceStartTime = System.currentTimeMillis();
								Global.StartFinishLineBearing = (double) myRaceObserver.getBearingToStartFinishLine();

								InitialRaceDataPoints.add(currentLocation);
								storePointsIntoInitialArray = true;

								// disable crossing detection temporarily
								CrossStartFinishLineTriggerEnabled = false;

								// After 10 seconds of the race we want to calculate the initial direction
								new Handler().postDelayed(new Runnable() {
																								@Override
																								public void run() {
																								        //calculateInitialBearing();
																								        //calculateTrackOrientation();
																																storePointsIntoInitialArray = false;
																																CrossStartFinishLineTriggerEnabled = true;
																								}
																}, 20000);

								// add to the lap data list
								Global.LapDataList.add(new LapData());
								Global.Lap++;

								// UpdateLocationSetting timer
								mTimer.setBase(SystemClock.elapsedRealtime());
								mTimer.start();
}

public Location getCurrentLocation() {
								return currentLocation;
}
}
