package com.ben.drivenbluetooth.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.ben.drivenbluetooth.util.ColorHelper;
import com.ben.drivenbluetooth.util.CustomLabelFormatter;
import com.ben.drivenbluetooth.util.DataBar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.utils.LargeValueFormatter;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.Timer;
import java.util.TimerTask;


public class FourGraphsBars extends Fragment {

	private static TextView Current;
	private static TextView Voltage;
	private static TextView RPM;
	private static TextView Speed;

	private static DataBar CurrentBar;
	private static DataBar VoltageBar;
	private static DataBar RPMBar;
	private static DataBar SpeedBar;

	private static LineChart myVoltsGraph;
	private static LineChart myAmpsGraph;
	private static LineChart myMotorRPMGraph;
	private static LineChart mySpeedGraph;

	private static TextView AmpHours;

	private static Timer 		FragmentUpdateTimer;

	/*===================*/
	/* FOURGRAPHSBARS
	/*===================*/
	public FourGraphsBars() {
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
	}

	private void InitializeGraphs() {
		View v = getView();
		myVoltsGraph 	= (LineChart) v.findViewById(R.id.voltsGraph);
		myAmpsGraph 	= (LineChart) v.findViewById(R.id.ampsGraph);
		myMotorRPMGraph = (LineChart) v.findViewById(R.id.RPMGraph);
		mySpeedGraph 	= (LineChart) v.findViewById(R.id.SpeedGraph);

		LineChart graphs[] = new LineChart[] {
				myVoltsGraph,
				myAmpsGraph,
				myMotorRPMGraph,
				mySpeedGraph
		};

		LineData data[] = new LineData[] {
				Global.VoltsHistory,
				Global.AmpsHistory,
				Global.MotorRPMHistory,
				Global.SpeedHistory
		};

		ValueFormatter labelFormats[] = new ValueFormatter[] {
				new CustomLabelFormatter("", "0", "V"),
				new CustomLabelFormatter("", "0", "A"),
				new LargeValueFormatter(),
				new CustomLabelFormatter("", "0", "")
		};

		float minMax[][] = new float[][] {
				new float[] {0, 26},	// volts
				new float[] {0, 50},	// amps
				new float[] {0, 2000},	// motor rpm
				new float[] {0, Global.Unit == Global.UNIT.MPH ? 50 : 70}	// speed
		};

		for (int i = 0; i < graphs.length; i++) {
			graphs[i].setData(data[i]);
			graphs[i].setDescription("");
			graphs[i].setVisibleXRangeMaximum(Global.maxGraphDataPoints);
			graphs[i].setNoDataText("");

			YAxis leftAxis = graphs[i].getAxisLeft();
			leftAxis.setAxisMinValue(minMax[i][0]);
			leftAxis.setAxisMaxValue(minMax[i][1]);
			leftAxis.setLabelCount(3, true);
			leftAxis.setValueFormatter(labelFormats[i]);

			YAxis rightAxis = graphs[i].getAxisRight();
			rightAxis.setEnabled(false);

			Legend l = graphs[i].getLegend();
			l.setEnabled(false);

			XAxis bottomAxis = graphs[i].getXAxis();
			bottomAxis.setEnabled(false);
		}
	}

	private void InitializeDataBars() {
		View v = getView();
		CurrentBar 		= (DataBar) v.findViewById(R.id.CurrentBar);
		VoltageBar 		= (DataBar) v.findViewById(R.id.VoltageBar);
		RPMBar 			= (DataBar) v.findViewById(R.id.RPMBar);
		SpeedBar 		= (DataBar) v.findViewById(R.id.SpeedBar);
	}

	/*===================*/
	/* LIFECYCLE
	/*===================*/
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

		AmpHours			= null;
	}

	/*===================*/
	/* FRAGMENT UPDATE
	/*===================*/
	public void UpdateFragmentUI() {
		UpdateVoltage();
		UpdateCurrent();
		UpdateAmpHours();
		UpdateSpeed();
		UpdateMotorRPM();
		UpdateGraphs();
	}

	private void UpdateVoltage() {
		try {
			Voltage.setText(String.format("%.2f", Global.Volts));
			Voltage.setTextColor(ColorHelper.GetVoltsColor(Global.Volts));
			VoltageBar.setValue(Global.Volts);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateCurrent() {
		try {
			Current.setText(String.format("%.1f", Global.Amps));
			Current.setTextColor(ColorHelper.GetAmpsColor(Global.Amps));
			CurrentBar.setValue(Global.Amps);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateAmpHours() {
		try {
			AmpHours.setText(String.format("%.2f", Global.AmpHours) + " Ah");
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
			} else if (Global.Unit == Global.UNIT.KPH) {
				Speed.setText(String.format("%.1f", Global.SpeedKPH) + " kph");
				SpeedBar.setValue(Global.SpeedKPH);
			}

		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateMotorRPM() {
		try {
			RPM.setText(String.format("%.0f", Global.MotorRPM));
			RPM.setTextColor(ColorHelper.GetRPMColor(Global.MotorRPM));
			RPMBar.setValue(Global.MotorRPM);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateGraphs() {
		try {
			myVoltsGraph.notifyDataSetChanged();
			myVoltsGraph.invalidate();
			myVoltsGraph.setVisibleXRangeMaximum(Global.maxGraphDataPoints);
			myVoltsGraph.moveViewToX(myVoltsGraph.getXValCount() - Global.maxGraphDataPoints - 1);

			myAmpsGraph.notifyDataSetChanged();
			myAmpsGraph.invalidate();
			myAmpsGraph.setVisibleXRangeMaximum(Global.maxGraphDataPoints);
			myAmpsGraph.moveViewToX(myAmpsGraph.getXValCount() - Global.maxGraphDataPoints - 1);

			myMotorRPMGraph.notifyDataSetChanged();
			myMotorRPMGraph.invalidate();
			myMotorRPMGraph.setVisibleXRangeMaximum(Global.maxGraphDataPoints);
			myMotorRPMGraph.moveViewToX(myMotorRPMGraph.getXValCount() - Global.maxGraphDataPoints - 1);

			mySpeedGraph.notifyDataSetChanged();
			mySpeedGraph.invalidate();
			mySpeedGraph.setVisibleXRangeMaximum(Global.maxGraphDataPoints);
			mySpeedGraph.moveViewToX(mySpeedGraph.getXValCount() - Global.maxGraphDataPoints - 1);
		} catch (Exception ignored) {}
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
		FragmentUpdateTimer = new Timer();
		FragmentUpdateTimer.schedule(fragmentUpdateTask, 250, Global.FAST_UI_UPDATE_INTERVAL);
	}

	private void StopFragmentUpdater() {
		FragmentUpdateTimer.cancel();
		FragmentUpdateTimer.purge();
	}
}
