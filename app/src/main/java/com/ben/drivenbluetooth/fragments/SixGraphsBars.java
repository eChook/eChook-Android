package com.ben.drivenbluetooth.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.ben.drivenbluetooth.util.ColorHelper;
import com.ben.drivenbluetooth.util.CustomLabelFormatter;
import com.ben.drivenbluetooth.util.DataBar;
import com.ben.drivenbluetooth.util.UpdateFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;


public class SixGraphsBars extends UpdateFragment {

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

	private static LineChart myThrottleGraph;
	private static LineChart myVoltsGraph;
	private static LineChart myAmpsGraph;
	private static LineChart myMotorRPMGraph;
	private static LineChart mySpeedGraph;
	private static LineChart myTempC1Graph;

	private static TextView AmpHours;

	/*===================*/
	/* SIXGRAPHSBARS
	/*===================*/
	public SixGraphsBars() {
		// Required empty public constructor
	}

	/*===================*/
	/* INITIALIZERS
	/*===================*/
	private void InitializeDataFields() {
		View v = getView();
		Throttle 		= (TextView) v.findViewById(R.id.throttle);
		Current 		= (TextView) v.findViewById(R.id.current);
		Voltage 		= (TextView) v.findViewById(R.id.voltage);
		Temp1 			= (TextView) v.findViewById(R.id.temp1);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
		AmpHours		= (TextView) v.findViewById(R.id.ampHours);
	}

