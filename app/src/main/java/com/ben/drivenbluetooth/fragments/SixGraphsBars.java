package com.ben.drivenbluetooth.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.events.ArduinoEvent;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.ben.drivenbluetooth.util.ColorHelper;
import com.ben.drivenbluetooth.util.UnitHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class SixGraphsBars extends Fragment {

    private TextView Throttle;
    private TextView Amps;
    private TextView Volts;
    private TextView VoltsB1;
    private TextView VoltsB2;
    private TextView Temp1;
    private TextView Temp2;
    private TextView RPM;
    private TextView Speed;


    private BarChart ThrottleBarChart;
    private BarChart AmpsBarChart;
    private BarChart VoltsBarChart;
    private BarChart TempBarChart;
    private BarChart SpeedBarChart;
    private BarChart RPMBarChart;

    private LineChart ThrottleLineChart;
    private LineChart VoltsLineChart;
    private LineChart AmpsLineChart;
    private LineChart RPMLineChart;
    private LineChart SpeedLineChart;
    private LineChart TempLineChart;

    private TextView AmpHours;
    private TextView WattHours;

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
        Throttle = v.findViewById(R.id.throttle);
        Amps = v.findViewById(R.id.current);
        Volts = v.findViewById(R.id.voltage);
        VoltsB1 = v.findViewById(R.id.VoltB1);
        VoltsB2 = v.findViewById(R.id.VoltB2);
        Temp1 = v.findViewById(R.id.temp1);
        Temp2 = v.findViewById(R.id.temp2);
        RPM = v.findViewById(R.id.rpm);
        Speed = v.findViewById(R.id.speed);
        AmpHours = v.findViewById(R.id.ampHours);
        WattHours = v.findViewById(R.id.wattHours);
//        Fan = v.findViewById(R.id.fan);
    }

