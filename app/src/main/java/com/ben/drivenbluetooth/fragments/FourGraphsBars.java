package com.ben.drivenbluetooth.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ben.drivenbluetooth.util.DataBar;
import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;

import java.util.Timer;
import java.util.TimerTask;


public class FourGraphsBars extends Fragment {

	private TextView Current;
	private TextView Voltage;
	private TextView RPM;
	private TextView Speed;

	private DataBar CurrentBar;
	private DataBar VoltageBar;
	private DataBar RPMBar;
	private DataBar SpeedBar;

	private GraphView myVoltsGraph;
	private GraphView myAmpsGraph;
	private GraphView myMotorRPMGraph;
	private GraphView mySpeedGraph;

	private OnFragmentInteractionListener mListener;

	private static Timer 		FragmentUpdateTimer;

	public FourGraphsBars() {
		// Required empty public constructor
	}

	private void InitializeDataFields() {
		View v = getView();
		Current 		= (TextView) v.findViewById(R.id.current);
		Voltage 		= (TextView) v.findViewById(R.id.voltage);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
	}

	private void InitializeGraphs() {
		View v = getView();

		myVoltsGraph = (GraphView) v.findViewById(R.id.voltsGraph);
		myVoltsGraph.addSeries(Global.VoltsHistory);
		myVoltsGraph.getViewport().setYAxisBoundsManual(true);
		myVoltsGraph.getViewport().setMinY(0.0);
		myVoltsGraph.getViewport().setMaxY(28.0);
		myVoltsGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		myAmpsGraph = (GraphView) v.findViewById(R.id.ampsGraph);
		myAmpsGraph.addSeries(Global.AmpsHistory);
		myAmpsGraph.getViewport().setYAxisBoundsManual(true);
		myAmpsGraph.getViewport().setMinY(0.0);
		myAmpsGraph.getViewport().setMaxY(40.0);
		myAmpsGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		myMotorRPMGraph = (GraphView) v.findViewById(R.id.RPMGraph);
		myMotorRPMGraph.addSeries(Global.MotorRPMHistory);
		myMotorRPMGraph.getViewport().setYAxisBoundsManual(true);
		myMotorRPMGraph.getViewport().setMinY(0.0);
		myMotorRPMGraph.getViewport().setMaxY(2500);
		myMotorRPMGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		mySpeedGraph = (GraphView) v.findViewById(R.id.SpeedGraph);
		mySpeedGraph.addSeries(Global.SpeedHistory);
		mySpeedGraph.getViewport().setYAxisBoundsManual(true);
		mySpeedGraph.getViewport().setMinY(0.0);
		if (Global.Unit == Global.UNIT.MPH) { mySpeedGraph.getViewport().setMaxY(50.0); }
		if (Global.Unit == Global.UNIT.KPH) { mySpeedGraph.getViewport().setMaxY(80.0); }
		mySpeedGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
	}

	private void InitializeDataBars() {
		View v = getView();
		CurrentBar 		= (DataBar) v.findViewById(R.id.CurrentBar);
		VoltageBar 		= (DataBar) v.findViewById(R.id.VoltageBar);
		RPMBar 			= (DataBar) v.findViewById(R.id.RPMBar);
		SpeedBar 		= (DataBar) v.findViewById(R.id.SpeedBar);
	}

	/** LIFECYCLE **/

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_four_graphs_bars, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		InitializeDataBars();
		InitializeDataFields();
		InitializeGraphs();
		StartFragmentUpdater();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		StopFragmentUpdater();
		Current				= null;
		Voltage				= null;
		RPM					= null;
		Speed				= null;

		CurrentBar			= null;
		VoltageBar			= null;
		RPMBar				= null;
		SpeedBar			= null;

		myVoltsGraph		= null;
		myAmpsGraph			= null;
		myMotorRPMGraph		= null;
		mySpeedGraph		= null;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public void UpdateFragmentUI() {
		UpdateVoltage();
		UpdateCurrent();
		UpdateSpeed();
		UpdateMotorRPM();
		Global.GraphTimeStamp += (float) Global.UI_UPDATE_INTERVAL / 1000.0f;
	}

	private void UpdateVoltage() {
		try {
			this.Voltage.setText(String.format("%.2f", Global.Volts));
			this.VoltageBar.setValue(Global.Volts);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateCurrent() {
		try {
			this.Current.setText(String.format("%.1f", Global.Amps));
			this.CurrentBar.setValue(Global.Amps);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateSpeed() {
		try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
				this.Speed.setText(String.format("%.1f", Global.SpeedMPH) + " mph");
				this.SpeedBar.setValue(Global.SpeedMPH);
			} else if (Global.Unit == Global.UNIT.KPH) {
				this.Speed.setText(String.format("%.1f", Global.SpeedKPH) + " kph");
				this.SpeedBar.setValue(Global.SpeedKPH);
			}

		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateMotorRPM() {
		try {
			this.RPM.setText(String.format("%.0f", Global.MotorRPM));
			this.RPMBar.setValue(Global.MotorRPM);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	/** FRAGMENT UI UPDATERS **/

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
		FragmentUpdateTimer = new Timer();
		FragmentUpdateTimer.schedule(fragmentUpdateTask, 250, Global.UI_UPDATE_INTERVAL);
	}

	private void StopFragmentUpdater() {
		FragmentUpdateTimer.cancel();
		FragmentUpdateTimer.purge();
	}
}