	private void InitializeGraphs() {
		View v = getView();
		myThrottleGraph = (LineChart) v.findViewById(R.id.throttleGraph);
		myVoltsGraph 	= (LineChart) v.findViewById(R.id.voltsGraph);
		myAmpsGraph 	= (LineChart) v.findViewById(R.id.ampsGraph);
		myTempC1Graph 	= (LineChart) v.findViewById(R.id.T1Graph);
		myMotorRPMGraph = (LineChart) v.findViewById(R.id.RPMGraph);
		mySpeedGraph 	= (LineChart) v.findViewById(R.id.SpeedGraph);

		LineChart graphs[] = new LineChart[] {
				myThrottleGraph,
				myVoltsGraph,
				myAmpsGraph,
				myTempC1Graph,
				myMotorRPMGraph,
				mySpeedGraph
		};

		LineData data[] = new LineData[] {
				Global.ThrottleHistory,
				Global.VoltsHistory,
				Global.AmpsHistory,
				Global.TempC1History,
				Global.MotorRPMHistory,
				Global.SpeedHistory
		};

		YAxisValueFormatter labelFormats[] = new YAxisValueFormatter[] {
				new CustomLabelFormatter("", "0", "%"),
				new CustomLabelFormatter("", "0", "V"),
				new CustomLabelFormatter("", "0", "A"),
				new CustomLabelFormatter("", "0", "C"),
				new LargeValueFormatter(),
				new CustomLabelFormatter("", "0", "")
		};

		float minMax[][] = new float[][] {
				new float[] {0, 100},	// throttle
				new float[] {0, 30},	// volts
				new float[] {0, 50},	// amps
				new float[] {0, 50},	// temp
				new float[] {0, 2000},	// motor rpm
				new float[] {0, Global.Unit == Global.UNIT.MPH ? 50 : 70}	// speed
		};

		for (int i = 0; i < graphs.length; i++) {
			graphs[i].setData(data[i]);
			graphs[i].setDescription("");
			graphs[i].setVisibleXRangeMaximum(Global.maxGraphDataPoints);
			graphs[i].setNoDataText("");
			graphs[i].setNoDataTextDescription("");
			graphs[i].setHardwareAccelerationEnabled(true);

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
		ThrottleBar 	= (DataBar) v.findViewById(R.id.ThrottleBar);
		CurrentBar 		= (DataBar) v.findViewById(R.id.CurrentBar);
		VoltageBar 		= (DataBar) v.findViewById(R.id.VoltageBar);
		T1Bar 			= (DataBar) v.findViewById(R.id.T1Bar);
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
		return inflater.inflate(R.layout.fragment_six_graphs_bars, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		InitializeDataBars();
		InitializeDataFields();
		if (Global.EnableGraphs) {
			InitializeGraphs();
		}
		//StartFragmentUpdater();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Current				= null;
		Voltage				= null;
		RPM					= null;
		Speed				= null;
		Temp1				= null;
		Throttle			= null;

		CurrentBar			= null;
		VoltageBar			= null;
		RPMBar				= null;
		SpeedBar			= null;
		T1Bar				= null;
		ThrottleBar			= null;

		myVoltsGraph		= null;
		myAmpsGraph			= null;
		myMotorRPMGraph		= null;
		mySpeedGraph		= null;
		myTempC1Graph		= null;
		myThrottleGraph		= null;
		AmpHours			= null;
	}

	/*===================*/
	/* FRAGMENT UPDATE
	/*===================*/
	public void UpdateFragmentUI() {
		UpdateVolts();
		UpdateAmps();
		UpdateThrottle();
		UpdateSpeed();
		UpdateTemp(1);
		UpdateMotorRPM();
		UpdateAmpHours();
	}

    @Override
    public synchronized void UpdateVolts() {
        try {
			Voltage.setText(String.format("%.2f", Global.Volts));
			Voltage.setTextColor(ColorHelper.GetVoltsColor(Global.Volts));
			VoltageBar.setValue(Global.Volts);

			if (Global.EnableGraphs) UpdateGraph(myVoltsGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateAmps() {
        try {
			Current.setText(String.format("%.1f", Global.Amps));
			Current.setTextColor(ColorHelper.GetAmpsColor(Global.Amps));
			CurrentBar.setValue(Global.Amps);

			if (Global.EnableGraphs) UpdateGraph(myAmpsGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateAmpHours() {
        try {
			AmpHours.setText(String.format("%.2f", Global.AmpHours) + " Ah");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateThrottle() {
        try {
			if (Global.ActualThrottle < Global.InputThrottle) {
				Throttle.setText(String.format("%.0f", Global.InputThrottle) + " (" + String.format("%.0f", Global.ActualThrottle) + ")");
			} else {
				Throttle.setText(String.format("%.0f", Global.InputThrottle));
			}
			ThrottleBar.setValue(Global.InputThrottle);

			if (Global.EnableGraphs) UpdateGraph(myThrottleGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateSpeed() {
        try {
			// check user preference for speed
			if (Global.Unit == Global.UNIT.MPH) {
                Speed.setText(String.format("%.1f", Global.SpeedKPH / 1.61) + " mph");
                SpeedBar.setValue(Global.SpeedKPH / 1.61);
            } else if (Global.Unit == Global.UNIT.KPH) {
				Speed.setText(String.format("%.1f", Global.SpeedKPH) + " kph");
				SpeedBar.setValue(Global.SpeedKPH);
			}

			if (Global.EnableGraphs) UpdateGraph(mySpeedGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateTemp(int sensorIndex) {
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

				if (Global.EnableGraphs) UpdateGraph(myTempC1Graph);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    @Override
    public synchronized void UpdateMotorRPM() {
        try {
			RPM.setText(String.format("%.0f", Global.MotorRPM));
			RPM.setTextColor(ColorHelper.GetRPMColor(Global.MotorRPM));
			RPMBar.setValue(Global.MotorRPM);

			if (Global.EnableGraphs) UpdateGraph(myMotorRPMGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateWattHours() {
        // TODO: implement method
    }

    private void UpdateGraph(LineChart graph) {
        graph.notifyDataSetChanged();
		graph.setVisibleXRangeMaximum(Global.maxGraphDataPoints);
		graph.moveViewToX(graph.getXValCount() - Global.maxGraphDataPoints - 1);
	}
}
