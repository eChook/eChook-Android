package com.ben.drivenbluetooth.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;

import java.util.Timer;
import java.util.TimerTask;

public class LapHistoryFragment extends Fragment {

	private TableLayout LapTable;

	private Timer FragmentUpdateTimer;

	private TextView Current;
	private TextView Voltage;
	private TextView RPM;
	private TextView Speed;

	public LapHistoryFragment() {
		// Required empty public constructor
	}

	private void InitializeDataFields() {
		View v = getView();
		Current 		= (TextView) v.findViewById(R.id.current);
		Voltage 		= (TextView) v.findViewById(R.id.voltage);
		RPM 			= (TextView) v.findViewById(R.id.rpm);
		Speed 			= (TextView) v.findViewById(R.id.speed);
	}

	/*===================*/
	/* LIFECYCLE
	/*===================*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_lap_history, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LapTable = (TableLayout) getView().findViewById(R.id.laptable);
		InitializeDataFields();
		StartFragmentUpdater();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		StopFragmentUpdater();
		LapTable = null;

		Current	= null;
		Voltage	= null;
		RPM		= null;
		Speed	= null;
	}

	public void UpdateLapTable() {
		LapTable.removeAllViews();
		_createHeaders();

		Context ctx = getActivity();
		TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		textViewParams.setMargins(20, 10, 20, 10);

		for (int i = 0; i < Global.LapDataList.size(); i++) {
			Double[] values = new Double[]{
					(double) i + 1,
					Global.LapDataList.get(i).getAmps(),
					Global.LapDataList.get(i).getVolts(),
					Global.LapDataList.get(i).getSpeedMPH(),
					Global.LapDataList.get(i).getRPM()
			};

			String[] formats = new String[] {
					"%.0f",
					"%.1f",
					"%.1f",
					"%.1f",
					"%.0f"
			};

			TableRow tr = new TableRow(ctx);
			tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

			for (int j = 0; j < values.length; j++) {
				TextView hv = new TextView(ctx);
				hv.setText(String.format(formats[j], values[j]));
				hv.setGravity(Gravity.CENTER);
				hv.setLayoutParams(textViewParams);
				tr.addView(hv);
			}
			TextView laptime = new TextView(ctx);
			laptime.setText(Global.LapDataList.get(i).lapTime);
			laptime.setGravity(Gravity.CENTER);
			laptime.setLayoutParams(textViewParams);
			tr.addView(laptime);
			LapTable.addView(tr);
		}
	}

	public void UpdateFragmentUI() {
		UpdateVoltage();
		UpdateCurrent();
		UpdateSpeed();
		UpdateMotorRPM();
	}

	private void UpdateVoltage() {
		try {
			this.Voltage.setText(String.format("%.2f", Global.Volts) + " V");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateCurrent() {
		try {
			this.Current.setText(String.format("%.1f", Global.Amps) + " A");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void UpdateSpeed() {
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

	private void UpdateMotorRPM() {
		try {
			this.RPM.setText(String.format("%.0f", Global.MotorRPM) + " RPM");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	private void _createHeaders() {
		Context ctx = getActivity();
		TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
							// left, top, right, bottom
		textViewParams.setMargins(20, 10, 20, 10);

		String[] headers = new String[] {
				"Lap",
				"Avg Amps (A)",
				"Avg Volts (V)",
				"Avg Spd (mph)",
				"Avg RPM",
				"Lap time"
		};

		TableRow tr = new TableRow(ctx);
		tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

		for (String header : headers) {
			TextView hv = new TextView(ctx);
			hv.setText(header);
			hv.setGravity(Gravity.CENTER);
			hv.setLayoutParams(textViewParams);
			hv.setTypeface(null, Typeface.BOLD);
			tr.addView(hv);
		}
		LapTable.addView(tr);
	}

	private void StartFragmentUpdater() {
		TimerTask tableUpdateTask = new TimerTask() {
			public void run() {
				MainActivity.MainActivityHandler.post(new Runnable() {
					public void run() {
						UpdateLapTable();
					}
				});
			}
		};
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
		FragmentUpdateTimer.schedule(tableUpdateTask, 0, 2000);
		FragmentUpdateTimer.schedule(fragmentUpdateTask, 0, Global.FAST_UI_UPDATE_INTERVAL);
	}

	private void StopFragmentUpdater() {
		try {
			FragmentUpdateTimer.cancel();
			FragmentUpdateTimer.purge();
		} catch (Exception ignored) {}
	}
}
