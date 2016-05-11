package com.ben.drivenbluetooth.fragments;

import android.graphics.Color;
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


public class SixGraphsBars extends UpdateFragment {

	private static TextView Throttle;
	private static TextView Amps;
	private static TextView Volts;
	private static TextView Temp1;
	private static TextView RPM;
	private static TextView Speed;

    private static BarChart ThrottleBarChart;
    private static BarChart AmpsBarChart;
    private static BarChart VoltsBarChart;
    private static BarChart TempBarChart;
    private static BarChart SpeedBarChart;
    private static BarChart RPMBarChart;

	private static LineChart ThrottleLineChart;
	private static LineChart VoltsLineChart;
	private static LineChart AmpsLineChart;
	private static LineChart RPMLineChart;
	private static LineChart SpeedLineChart;
	private static LineChart TempLineChart;

	private static TextView AmpHours;
    private static TextView WattHours;

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
		Amps = (TextView) v.findViewById(R.id.current);
		Volts = (TextView) v.findViewById(R.id.voltage);
		Temp1 			= (TextView) v.findViewById(R.id.temp1);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
		AmpHours		= (TextView) v.findViewById(R.id.ampHours);
        WattHours = (TextView) v.findViewById(R.id.wattHours);
	}

	private void InitializeGraphs() {
		View v = getView();
		ThrottleLineChart   = (LineChart) v.findViewById(R.id.ThrottleLineChart);
		VoltsLineChart      = (LineChart) v.findViewById(R.id.VoltsLineChart);
		AmpsLineChart       = (LineChart) v.findViewById(R.id.AmpsLineChart);
		TempLineChart       = (LineChart) v.findViewById(R.id.TempLineChart);
		RPMLineChart        = (LineChart) v.findViewById(R.id.RPMLineChart);
		SpeedLineChart      = (LineChart) v.findViewById(R.id.SpeedLineChart);

        ThrottleBarChart   = (BarChart) v.findViewById(R.id.ThrottleBarChart);
        AmpsBarChart       = (BarChart) v.findViewById(R.id.AmpsBarChart);
        VoltsBarChart      = (BarChart) v.findViewById(R.id.VoltsBarChart);
        TempBarChart       = (BarChart) v.findViewById(R.id.TempBarChart);
        SpeedBarChart      = (BarChart) v.findViewById(R.id.SpeedBarChart);
        RPMBarChart        = (BarChart) v.findViewById(R.id.RPMBarChart);

		LineChart lineCharts[] = new LineChart[] {
                ThrottleLineChart,
                VoltsLineChart,
                AmpsLineChart,
                TempLineChart,
                RPMLineChart,
                SpeedLineChart
		};

		LineData lineDatas[] = new LineData[] {
				Global.ThrottleHistory,
				Global.VoltsHistory,
				Global.AmpsHistory,
				Global.TempC1History,
				Global.MotorRPMHistory,
				Global.SpeedHistory
		};

        BarChart barCharts[] = new BarChart[] {
                ThrottleBarChart,
                VoltsBarChart,
                AmpsBarChart,
                TempBarChart,
                RPMBarChart,
                SpeedBarChart
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
				new float[] {0, 2100},	// motor rpm
				new float[] {0, Global.Unit == Global.UNIT.MPH ? 50 : 70}	// speed
		};

        for (int i = 0; i < lineCharts.length; i++) {
            LineChart chart = lineCharts[i];
            chart.setData(lineDatas[i]);
            chart.setDescription("");
            chart.setVisibleXRangeMaximum(Global.MAX_GRAPH_DATA_POINTS);
            chart.setNoDataText("");
            chart.setNoDataTextDescription("");

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setAxisMinValue(minMax[i][0]);
            leftAxis.setAxisMaxValue(minMax[i][1]);
            leftAxis.setLabelCount(3, true);
            leftAxis.setValueFormatter(labelFormats[i]);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);

            Legend l = chart.getLegend();
            l.setEnabled(false);

            XAxis bottomAxis = chart.getXAxis();
            bottomAxis.setEnabled(false);

            // Remove padding
            chart.setViewPortOffsets(0f, 0f, 0f, 0f);
        }

        String legend[] = new String[] {
                "Throttle",
                "Volts",
                "Amps",
                "Temp",
                "RPM",
                Global.Unit == Global.UNIT.KPH ? "kph" : "mph"
        };

        for (int i = 0; i < barCharts.length; i++) {
            BarChart chart = barCharts[i];
            // Disable right-hand y-axis
            chart.getAxisRight().setEnabled(false);

            // Disable legend
            chart.getLegend().setEnabled(false);

            // Disable description
            chart.setDescription("");

            // Set y-axis limits
            YAxis yAxis = chart.getAxisLeft();
            yAxis.setAxisMinValue(minMax[i][0]);
            yAxis.setAxisMaxValue(minMax[i][1]);

            // Create data
            BarEntry entry = new BarEntry(0,0);
            BarDataSet dataSet = new BarDataSet(new ArrayList<BarEntry>(), legend[i]);
            BarData data = new BarData();
            data.setDrawValues(false);

            // Attach data
            dataSet.addEntry(entry);
            data.addDataSet(dataSet);
            chart.setData(data);

            chart.setHardwareAccelerationEnabled(true);

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
		return inflater.inflate(R.layout.fragment_six_graphs_bars, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		InitializeDataFields();
		if (Global.EnableGraphs) {
			InitializeGraphs();
		}
		//StartFragmentUpdater();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Amps = null;
		Volts = null;
		RPM					= null;
		Speed				= null;
		Temp1				= null;
		Throttle			= null;

		ThrottleBarChart	= null;
		AmpsBarChart    	= null;
		VoltsBarChart   	= null;
		TempBarChart    	= null;
		SpeedBarChart   	= null;
		RPMBarChart     	= null;

		VoltsLineChart = null;
		AmpsLineChart = null;
		RPMLineChart = null;
		SpeedLineChart = null;
		TempLineChart = null;
		ThrottleLineChart = null;
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
    public synchronized void UpdateThrottle() {
        try {
            if (Global.ActualThrottle < Global.InputThrottle) {
                Throttle.setText(String.format("%.0f", Global.InputThrottle) + " (" + String.format("%.0f", Global.ActualThrottle) + ")");
            } else {
                Throttle.setText(String.format("%.0f", Global.InputThrottle));
            }
            if (Global.EnableGraphs) {
                UpdateBarChart(ThrottleBarChart, Global.InputThrottle, ColorHelper.getThrottleColor(Global.InputThrottle));
                UpdateLineChart(ThrottleLineChart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void UpdateVolts() {
        try {
            Volts.setText(String.format("%.2f", Global.Volts));
            Volts.setTextColor(ColorHelper.GetVoltsColor(Global.Volts));
            if (Global.EnableGraphs) {
                UpdateBarChart(VoltsBarChart, Global.Volts, ColorHelper.GetVoltsColor(Global.Volts));
                UpdateLineChart(VoltsLineChart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void UpdateAmps() {
        try {
            Amps.setText(String.format("%.1f", Global.Amps));
            Amps.setTextColor(ColorHelper.GetVoltsColor(Global.Amps));
            if (Global.EnableGraphs) {
                UpdateBarChart(AmpsBarChart, Global.Amps, ColorHelper.GetAmpsColor(Global.Amps));
                UpdateLineChart(AmpsLineChart);
            }
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
            Double speed = 0d;
            String speedText = "";
            if (Global.Unit == Global.UNIT.MPH) {
                speed = Global.SpeedMPS * 2.2;
                speedText = String.format("%.1f mph", speed);
            } else if (Global.Unit == Global.UNIT.KPH) {
                speed = Global.SpeedMPS * 3.6;
                speedText = String.format("%.1f kph", speed);
            }

            Speed.setText(speedText);

            if (Global.EnableGraphs) {
                UpdateBarChart(SpeedBarChart, speed, Color.BLACK);
                UpdateLineChart(SpeedLineChart);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void UpdateTemp(int sensorIndex) {
        try {
            Temp1.setText(String.format("%.1f", Global.Amps));
            Temp1.setTextColor(ColorHelper.GetVoltsColor(Global.Amps));
            if (Global.EnableGraphs) {
                UpdateBarChart(TempBarChart, Global.TempC1, ColorHelper.getTempColor(Global.TempC1));
                UpdateLineChart(TempLineChart);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    @Override
    public synchronized void UpdateMotorRPM() {
        try {
            RPM.setText(String.format("%.0f", Global.MotorRPM));
            RPM.setTextColor(ColorHelper.GetRPMColor(Global.MotorRPM));
            if (Global.EnableGraphs) {
                UpdateBarChart(RPMBarChart, Global.MotorRPM, ColorHelper.GetRPMColor(Global.MotorRPM));
                UpdateLineChart(RPMLineChart);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void UpdateWattHours() {
        WattHours.setText(String.format("%.2f Wh/km", Global.WattHoursPerMeter * 1000));
    }

    private void UpdateBarChart(BarChart chart, Double value, int color) {
        chart.getBarData().getDataSetByIndex(0).getEntryForIndex(0).setVal(value.floatValue());
        DataSet set = (DataSet) chart.getData().getDataSetByIndex(0);
        set.setColor(color);
        chart.invalidate();
    }

    private void UpdateLineChart(LineChart graph) {
        graph.notifyDataSetChanged();
        graph.setVisibleXRangeMaximum(Global.MAX_GRAPH_DATA_POINTS);
        graph.moveViewToX(graph.getXValCount() - Global.MAX_GRAPH_DATA_POINTS - 1);
    }
}
