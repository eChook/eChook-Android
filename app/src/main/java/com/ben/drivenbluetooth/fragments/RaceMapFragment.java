package com.ben.drivenbluetooth.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.ben.drivenbluetooth.util.Bezier;
import com.ben.drivenbluetooth.util.RaceObserver;
import com.ben.drivenbluetooth.util.UpdateFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RaceMapFragment extends UpdateFragment
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
	private TextView AmpHours;

	private TextView CurBearing;
	private TextView SFLBearing;
	private TextView Accuracy;

	private Polyline pathHistory;
	private Circle ObserverLocation;

	private static Timer FragmentUpdateTimer;

	/*===================*/
	/* RACEMAPFRAGMENT
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
		AmpHours		= (TextView) v.findViewById(R.id.ampHours);

		CurBearing		= (TextView) v.findViewById(R.id.txtCurBearing);
		SFLBearing		= (TextView) v.findViewById(R.id.txtSFLBearing);
		Accuracy		= (TextView) v.findViewById(R.id.txtAccuracy);
	}

	private void InitializeMap(GoogleMap googleMap) {
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
		AmpHours	= null;

		CurBearing 	= null;
		SFLBearing 	= null;
		map 		= null;
		mFragment	= null;
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		try {

			InitializeMap(googleMap);

			pathHistory = map.addPolyline(MainActivity.myDrivenLocation.pathHistory);

			GetObserver();
			GetStartFinishLineMarker();

		} catch (Exception e) {
			MainActivity.showError(e);
		}
	}

	/*===================*/
	/* MAIN FUNCTIONS
	/*===================*/

	private void GetStartFinishLineMarker() {
		if (Global.StartFinishLineLocation != null) {
			map.addCircle(new CircleOptions()
					.center(new LatLng(Global.StartFinishLineLocation.getLatitude(), Global.StartFinishLineLocation.getLongitude()))
					.radius(5)
					.fillColor(Color.WHITE));
		}
	}

	private void GetObserver() {
		if (ObserverLocation != null) {
			try {
				ObserverLocation = map.addCircle(MainActivity.myDrivenLocation.ObserverLocation);
			} catch (NullPointerException ignored) {}
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
		// User confirmed the observer location; we must now ask the track orientation
		final Location loc = new Location("");
		loc.setLatitude(marker.getPosition().latitude);
		loc.setLongitude(marker.getPosition().longitude);

		// Custom alert to decide track orientation
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
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/*===================*/
	/* PATH SMOOTHING - NOT USED
	/*===================*/

	private void DemoSmoothedLine() {
		PolylineOptions UK = new PolylineOptions();
		UK.add(new LatLng(51.507351, -0.127758)); 	// Charing Cross, London
		UK.add(new LatLng(52.486243, -1.890401));	// Aston University, Birmingham
		UK.add(new LatLng(54.978252, -1.61778));		// Haymarket, Newcastle upon Tyne
		UK.add(new LatLng(55.953252, -3.188267));	// Edinburgh Waverley, Edinburgh
		UK.add(new LatLng(55.953252, -4.188267));
		UK.add(new LatLng(50.953252, -5.188267));
		UK.add(new LatLng(48.953252, 3.188267));

		PolylineOptions someshit = new PolylineOptions();
		someshit.add(new LatLng(51.507351, -0.127758)); 	// Charing Cross, London
		someshit.add(new LatLng(52.486243, -1.890401));	// Aston University, Birmingham
		someshit.add(new LatLng(54.978252, -1.61778));	// Haymarket, Newcastle upon Tyne
		someshit.add(new LatLng(55.953252, -3.188267));	// Edinburgh Waverley, Edinburgh
		someshit.color(Color.RED);

		PolylineOptions someshit2 = new PolylineOptions();
		someshit2.add(new LatLng(51.507351, -0.127758)); 	// Charing Cross, London
		someshit2.add(new LatLng(52.486243, -1.890401));	// Aston University, Birmingham
		someshit2.add(new LatLng(54.978252, -1.61778));	// Haymarket, Newcastle upon Tyne
		someshit2.add(new LatLng(55.953252, -3.188267));
		someshit2.add(new LatLng(55.953252, -4.188267));
		someshit2.add(new LatLng(50.953252, -5.188267));
		someshit2.add(new LatLng(48.953252, 3.188267));// Edinburgh Waverley, Edinburgh
		someshit2.color(Color.BLUE);

		PolylineOptions someshit3 = new PolylineOptions();
		someshit3.add(new LatLng(51.507351, -0.127758)); 	// Charing Cross, London
		someshit3.add(new LatLng(52.486243, -1.890401));	// Aston University, Birmingham
		someshit3.add(new LatLng(54.978252, -1.61778));	// Haymarket, Newcastle upon Tyne
		someshit3.add(new LatLng(55.953252, -3.188267));	// Edinburgh Waverley, Edinburgh
		someshit3.color(Color.GREEN);

		Polyline herp = map.addPolyline(UK);
		Polyline smoothedherp = map.addPolyline(someshit);
		smoothedherp.setPoints(SmoothPath(UK));

		Polyline smoothedherp2 = map.addPolyline(someshit2);
		smoothedherp2.setPoints(SmoothPath(UK, 0.5f));

		//Polyline smoothedherp3 = map.addPolyline(someshit3);
		//smoothedherp3.setPoints(SmoothPath(UK, 1f));
	}

	private List<LatLng> SmoothPath(PolylineOptions PLO, float scale) {
		List<LatLng> listLatLng = PLO.getPoints();
		Path path = Bezier.GetBezierPath(listLatLng, scale);

		PathMeasure pm = new PathMeasure(path, false);
		float length = pm.getLength();
		List<LatLng> smoothedLine = new ArrayList<>();
		if (length > 1e-4) {
			for (double f = 0.0; f <= length; f += (double) length / (double) (4 * listLatLng.size())) {
				float[] latlng = new float[2];
				pm.getPosTan((float) f, latlng, null);
				smoothedLine.add(new LatLng(latlng[0], latlng[1]));
			}
			// sometimes the last point is not added, so force it
			float[] latlng = new float[2];
			pm.getPosTan(1, latlng, null);
			smoothedLine.add(new LatLng(latlng[0], latlng[1]));
		}
		return smoothedLine;
	}

	private List<LatLng> SmoothPath(PolylineOptions PLO) {
		return SmoothPath(PLO, 1f);
	}

	/*===================*/
	/* FRAGMENT UPDATING
	/*===================*/
	public void UpdateFragmentUI() {
		UpdateAmps();
		UpdateAmpHours();
		UpdateMotorRPM();
		UpdateVolts();
		UpdateSpeed();
	}

	public void UpdateMap() {
		if (map != null) {
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
			} catch (NullPointerException ignored) {
			}            // Observerlocation has not been initialized yet so do nothing

			if (Global.StartFinishLineLocation != null) {
				map.addCircle(new CircleOptions()
						.center(new LatLng(Global.StartFinishLineLocation.getLatitude(), Global.StartFinishLineLocation.getLongitude()))
						.radius(5)
						.fillColor(Color.WHITE));
			}
			UpdateMapText();
		}
	}

	public void UpdateVolts() {
		try {
			this.Voltage.setText(String.format("%.2f", Global.Volts) + " V");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public void UpdateAmps() {
		try {
			this.Current.setText(String.format("%.1f", Global.Amps) + " A");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public void UpdateAmpHours() {
		try {
			AmpHours.setText(String.format("%.2f", Global.AmpHours) + " Ah");
		} catch (Exception e) {
			e.toString();
		}
	}

	public void UpdateSpeed() {
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

	public void UpdateMotorRPM() {
		try {
			this.RPM.setText(String.format("%.0f", Global.MotorRPM) + " RPM");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public void UpdateMapText() {
		CurBearing.setText("car: " + String.format("%.1f", MainActivity.myDrivenLocation.GetRaceObserverBearing_Current()));
		SFLBearing.setText("sfl: " + String.format("%.1f", MainActivity.myDrivenLocation.GetRaceObserverBearing_SFL()));
		Accuracy.setText("acc: " + String.format("%.1f", Global.GPSAccuracy));
	}

	public void StartFragmentUpdater() {
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
	}

	public void StopFragmentUpdater() {
		try {
			FragmentUpdateTimer.cancel();
			FragmentUpdateTimer.purge();
		} catch (Exception ignored) {}
	}

	@Override
	public void UpdateTemp(int ignored) {}	// required as per UpdateFragment contract

	@Override
	public void UpdateThrottle() {}	// required as per UpdateFragment contract
}
