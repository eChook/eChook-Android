package com.ben.drivenbluetooth.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class MainMapFragment 	extends Fragment
								implements 	OnMapReadyCallback,
											GoogleMap.OnMapClickListener,
											GoogleMap.OnInfoWindowClickListener
{
	private GoogleMap map;
	private OnFragmentInteractionListener mListener;

	private TextView Current;
	private TextView Voltage;
	private TextView RPM;
	private TextView Speed;

	private static Timer FragmentUpdateTimer;

	public MainMapFragment() {
		// Required empty public constructor
	}

	private void InitializeDataFields() {
		View v = getView();
		Current 		= (TextView) v.findViewById(R.id.current);
		Voltage 		= (TextView) v.findViewById(R.id.voltage);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_map, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MapFragment mFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		if (mFragment != null) {
			mFragment.getMapAsync(this);
		} else {
			MainActivity.showMessage(MainActivity.getAppContext(), "Could not find map fragment", Toast.LENGTH_LONG);
		}
		InitializeDataFields();

		StartFragmentUpdater();
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		StopFragmentUpdater();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		try {
			LatLng curLoc = new LatLng(Global.Latitude, Global.Longitude);
			map = googleMap;
			map.setMyLocationEnabled(true);
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(Global.Latitude, Global.Longitude))      // Sets the center of the map to Mountain View
					.zoom(17)                   // Sets the zoom
					.bearing(Global.Bearing.floatValue())                // Sets the orientation of the camera to east
					.tilt(30)                   // Sets the tilt of the camera to 30 degrees
					.build();
			map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			map.setOnMapClickListener(this);
			map.setOnInfoWindowClickListener(this);
		} catch (Exception e) {
			MainActivity.showMessage(MainActivity.getAppContext(), e.getMessage(), Toast.LENGTH_LONG);
		}
	}

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
				MainActivity.myDrivenLocation.myRaceObserver = new RaceObserver(loc, RaceObserver.ORIENTATION.CLOCKWISE);
			}
		});
		builder.setNegativeButton("Anticlockwise", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				MainActivity.myDrivenLocation.myRaceObserver = new RaceObserver(loc, RaceObserver.ORIENTATION.ANTICLOCKWISE);
			}
		});
		builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// do nothing
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

	public void UpdateFragmentUI() {
		UpdateCurrent();
		UpdateMotorRPM();
		UpdateVoltage();
		UpdateSpeed();
	}

	public void UpdateMap() {
		CameraUpdate herp = CameraUpdateFactory.newLatLngZoom(new LatLng(Global.Latitude, Global.Longitude), 17);
		map.animateCamera(herp);
		map.addPolyline(MainActivity.myDrivenLocation.pathHistory);
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
		FragmentUpdateTimer.schedule(mapUpdateTask, 5000, 5000);
		FragmentUpdateTimer.schedule(fragmentUpdateTask, 0, Global.UI_UPDATE_INTERVAL);
	}

	private void StopFragmentUpdater() {
		try {
			FragmentUpdateTimer.cancel();
			FragmentUpdateTimer.purge();
		} catch (Exception ignored) {}
	}
}