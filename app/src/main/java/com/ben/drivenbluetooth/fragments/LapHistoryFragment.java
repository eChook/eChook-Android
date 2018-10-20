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
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.events.ArduinoEvent;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.ben.drivenbluetooth.util.UnitHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LapHistoryFragment extends Fragment {

	private TableLayout LapTable;

	private Timer FragmentUpdateTimer;

	private TextView Current;
	private TextView Voltage;
	private TextView RPM;
	private TextView Speed;
	private TextView AmpHours;

	public LapHistoryFragment() {
		// Required empty public constructor
	}

	private void InitializeDataFields() {
		View v = getView();

        assert v != null;
        Current = v.findViewById(R.id.current);
        Voltage = v.findViewById(R.id.voltage);
        RPM = v.findViewById(R.id.rpm);
        Speed = v.findViewById(R.id.speed);
        AmpHours = v.findViewById(R.id.ampHours);

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
        LapTable = Objects.requireNonNull(getView()).findViewById(R.id.laptable);
		InitializeDataFields();
		StartFragmentUpdater();
        EventBus.getDefault().register(this);
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

        EventBus.getDefault().unregister(this);
	}

	private void UpdateLapTable() {
		if (LapTable != null) {
			LapTable.removeAllViews();
			_createHeaders();

			Context ctx = getActivity();
			TableRow.LayoutParams textViewParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			textViewParams.setMargins(20, 10, 20, 10);

			for (int i = 0; i < Global.LapDataList.size(); i++) {
				String[] values = new String[]{
                        String.format(Locale.ENGLISH, "%d", i + 1),
                        String.format(Locale.ENGLISH, "%.1f", Global.LapDataList.get(i).getAverageAmps()),
                        String.format(Locale.ENGLISH, "%.1f", Global.LapDataList.get(i).getAverageVolts()),
                        String.format(Locale.ENGLISH, "%.1f", Global.SpeedUnit == Global.UNIT.KPH ? Global.LapDataList.get(i).getAverageSpeedKPH() : Global.LapDataList.get(i).getAverageSpeedMPH()),
                        String.format(Locale.ENGLISH, "%.0f", Global.LapDataList.get(i).getAverageRPM()),
                        String.format(Locale.ENGLISH, "%.2f", Global.LapDataList.get(i).getAmpHours()),
                        String.format(Locale.ENGLISH, "%.2f", Global.LapDataList.get(i).getWattHoursPerKM()),
                        Global.LapDataList.get(i).getLapTimeString()
                };

				TableRow tr = new TableRow(ctx);
				tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                for (String value : values) {
                    TextView hv = new TextView(ctx);
                    hv.setText(value);
                    hv.setGravity(Gravity.CENTER);
                    hv.setLayoutParams(textViewParams);
                    tr.addView(hv);
                }
				TextView laptime = new TextView(ctx);
				laptime.setText(Global.LapDataList.get(i).getLapTimeString());
				laptime.setGravity(Gravity.CENTER);
				laptime.setLayoutParams(textViewParams);
				tr.addView(laptime);
				LapTable.addView(tr);
			}
		}
	}

	public void UpdateFragmentUI() {
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
				case WattHours:
					UpdateWattHours();
					break;
			}
		} catch (Exception e) {
			EventBus.getDefault().post(new SnackbarEvent(e));
e.printStackTrace();
		}
	}

    private synchronized void UpdateVolts() {
        this.Voltage.setText(String.format(Locale.ENGLISH, "%.2f V", Global.Volts));
    }

    private synchronized void UpdateAmps() {
        this.Current.setText(String.format(Locale.ENGLISH, "%.1f A", Global.Amps));
    }

    private synchronized void UpdateAmpHours() {
        try {
            AmpHours.setText(String.format(Locale.ENGLISH, "%.2f Ah", Global.AmpHours));
		} catch (Exception e) {
			e.toString();
		}
	}

    private synchronized void UpdateSpeed() {
        try {
            Speed.setText(UnitHelper.getSpeedText(Global.SpeedMPS, Global.SpeedUnit));

		} catch (Exception e) {
			e.getMessage();
		}
	}

    private synchronized void UpdateMotorRPM() {
        try {
            this.RPM.setText(String.format(Locale.ENGLISH, "%.0f RPM", Global.MotorRPM));
		} catch (Exception e) {
			e.getMessage();
		}
	}

    private synchronized void UpdateWattHours() {
        // TODO: implement method
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
                Global.SpeedUnit == Global.UNIT.KPH ? "Avg Spd (kph)" : "Avg Spd (mph)",
                "Avg RPM",
				"Amp hours (Ah)",
                "Avg Wh/km",
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

		FragmentUpdateTimer = new Timer();
		FragmentUpdateTimer.schedule(tableUpdateTask, 0, 2000);
	}

	private void StopFragmentUpdater() {
		try {
			FragmentUpdateTimer.cancel();
			FragmentUpdateTimer.purge();
		} catch (Exception ignored) {}
	}
}
