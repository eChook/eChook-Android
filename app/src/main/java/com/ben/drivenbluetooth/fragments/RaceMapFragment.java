package com.ben.drivenbluetooth.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.ben.drivenbluetooth.util.RaceObserver;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class RaceMapFragment extends Fragment
								implements 	OnMapReadyCallback,
											GoogleMap.OnMapClickListener,
											GoogleMap.OnInfoWindowClickListener
{
	private GoogleMap map;
	private MapFragment mFragment;

	private TextView Current;
	private TextView Voltage;
	private TextView RPM;
	private TextView Speed;

	private TextView CurBearing;
	private TextView SFLBearing;

	private Polyline pathHistory;
	private Circle ObserverLocation;

	private static Timer FragmentUpdateTimer;

	/*===================*/
	/* MAINMAPFRAGMENT
	/*===================*/
	public RaceMapFragment() {
		// Required empty public constructor
	}

	/*===================*/
	/* INITIALIZERS
	/*===================*/
	private void InitializeDataFields() {
		View v = getView();
		Current 		= (TextView) v.findViewById(R.id.current);
		Voltage 		= (TextView) v.findViewById(R.id.voltage);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
		CurBearing		= (TextView) v.findViewById(R.id.txtCurBearing);
		SFLBearing		= (TextView) v.findViewById(R.id.txtSFLBearing);
	}

	/*===================*/
	/* LIFECYCLE
	/*===================*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_map, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		mFragment.getMapAsync(this);
		InitializeDataFields();
		StartFragmentUpdater();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		StopFragmentUpdater();

		Current		= null;
		Voltage		= null;
		RPM			= null;
		Speed		= null;
		CurBearing 	= null;
		SFLBearing 	= null;
		map 		= null;
		mFragment	= null;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		try {
			map = googleMap;
			map.setMyLocationEnabled(true);
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(Global.Latitude, Global.Longitude))
					.zoom(16)                   // Sets the zoom
					.bearing(Global.Bearing.floatValue())                // Sets the orientation of the camera to the bearing of the car
					//.tilt(30)                   // Sets the tilt of the camera to 30 degrees
					.build();
			map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			map.setOnMapClickListener(this);
			map.setOnInfoWindowClickListener(this);
			map.getUiSettings().setMapToolbarEnabled(false);
			pathHistory = map.addPolyline(MainActivity.myDrivenLocation.pathHistory);
			if (ObserverLocation != null) {
				try {
					ObserverLocation = map.addCircle(MainActivity.myDrivenLocation.ObserverLocation);
				} catch (NullPointerException ignored) {}
			}

			if (Global.StartFinishLineLocation != null) {
				map.addCircle(new CircleOptions()
						.center(new LatLng(Global.StartFinishLineLocation.getLatitude(), Global.StartFinishLineLocation.getLongitude()))
						.radius(5)
						.fillColor(Color.WHITE));
			}
		} catch (Exception e) {
			MainActivity.showError(e);
		}
	}

	/*===================*/
	/* INTERACTION LISTENERS
	/*===================*/
	@Override
	public void onMapClick(LatLng latLng) {
		map.clear(); // clear any markers already present
		map.addMarker(new MarkerOptions()
				.position(latLng)
				.title("Observer location")
				.snippet("Click here to confirm"));
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		final Location loc = new Location("");
		loc.setLatitude(marker.getPosition().latitude);
		loc.setLongitude(marker.getPosition().longitude);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Track orientation");
		builder.setPositiveButton("Clockwise", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				MainActivity.myDrivenLocation.setMyRaceObserverLocation(loc, RaceObserver.ORIENTATION.CLOCKWISE);
				map.clear();
				ObserverLocation = map.addCircle(MainActivity.myDrivenLocation.ObserverLocation);
			}
		});
		builder.setNegativeButton("Anticlockwise", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				MainActivity.myDrivenLocation.setMyRaceObserverLocation(loc, RaceObserver.ORIENTATION.ANTICLOCKWISE);
				map.clear();
				ObserverLocation = map.addCircle(MainActivity.myDrivenLocation.ObserverLocation);
			}
		});
		builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				map.clear();
				ObserverLocation = map.addCircle(MainActivity.myDrivenLocation.ObserverLocation);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/*===================*/
	/* FRAGMENT UPDATING
	/*===================*/
	public void UpdateFragmentUI() {
		UpdateCurrent();
		UpdateMotorRPM();
		UpdateVoltage();
		UpdateSpeed();
	}

	public void UpdateMap() {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(Global.Latitude, Global.Longitude))      // Sets the center of the map to Mountain View
				.zoom(16)                   // Sets the zoom
				.bearing(Global.Bearing.floatValue())                // Sets the orientation of the camera to east
				//.tilt(30)                   // Sets the tilt of the camera to 30 degrees
				.build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		pathHistory.setPoints(MainActivity.myDrivenLocation.pathHistory.getPoints());
		try {
			ObserverLocation.setCenter(MainActivity.myDrivenLocation.ObserverLocation.getCenter());
		} catch (NullPointerException ignored) {}			// Observerlocation has not been initialized yet so do nothing

		if (Global.StartFinishLineLocation != null) {
			map.addCircle(new CircleOptions()
					.center(new LatLng(Global.StartFinishLineLocation.getLatitude(), Global.StartFinishLineLocation.getLongitude()))
					.radius(5)
					.fillColor(Color.WHITE));
		}

		UpdateBearings();
	}

	private void UpdateVoltage() {
		try {
			this.Voltage.setText(String.format("%.2f", Global.Volts) + " V");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateCurrent() {
		try {
			this.Current.setText(String.format("%.1f", Global.Amps) + " A");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateSpeed() {
		try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
				this.Speed.setText(String.format("%.1f", Global.SpeedMPH) + " mph");
			} else if (Global.Unit == Global.UNIT.KPH) {
				this.Speed.setText(String.format("%.1f", Global.SpeedKPH) + " kph");
			}

		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateMotorRPM() {
		try {
			this.RPM.setText(String.format("%.0f", Global.MotorRPM) + " RPM");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateBearings() {
		CurBearing.setText(String.format("%.1f", MainActivity.myDrivenLocation.GetRaceObserverBearing_Current()));
		SFLBearing.setText(String.format("%.1f", MainActivity.myDrivenLocation.GetRaceObserverBearing_SFL()));
	}

	private void StartFragmentUpdater() {
		TimerTask fragmentUpdateTask = new TimerTask() {
			public void run() {
				MainActivity.MainActivityHandler.post(new Runnable() {
					public void run() {
						UpdateFragmentUI();
					}
				});
			}
		};

		TimerTask mapUpdateTask = new TimerTask() {
			public void run() {
				MainActivity.MainActivityHandler.post(new Runnable() {
					@Override
					public void run() {
						UpdateMap();
					}
				});
			}
		};
		FragmentUpdateTimer = new Timer();
		FragmentUpdateTimer.schedule(mapUpdateTask, Global.MAP_UPDATE_INTERVAL, Global.MAP_UPDATE_INTERVAL);
		FragmentUpdateTimer.schedule(fragmentUpdateTask, 0, Global.FAST_UI_UPDATE_INTERVAL);
	}

	private void StopFragmentUpdater() {
		try {
			FragmentUpdateTimer.cancel();
			FragmentUpdateTimer.purge();
		} catch (Exception ignored) {}
	}
}
