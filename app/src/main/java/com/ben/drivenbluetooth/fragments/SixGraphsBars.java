package com.ben.drivenbluetooth;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SixGraphsBars.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SixGraphsBars#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SixGraphsBars extends Fragment {

	private static TextView Throttle;
	private static TextView Current;
	private static TextView Voltage;
	private static TextView Temp1;
	private static TextView RPM;
	private static TextView Speed;

	private static DataBar ThrottleBar;
	private static DataBar CurrentBar;
	private static DataBar VoltageBar;
	private static DataBar T1Bar;
	private static DataBar RPMBar;
	private static DataBar SpeedBar;

	private OnFragmentInteractionListener mListener;

	public static SixGraphsBars newInstance() {
		SixGraphsBars fragment = new SixGraphsBars();
		return fragment;
	}

	public SixGraphsBars() {
		// Required empty public constructor
	}

	private void InitializeDataFields(View v) {
		Throttle 		= (TextView) v.findViewById(R.id.throttle);
		Current 		= (TextView) v.findViewById(R.id.current);
		Voltage 		= (TextView) v.findViewById(R.id.voltage);
		Temp1 			= (TextView) v.findViewById(R.id.temp1);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
	}

	private void InitializeGraphs(View v) {
		GraphView myThrottleGraph = (GraphView) v.findViewById(R.id.throttleGraph);
		myThrottleGraph.addSeries(Global.ThrottleHistory);
		myThrottleGraph.getViewport().setYAxisBoundsManual(true);
		myThrottleGraph.getViewport().setMinY(0.0);
		myThrottleGraph.getViewport().setMaxY(100.0);
		myThrottleGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		GraphView myVoltsGraph = (GraphView) v.findViewById(R.id.voltsGraph);
		//myVoltsGraph.addSeries(Global.VoltsHistory);
		myVoltsGraph.getViewport().setYAxisBoundsManual(true);
		myVoltsGraph.getViewport().setMinY(0.0);
		myVoltsGraph.getViewport().setMaxY(28.0);
		myVoltsGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		GraphView myAmpsGraph = (GraphView) v.findViewById(R.id.ampsGraph);
		//myAmpsGraph.addSeries(Global.AmpsHistory);
		myAmpsGraph.getViewport().setYAxisBoundsManual(true);
		myAmpsGraph.getViewport().setMinY(0.0);
		myAmpsGraph.getViewport().setMaxY(40.0);
		myAmpsGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		GraphView myTempC1Graph = (GraphView) v.findViewById(R.id.T1Graph);
		//myTempC1Graph.addSeries(Global.TempC1History);
		myTempC1Graph.getViewport().setYAxisBoundsManual(true);
		myTempC1Graph.getViewport().setMinY(0.0);
		myTempC1Graph.getViewport().setMaxY(100.0);
		myTempC1Graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		GraphView myMotorRPMGraph = (GraphView) v.findViewById(R.id.RPMGraph);
		//myMotorRPMGraph.addSeries(Global.MotorRPMHistory);
		myMotorRPMGraph.getViewport().setYAxisBoundsManual(true);
		myMotorRPMGraph.getViewport().setMinY(0.0);
		myMotorRPMGraph.getViewport().setMaxY(2500);
		myMotorRPMGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

		GraphView mySpeedGraph = (GraphView) v.findViewById(R.id.SpeedGraph);
		//mySpeedGraph.addSeries(Global.SpeedHistory);
		mySpeedGraph.getViewport().setYAxisBoundsManual(true);
		mySpeedGraph.getViewport().setMinY(0.0);
		if (Global.Unit == Global.UNIT.MPH) { mySpeedGraph.getViewport().setMaxY(50.0); }
		if (Global.Unit == Global.UNIT.KPH) { mySpeedGraph.getViewport().setMaxY(80.0); }
		mySpeedGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
	}

	private void InitializeDataBars(View v) {

		ThrottleBar 	= (DataBar) v.findViewById(R.id.ThrottleBar);
		CurrentBar 		= (DataBar) v.findViewById(R.id.CurrentBar);
		VoltageBar 		= (DataBar) v.findViewById(R.id.VoltageBar);
		T1Bar 			= (DataBar) v.findViewById(R.id.T1Bar);
		RPMBar 			= (DataBar) v.findViewById(R.id.RPMBar);
		SpeedBar 		= (DataBar) v.findViewById(R.id.SpeedBar);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_six_graphs_bars, container, false);

		InitializeDataBars(v);
		InitializeDataFields(v);
		InitializeGraphs(v);

		return v;
	}

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
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public void UpdateFragmentUI() {
		UpdateVoltage();
		UpdateCurrent();
		UpdateThrottle();
		UpdateSpeed();
		UpdateTemp(1);
		UpdateMotorRPM();
	}

	private void UpdateVoltage() {
		try {
			Voltage.setText(String.format("%.2f", Global.Volts));
			VoltageBar.setValue(Global.Volts);
			Global.VoltsHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Volts), true, Global.maxGraphDataPoints);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateCurrent() {
		try {
			Current.setText(String.format("%.1f", Global.Amps));
			CurrentBar.setValue(Global.Amps);
			Global.AmpsHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Amps), true, Global.maxGraphDataPoints);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateThrottle() {
		try {
			Throttle.setText(String.format("%.0f", Global.Throttle));
			ThrottleBar.setValue(Global.Throttle);
			Global.ThrottleHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.Throttle), true, Global.maxGraphDataPoints);
		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateSpeed() {
		try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
				Speed.setText(String.format("%.1f", Global.SpeedMPH) + " mph");
				SpeedBar.setValue(Global.SpeedMPH);
				Global.SpeedHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.SpeedMPH), true, Global.maxGraphDataPoints);
			} else if (Global.Unit == Global.UNIT.KPH) {
				Speed.setText(String.format("%.1f", Global.SpeedKPH) + " kph");
				SpeedBar.setValue(Global.SpeedKPH);
				Global.SpeedHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.SpeedKPH), true, Global.maxGraphDataPoints);
			}

		} catch (Exception e) {
			e.toString();
		}
	}

	private void UpdateTemp(int sensorIndex) {
		Double TempValue;
		TextView TempText;
		DataBar TempBar;
		switch (sensorIndex) {
			case 1:
				TempValue = Global.TempC1;
				TempText = Temp1;
				TempBar = T1Bar;
				break;

			default:
				TempValue = null;
				TempText = null;
				TempBar = null;
				break;
		}

		if (TempValue != null && TempText != null && TempBar != null) {
			try {
				TempText.setText(String.format("%.1f", TempValue) + " C");
				TempBar.setValue(TempValue);
				Global.TempC1History.appendData(new DataPoint(Global.GraphTimeStamp, TempValue), true, Global.maxGraphDataPoints);
			} catch (Exception e) {
				e.toString();
			}
		}
	}

	private void UpdateMotorRPM() {
		try {
			RPM.setText(String.format("%.0f", Global.MotorRPM));
			RPMBar.setValue(Global.MotorRPM);
			Global.MotorRPMHistory.appendData(new DataPoint(Global.GraphTimeStamp, Global.MotorRPM), true, Global.maxGraphDataPoints);
		} catch (Exception e) {
			e.toString();
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
		public void onFragmentInteraction(Uri uri);
	}
}
