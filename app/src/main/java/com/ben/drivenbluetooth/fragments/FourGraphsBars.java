package com.ben.drivenbluetooth.fragments;

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
import com.ben.drivenbluetooth.util.CustomLabelFormatter;
import com.ben.drivenbluetooth.util.UnitHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

//import com.github.mikephil.charting.charts.BarChart;


public class FourGraphsBars extends Fragment {

	private TextView Amps;
	private TextView Volts;
	private TextView RPM;
	private TextView Speed;
	private TextView WattHours;
	private TextView AmpHours;

	private LineChart VoltsLineChart;
	private LineChart AmpsLineChart;
	private LineChart RPMLineChart;
	private LineChart SpeedLineChart;


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
		Amps 		= v.findViewById(R.id.current);
		Volts 		= v.findViewById(R.id.voltage);
		RPM 		= v.findViewById(R.id.rpm);
		Speed 		= v.findViewById(R.id.speed);
		AmpHours	= v.findViewById(R.id.ampHours);
		WattHours 	= v.findViewById(R.id.wattHours);
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
		UpdateFragmentUI();

		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Amps                = null;
		Volts               = null;
		RPM					= null;
		Speed				= null;

//		AmpsBarChart		= null;
//		VoltsBarChart		= null;
//		SpeedBarChart	    = null;
//		RPMBarChart 	    = null;

		VoltsLineChart      = null;
		AmpsLineChart       = null;
		SpeedLineChart      = null;
		RPMLineChart        = null;

		AmpHours			= null;

		EventBus.getDefault().unregister(this);
	}

	/*===================*/
	/* FRAGMENT UPDATE
	/*===================*/
	private void UpdateFragmentUI() {
		UpdateVolts();
		UpdateAmps();
		UpdateAmpHours();
		UpdateSpeed();
		UpdateMotorRPM();
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
                case WheelSpeedMPS:
                    UpdateSpeed();
                    break;
                case MotorSpeedRPM:
                    UpdateMotorRPM();
                    break;
            }
        } catch (Exception e) {
            EventBus.getDefault().post(new SnackbarEvent(e));
e.printStackTrace();
        }
    }

	private void UpdateVolts() {
		try {
			Volts.setText(String.format("%.2f", Global.Volts));
			Volts.setTextColor(ColorHelper.GetVoltsColor(Global.Volts));
			if (Global.EnableGraphs) {
//				UpdateBarChart(VoltsBarChart, Global.Volts, ColorHelper.GetVoltsColor(Global.Volts));
				UpdateLineChart(VoltsLineChart);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void UpdateAmps() {
		try {
			Amps.setText(String.format("%.1f", Global.Amps));
			Amps.setTextColor(ColorHelper.GetAmpsColor(Global.Amps));
			if (Global.EnableGraphs) {
//				UpdateBarChart(AmpsBarChart, Global.Amps, ColorHelper.GetAmpsColor(Global.Amps));
				UpdateLineChart(AmpsLineChart);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateAmpHours() {
		try {
			AmpHours.setText(String.format("%.2f", Global.AmpHours) + " Ah");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateSpeed() {
		try {
            Double speed = UnitHelper.getSpeed(Global.SpeedMPS, Global.SpeedUnit);
            Speed.setText(UnitHelper.getSpeedText(Global.SpeedMPS, Global.SpeedUnit));

			if (Global.EnableGraphs) {
//				UpdateBarChart(SpeedBarChart, speed, Color.BLACK);
				UpdateLineChart(SpeedLineChart);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateMotorRPM() {
		try {
			RPM.setText(String.format("%.0f", Global.MotorRPM));
			RPM.setTextColor(ColorHelper.GetRPMColor(Global.MotorRPM));
			if (Global.EnableGraphs) {
//				UpdateBarChart(RPMBarChart, Global.MotorRPM, ColorHelper.GetRPMColor(Global.MotorRPM));
				UpdateLineChart(RPMLineChart);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateWattHours() {
		try {
			WattHours.setText(String.format("%.2f Wh/km", Global.WattHoursPerMeter * 1000));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void UpdateBarChart(BarChart chart, Double value, int color) {
//		chart.getBarData().getDataSetByIndex(0).getEntryForIndex(0).setY(value.floatValue());
//		DataSet set = (DataSet) chart.getData().getDataSetByIndex(0);
//		set.setColor(color);
//		chart.invalidate();
//	}

	private void UpdateLineChart(LineChart graph) {
		graph.notifyDataSetChanged();
		graph.setVisibleXRangeMaximum(Global.MAX_GRAPH_DATA_POINTS);
		graph.moveViewToX(graph.getData().getEntryCount() - Global.MAX_GRAPH_DATA_POINTS - 1);
	}
}