//  private void InitializeGraphs() {
//    View v = getView();
//    ThrottleLineChart   = v.findViewById(R.id.ThrottleLineChart);
//    VoltsLineChart      = v.findViewById(R.id.VoltsLineChart);
//    AmpsLineChart       = v.findViewById(R.id.AmpsLineChart);
//    TempLineChart       = v.findViewById(R.id.TempLineChart);
//    RPMLineChart        = v.findViewById(R.id.RPMLineChart);
//    SpeedLineChart      = v.findViewById(R.id.SpeedLineChart);
//
//    ThrottleBarChart   = v.findViewById(R.id.ThrottleBarChart);
//    AmpsBarChart       = v.findViewById(R.id.AmpsBarChart);
//    VoltsBarChart      = v.findViewById(R.id.VoltsBarChart);
//    TempBarChart       = v.findViewById(R.id.TempBarChart);
//    SpeedBarChart      = v.findViewById(R.id.SpeedBarChart);
//    RPMBarChart        = v.findViewById(R.id.RPMBarChart);
//
//    LineChart lineCharts[] = new LineChart[] {
//            ThrottleLineChart,
//            VoltsLineChart,
//            AmpsLineChart,
//            TempLineChart,
//            RPMLineChart,
//            SpeedLineChart
//    };
//
//    LineData lineDatas[] = new LineData[] {
//            Global.ThrottleHistory,
//            Global.VoltsHistory,
//            Global.AmpsHistory,
//            Global.TempC1History,
//            Global.MotorRPMHistory,
//            Global.SpeedHistory
//    };
//
//    BarChart barCharts[] = new BarChart[] {
//            ThrottleBarChart,
//            VoltsBarChart,
//            AmpsBarChart,
//            TempBarChart,
//            RPMBarChart,
//            SpeedBarChart
//    };
//
//    IAxisValueFormatter labelFormats[] = new IAxisValueFormatter[] {
//            new CustomLabelFormatter("", "0", "%"),
//            new CustomLabelFormatter("", "0", "V"),
//            new CustomLabelFormatter("", "0", "A"),
//            new CustomLabelFormatter("", "0", "C"),
//            new LargeValueFormatter(),
//            new CustomLabelFormatter("", "0", "")
//    };
//
//    float minMax[][] = new float[][] {
//            new float[] {0, 100},	// throttle
//            new float[] {0, 30},	// volts
//            new float[] {0, 50},	// amps
//            new float[] {0, 50},	// temp
//            new float[] {0, 2100},	// motor rpm
//            new float[]{0, UnitHelper.getMaxSpeed(Global.SpeedUnit)}    // speed
//    };
//
//    for (int i = 0; i < lineCharts.length; i++) {
//      LineChart chart = lineCharts[i];
//      if (Global.EnableGraphs) {
//        chart.setData(lineDatas[i]);
//        chart.setVisibleXRangeMaximum(Global.MAX_GRAPH_DATA_POINTS);
//        chart.setNoDataText("");
//
//        YAxis leftAxis = chart.getAxisLeft();
//        leftAxis.setAxisMinimum(minMax[i][0]);
//        leftAxis.setAxisMaximum(minMax[i][1]);
//        leftAxis.setLabelCount(3, true);
//        leftAxis.setValueFormatter(labelFormats[i]);
//
//        YAxis rightAxis = chart.getAxisRight();
//        rightAxis.setEnabled(false);
//
//        Legend l = chart.getLegend();
//        l.setEnabled(false);
//
//        XAxis bottomAxis = chart.getXAxis();
//        bottomAxis.setEnabled(false);
//
//        // Remove padding
//        chart.setViewPortOffsets(0f, 0f, 0f, 0f);
//      } else {
//        chart.setNoDataText("");
//      }
//      chart.invalidate();
//    }
//
//    String legend[] = new String[] {
//            "Throttle",
//            "Volts",
//            "Amps",
//            "Temp",
//            "RPM",
//            Global.SpeedUnit == Global.UNIT.KPH ? "kph" : "mph"
//    };
//
//    for (int i = 0; i < barCharts.length; i++) {
//      BarChart chart = barCharts[i];
//
//      if (Global.EnableGraphs) {
//        // Disable right-hand y-axis
//        chart.getAxisRight().setEnabled(false);
//
//        // Disable legend
//        chart.getLegend().setEnabled(false);
//
//        // Set y-axis limits
//        YAxis yAxis = chart.getAxisLeft();
//        yAxis.setAxisMinimum(minMax[i][0]);
//        yAxis.setAxisMaximum(minMax[i][1]);
//
//        // Create data
//        BarEntry entry = new BarEntry(0, 0);
//        BarDataSet dataSet = new BarDataSet(new ArrayList<BarEntry>(), legend[i]);
//        BarData data = new BarData();
//        data.setDrawValues(false);
//
//        // Attach data
//        dataSet.addEntry(entry);
//        data.addDataSet(dataSet);
//        chart.setData(data);
//
//        chart.setHardwareAccelerationEnabled(true);
//      } else {
//        chart.setNoDataText("");
//      }
//
//      // Refresh chart
//      chart.invalidate();
//    }
//  }

    /*===================*/
	/* LIFECYCLE
	/*===================*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            return inflater.inflate(R.layout.fragment_six_graphs_bars, container, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        InitializeDataFields();
//    InitializeGraphs();
        UpdateFragmentUI();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Amps = null;
        Volts = null;
        RPM = null;
        Speed = null;
        Temp1 = null;
        Temp2 = null;
        Throttle = null;

//    ThrottleBarChart	= null;
//    AmpsBarChart    	= null;
//    VoltsBarChart   	= null;
//    TempBarChart    	= null;
//    SpeedBarChart   	= null;
//    RPMBarChart     	= null;
//
//    VoltsLineChart      = null;
//    AmpsLineChart       = null;
//    RPMLineChart        = null;
//    SpeedLineChart      = null;
//    TempLineChart       = null;
//    ThrottleLineChart   = null;
        AmpHours = null;

        EventBus.getDefault().unregister(this);
    }

    /*===================*/
	/* FRAGMENT UPDATE
	/*===================*/
    private void UpdateFragmentUI() {
        UpdateVolts();
        UpdateAmps();
        UpdateThrottle();
        UpdateSpeed();
        UpdateTemp();
        UpdateTemp2();
        UpdateMotorRPM();
        UpdateAmpHours();
        UpdateWattHours();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArduinoEvent(ArduinoEvent event) {
        try {
            switch (event.eventType) {

                case Volts:
                    UpdateVolts();
                    break;
                case Amps:
                    UpdateAmps();
                    break;
                case AmpHours:
                    UpdateAmpHours();
                    break;
                case ThrottleInput:
                    UpdateThrottle();
                    break;
                case ThrottleActual:
                    UpdateThrottle();
                    break;
                case WheelSpeedMPS:
                    UpdateSpeed();
                    break;
                case MotorSpeedRPM:
                    UpdateMotorRPM();
                    break;
                case TemperatureA:
                    UpdateTemp();
                    break;
                case TemperatureB:
                    UpdateTemp2();
                    break;
                case FanDuty:
                    UpdateTemp();
                    break;
                case WattHours:
                    UpdateWattHours();
                    break;
            }
        } catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e));
            e.printStackTrace();
        }
    }

    private void UpdateThrottle() {
        try {
            if (Global.ActualThrottle < Global.InputThrottle) {
                Throttle.setText(String.format("%.0f", Global.InputThrottle) + " (" + String.format("%.0f", Global.ActualThrottle) + ")");
            } else {
                Throttle.setText(String.format("%.0f", Global.InputThrottle));
            }
//      if (Global.EnableGraphs) {
//        UpdateBarChart(ThrottleBarChart, Global.InputThrottle, ColorHelper.getThrottleColor(Global.InputThrottle));
//        UpdateLineChart(ThrottleLineChart);
//      }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void UpdateVolts() {
        try {
            Volts.setText(String.format("%.2f", Global.Volts));
            Volts.setTextColor(ColorHelper.GetVoltsColor(Global.Volts));
            VoltsB1.setText(String.format("%.2f", Global.VoltsAux));
            VoltsB2.setText(String.format("%.2f", (Global.Volts - Global.VoltsAux)));
//      if (Global.EnableGraphs) {
//        UpdateBarChart(VoltsBarChart, Global.Volts, ColorHelper.GetVoltsColor(Global.Volts));
//        UpdateLineChart(VoltsLineChart);
//      }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateAmps() {
        try {
            Amps.setText(String.format("%.1f", Global.Amps));
            Amps.setTextColor(ColorHelper.GetVoltsColor(Global.Amps));
//      if (Global.EnableGraphs) {
//        UpdateBarChart(AmpsBarChart, Global.Amps, ColorHelper.GetAmpsColor(Global.Amps));
//        UpdateLineChart(AmpsLineChart);
//      }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateAmpHours() {
        try {
            AmpHours.setText(String.format("%.2f", Global.AmpHours));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateSpeed() {
        try {
            Double speed = UnitHelper.getSpeed(Global.SpeedMPS, Global.SpeedUnit);
            Speed.setText(String.format("%.1f", speed));

//      if (Global.EnableGraphs) {
//        UpdateBarChart(SpeedBarChart, speed, Color.BLACK);
//        UpdateLineChart(SpeedLineChart);
//      }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateTemp() {
        try {
            Temp1.setText(String.format("%.1f", Global.TempC1));
            Temp1.setTextColor(ColorHelper.GetVoltsColor(Global.TempC1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateTemp2() {
        try {
            Temp2.setText(String.format("%.1f", Global.TempC2));
            Temp2.setTextColor(ColorHelper.GetVoltsColor(Global.TempC2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateMotorRPM() {
        try {
            RPM.setText(String.format("%.0f", Global.MotorRPM));
            RPM.setTextColor(ColorHelper.GetRPMColor(Global.MotorRPM));
//      if (Global.EnableGraphs) {
////        UpdateBarChart(RPMBarChart, Global.MotorRPM, ColorHelper.GetRPMColor(Global.MotorRPM));
////        UpdateLineChart(RPMLineChart);
//      }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateWattHours() {
        WattHours.setText(String.format("%.2f", Global.WattHoursPerMeter * 1000));
    }

//  private void UpdateBarChart(BarChart chart, Double value, int color) {
//    chart.getBarData().getDataSetByIndex(0).getEntryForIndex(0).setY(value.floatValue());
//    DataSet set = (DataSet) chart.getData().getDataSetByIndex(0);
//    set.setColor(color);
//    chart.invalidate();
//  }

//  private void UpdateLineChart(LineChart graph) {
//    graph.notifyDataSetChanged();
//    graph.setVisibleXRangeMaximum(Global.MAX_GRAPH_DATA_POINTS);
//    graph.moveViewToX(graph.getData().getEntryCount() - Global.MAX_GRAPH_DATA_POINTS - 1);
//  }
}
