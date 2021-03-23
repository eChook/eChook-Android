package com.ben.drivenbluetooth.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.events.ArduinoEvent;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.ben.drivenbluetooth.util.Bezier;
import com.ben.drivenbluetooth.util.LocationMonitor;
import com.ben.drivenbluetooth.util.RaceObserver;
import com.ben.drivenbluetooth.util.UnitHelper;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.ActivityCompat;

public class RaceMapFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private static Timer FragmentUpdateTimer;
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
    private Polyline ObserverToSFLLine;
    private Polyline ObserverToCarLine;
    private Circle ObserverLocation;

    private LocationMonitor myLocationMonitor;

    /*===================*/
	/* RACEMAPFRAGMENT
	/*===================*/
    public RaceMapFragment() {
        // Required empty public constructor
    }

    public void initialize(LocationMonitor dl) {
        myLocationMonitor = dl;
    }

    /*===================*/
	/* INITIALIZERS
	/*===================*/
    private void InitializeDataFields() {
        View v = getView();
        Current = v.findViewById(R.id.current);
        Voltage = v.findViewById(R.id.voltage);
        RPM = v.findViewById(R.id.rpm);
        Speed = v.findViewById(R.id.speed);
        AmpHours = v.findViewById(R.id.ampHours);

        CurBearing = v.findViewById(R.id.txtCurBearing);
        SFLBearing = v.findViewById(R.id.txtSFLBearing);
        Accuracy = v.findViewById(R.id.txtAccuracy);
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
        UpdateFragmentUI();

        EventBus.getDefault().register(this);
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

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {

            InitializeMap(googleMap);

            PolylineOptions ObserverToCarLineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .clickable(false)
                    .visible(true)
                    .width(10f);
            PolylineOptions ObserverToSFLLineOptions = new PolylineOptions()
                    .color(Color.GREEN)
                    .clickable(false)
                    .visible(true)
                    .width(10f);

            ObserverToCarLine = map.addPolyline(ObserverToCarLineOptions);
            ObserverToSFLLine = map.addPolyline(ObserverToSFLLineOptions);

            GetObserver();
            GetStartFinishLineMarker();

        } catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e));
e.printStackTrace();
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
                ObserverLocation = map.addCircle(myLocationMonitor.ObserverLocation);
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
                myLocationMonitor.setMyRaceObserverLocation(loc, RaceObserver.ORIENTATION.CLOCKWISE);
                myLocationMonitor.setMyRaceObserverLocation(loc, RaceObserver.ORIENTATION.CLOCKWISE);
                map.clear();
                ObserverLocation = map.addCircle(myLocationMonitor.ObserverLocation);
            }
        });
        builder.setNegativeButton("Anticlockwise", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                myLocationMonitor.setMyRaceObserverLocation(loc, RaceObserver.ORIENTATION.ANTICLOCKWISE);
                map.clear();
                ObserverLocation = map.addCircle(myLocationMonitor.ObserverLocation);
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
        Voltage.setText(String.format(Locale.ENGLISH,"%.2f", Global.Volts) + " V");
        Current.setText(String.format(Locale.ENGLISH,"%.1f", Global.Amps) + " A");
        AmpHours.setText(String.format(Locale.ENGLISH,"%.2f", Global.AmpHours) + " Ah");
        RPM.setText(String.format(Locale.ENGLISH,"%.0f", Global.MotorRPM) + " RPM");
    }

    private void UpdateMap() {
        if (map != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(Global.Latitude, Global.Longitude))
                    .zoom(16)                   // Sets the zoom
//                    .bearing(Global.Bearing.floatValue())                // Sets the orientation of the camera to east
                    //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (pathHistory != null) {
//                pathHistory.setPoints(myLocationMonitor.pathHistory.getPoints());
            }

            // debugger lines
            if (ObserverLocation != null && myLocationMonitor.getCurrentLocation() != null) {
                ObserverToCarLine.setPoints(new ArrayList<>(
                        Arrays.asList(
                                ObserverLocation.getCenter(),
                                new LatLng(
                                        myLocationMonitor.getCurrentLocation().getLatitude(),
                                        myLocationMonitor.getCurrentLocation().getLongitude()
                                )
                        )
                ));
            }

            if (ObserverLocation != null && Global.StartFinishLineLocation != null) {
                ObserverToSFLLine.setPoints(new ArrayList<>(
                        Arrays.asList(
                                ObserverLocation.getCenter(),
                                new LatLng(
                                        Global.StartFinishLineLocation.getLatitude(),
                                        Global.StartFinishLineLocation.getLongitude()
                                )
                        )
                ));
            }

            try {
                ObserverLocation.setCenter(myLocationMonitor.ObserverLocation.getCenter());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArduinoEvent(ArduinoEvent event) {
        try {
            switch (event.eventType) {

                case Volts:
                    Voltage.setText(String.format(Locale.ENGLISH,"%.2f", Global.Volts) + " V");
                    break;
                case Amps:
                    Current.setText(String.format(Locale.ENGLISH,"%.1f", Global.Amps) + " A");
                    break;
                case AmpHours:
                    AmpHours.setText(String.format(Locale.ENGLISH,"%.2f", Global.AmpHours) + " Ah");
                    break;
                case WheelSpeedMPS:
                    Speed.setText(UnitHelper.getSpeedText(Global.SpeedMPS, Global.SpeedUnit));
                    break;
                case MotorSpeedRPM:
                    RPM.setText(String.format(Locale.ENGLISH,"%.0f", Global.MotorRPM) + " RPM");
                    break;
            }
        } catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e.getMessage()));
        }
    }

    private void UpdateMapText() {
        CurBearing.setText("car: " + String.format(Locale.ENGLISH,"%.1f", myLocationMonitor.GetRaceObserverBearing_Current()));
        SFLBearing.setText("sfl: " + String.format(Locale.ENGLISH,"%.1f", myLocationMonitor.GetRaceObserverBearing_SFL()));
        Accuracy.setText("acc: " + String.format(Locale.ENGLISH,"%.1f", Global.GPSAccuracy));
    }

    private void StartFragmentUpdater() {
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

    private void StopFragmentUpdater() {
        try {
            FragmentUpdateTimer.cancel();
            FragmentUpdateTimer.purge();
        } catch (Exception ignored) {}
    }
}
