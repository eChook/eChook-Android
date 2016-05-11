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
import com.ben.drivenbluetooth.util.UpdateFragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.util.ArrayList;


public class FourGraphsBars extends UpdateFragment {

	private static TextView Amps;
	private static TextView Volts;
	private static TextView RPM;
	private static TextView Speed;
    private static TextView WattHours;
    private static TextView AmpHours;

    private static BarChart AmpsBarChart;
    private static BarChart VoltsBarChart;
    private static BarChart SpeedBarChart;
    private static BarChart RPMBarChart;

	private static LineChart myVoltsGraph;
	private static LineChart myAmpsGraph;
	private static LineChart myMotorRPMGraph;
	private static LineChart mySpeedGraph;


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
		Amps = (TextView) v.findViewById(R.id.current);
		Volts = (TextView) v.findViewById(R.id.voltage);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
		AmpHours		= (TextView) v.findViewById(R.id.ampHours);
        WattHours = (TextView) v.findViewById(R.id.wattHours);
    }

	private void InitializeGraphs() {
		View v = getView();
        VoltsBarChart   = (BarChart) v.findViewById(R.id.VoltsBarChart);
        AmpsBarChart    = (BarChart) v.findViewById(R.id.AmpsBarChart);
        RPMBarChart     = (BarChart) v.findViewById(R.id.RPMBarChart);
        SpeedBarChart   = (BarChart) v.findViewById(R.id.SpeedBarChart);

        myVoltsGraph    = (LineChart) v.findViewById(R.id.voltsGraph);
        myAmpsGraph     = (LineChart) v.findViewById(R.id.ampsGraph);
        myMotorRPMGraph = (LineChart) v.findViewById(R.id.RPMGraph);
        mySpeedGraph    = (LineChart) v.findViewById(R.id.SpeedGraph);

        BarChart barCharts[] = new BarChart[] {
                VoltsBarChart,
                AmpsBarChart,
                RPMBarChart,
                SpeedBarChart
        };

		LineChart lineCharts[] = new LineChart[] {
				myVoltsGraph,
				myAmpsGraph,
				myMotorRPMGraph,
				mySpeedGraph
		};

		LineData lineDatas[] = new LineData[] {
				Global.VoltsHistory,
				Global.AmpsHistory,
				Global.MotorRPMHistory,
				Global.SpeedHistory
		};

		YAxisValueFormatter labelFormats[] = new YAxisValueFormatter[] {
				new CustomLabelFormatter("", "0", "V"),
				new CustomLabelFormatter("", "0", "A"),
				new LargeValueFormatter(),
				new CustomLabelFormatter("", "0", "")
		};

		float minMax[][] = new float[][] {
				new float[] {0, 30},	// volts
				new float[] {0, 50},	// amps
				new float[] {0, 2000},	// motor rpm
				new float[] {0, Global.Unit == Global.UNIT.MPH ? 50 : 70}	// speed
		};

		for (int i = 0; i < lineCharts.length; i++) {
			lineCharts[i].setData(lineDatas[i]);
			lineCharts[i].setDescription("");
			lineCharts[i].setVisibleXRangeMaximum(Global.maxGraphDataPoints);
			lineCharts[i].setNoDataText("");
			lineCharts[i].setNoDataTextDescription("");

			YAxis leftAxis = lineCharts[i].getAxisLeft();
			leftAxis.setAxisMinValue(minMax[i][0]);
			leftAxis.setAxisMaxValue(minMax[i][1]);
			leftAxis.setLabelCount(3, true);
			leftAxis.setValueFormatter(labelFormats[i]);

			YAxis rightAxis = lineCharts[i].getAxisRight();
			rightAxis.setEnabled(false);

			Legend l = lineCharts[i].getLegend();
			l.setEnabled(false);

			XAxis bottomAxis = lineCharts[i].getXAxis();
			bottomAxis.setEnabled(false);
		}

        String legend[] = new String[] {
                "Volts",
                "Amps",
                "RPM",
                Global.Unit == Global.UNIT.KPH ? "kph" : "mph"
        };

        for (int i = 0; i < barCharts.length; i++) {
            BarChart chart = barCharts[i];
            // Disable right-hand y-axis
            chart.getAxisLeft().setEnabled(false);

            // Disable legend
            chart.getLegend().setEnabled(false);

            // Set y-axis limits
            YAxis yAxis = chart.getAxisLeft();
            yAxis.setAxisMinValue(minMax[i][0]);
            yAxis.setAxisMaxValue(minMax[i][1]);

            // Create data
            BarEntry entry = new BarEntry(0,0);
            BarDataSet dataSet = new BarDataSet(new ArrayList<BarEntry>(), legend[i]);
            BarData data = new BarData();

            // Attach data
            dataSet.addEntry(entry);
            data.addDataSet(dataSet);
            chart.setData(data);

            // Refresh chart
            chart.invalidate();
        }
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
		InitializeDataFields();

		if (Global.EnableGraphs) {
			InitializeGraphs();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Amps                = null;
		Volts               = null;
		RPM					= null;
		Speed				= null;

		AmpsBarChart		= null;
		VoltsBarChart		= null;
		SpeedBarChart	    = null;
		RPMBarChart 	    = null;

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
		UpdateVolts();
		UpdateAmps();
		UpdateAmpHours();
		UpdateSpeed();
		UpdateMotorRPM();
	}

    @Override
    public synchronized void UpdateVolts() {
        try {
			Volts.setText(String.format("%.2f", Global.Volts));
			Volts.setTextColor(ColorHelper.GetVoltsColor(Global.Volts));
			VoltageBar.setValue(Global.Volts);
            VoltageBar.setBarColor(ColorHelper.GetVoltsColor(Global.Volts));

			if (Global.EnableGraphs) UpdateLineChart(myVoltsGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateAmps() {
        try {
			Amps.setText(String.format("%.1f", Global.Amps));
			Amps.setTextColor(ColorHelper.GetAmpsColor(Global.Amps));
            if (Global.EnableGraphs) UpdateBarChart(AmpsBarChart, Global.Amps, ColorHelper.GetAmpsColor(Global.Amps));
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

			if (Global.EnableGraphs) UpdateLineChart(mySpeedGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateMotorRPM() {
        try {
			RPM.setText(String.format("%.0f", Global.MotorRPM));
			RPM.setTextColor(ColorHelper.GetRPMColor(Global.MotorRPM));
			RPMBar.setValue(Global.MotorRPM);
            RPMBar.setBarColor(ColorHelper.GetRPMColor(Global.MotorRPM));

			if (Global.EnableGraphs) UpdateLineChart(myMotorRPMGraph);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public synchronized void UpdateWattHours() {
        WattHours.setText(String.format("%.2f Wh/km", Global.WattHoursPerKM));
    }

    private void UpdateBarChart(BarChart chart, Double value, int color) {
        chart.getBarData().getDataSetByIndex(0).getEntryForIndex(0).setVal(value.floatValue());
        DataSet set = (DataSet) chart.getData().getDataSetByIndex(0);
        set.setColor(color);
        chart.animateY(100);
    }

    private void UpdateLineChart(LineChart graph) {
        graph.notifyDataSetChanged();
        graph.setVisibleXRangeMaximum(Global.maxGraphDataPoints);
        graph.moveViewToX(graph.getXValCount() - Global.maxGraphDataPoints - 1);
    }

	@Deprecated
	public void UpdateThrottle() {}

	@Deprecated
	public void UpdateTemp(int ignored) {}
}
