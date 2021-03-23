package com.ben.drivenbluetooth.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.R;
import com.ben.drivenbluetooth.events.ArduinoEvent;
import com.ben.drivenbluetooth.events.NewLapEvent;
import com.ben.drivenbluetooth.events.SnackbarEvent;
import com.ben.drivenbluetooth.util.ColorHelper;
import com.ben.drivenbluetooth.util.UnitHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;


public class SimpleDataFragment extends Fragment {

	private TextView Amps;
	private TextView SpeedLabel;
	private TextView Volts;
	private TextView RPM;
	private TextView Speed;
	private TextView WattHours;
	private TextView AmpHours;
    private TextView LapVolts;
    private TextView LapAmps;
    private TextView LapRPM;
    private TextView LapSpeed;
    private TextView DiffVolts;
    private TextView DiffAmps;
    private TextView DiffRPM;
    private TextView DiffSpeed;

    // Last Lap and Diff values
	int voltageLl, currentLl, rpmLl, SpeedLl, voltageLlDiff, currentLlDiff, rpmLlDiff, SpeedLlDiff;


    String TAG = "SDF";
	/*===================*/
	/* FOURGRAPHSBARS
	/*===================*/
	public SimpleDataFragment() {
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
        SpeedLabel = v.findViewById(R.id.SpeedLabel);
        LapVolts = v.findViewById(R.id.voltage_ll);
        LapAmps = v.findViewById(R.id.current_ll);
        LapRPM = v.findViewById(R.id.rpm_ll);
        LapSpeed = v.findViewById(R.id.speed_ll);
        DiffVolts = v.findViewById(R.id.voltage_ll_diff);
        DiffAmps = v.findViewById(R.id.current_ll_diff);
        DiffRPM = v.findViewById(R.id.rpm_ll_diff);
        DiffSpeed = v.findViewById(R.id.speed_ll_diff);

	}


	/*===================*/
	/* LIFECYCLE
	/*===================*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
													 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_simple_data, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		InitializeDataFields();
		UpdateFragmentUI();
        SpeedLabel.setText("Speed (" + Global.SpeedUnit + ")");


		EventBus.getDefault().register(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Amps                = null;
		Volts               = null;
		RPM					= null;
		Speed				= null;
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
		UpdateLap();
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
	public void onLocationEvent(NewLapEvent event) {
            UpdateLap();
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
            Volts.setText(String.format(Locale.ENGLISH, "%.1f", Global.Volts));
			Volts.setTextColor(ColorHelper.GetVoltsColor(Global.Volts));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void UpdateAmps() {
		try {
            Amps.setText(String.format(Locale.ENGLISH, "%.1f", Global.Amps));
			Amps.setTextColor(ColorHelper.GetAmpsColor(Global.Amps));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateAmpHours() {
		try {
            AmpHours.setText(String.format(Locale.ENGLISH, "%.2f Ah", Global.AmpHours));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateSpeed() {
		try {
            Speed.setText(String.format(Locale.ENGLISH, "%.1f", UnitHelper.getSpeed(Global.SpeedMPS, Global.SpeedUnit)));
			} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateMotorRPM() {
		try {
            RPM.setText(String.format(Locale.ENGLISH, "%.0f", Global.MotorRPM));
			RPM.setTextColor(ColorHelper.GetRPMColor(Global.MotorRPM));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateWattHours() {
		try {
            WattHours.setText(String.format(Locale.ENGLISH, "%.2f Wh/km", Global.WattHoursPerMeter * 1000));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void UpdateLap() {
		int lapDataIndex = Global.LapDataList.size() -2; // -1 is current lap, -2 is last lap, -3 is lap before last
        if (Global.LapDataList.size() > 2) { //have data to calc diff
            double tempDiff;
			tempDiff = Global.LapDataList.get(lapDataIndex-1).getAverageVolts() - Global.LapDataList.get(lapDataIndex).getAverageVolts();
			DiffVolts.setText(String.format(Locale.ENGLISH, "(%.2f)", tempDiff));

            tempDiff = Global.LapDataList.get(lapDataIndex-1).getAverageAmps() - Global.LapDataList.get(lapDataIndex).getAverageAmps();
            DiffAmps.setText(String.format(Locale.ENGLISH, "(%.2f)", tempDiff));

            tempDiff = Global.LapDataList.get(lapDataIndex-1).getAverageRPM() - Global.LapDataList.get(lapDataIndex).getAverageRPM();
            DiffRPM.setText(String.format(Locale.ENGLISH, "(%.0f)", tempDiff));

            tempDiff = Global.LapDataList.get(lapDataIndex-1).getAverageSpeed() - Global.LapDataList.get(lapDataIndex).getAverageSpeed();
            DiffSpeed.setText(String.format(Locale.ENGLISH, "(%.2f)", tempDiff));

        }

        if (Global.LapDataList.size() > 1) { // have data to fill last lap
            LapVolts.setText(String.format(Locale.ENGLISH, "%.2f", Global.LapDataList.get(lapDataIndex).getAverageVolts()));
            LapAmps.setText(String.format(Locale.ENGLISH, "%.2f", Global.LapDataList.get(lapDataIndex).getAverageAmps()));
            LapRPM.setText(String.format(Locale.ENGLISH, "%.0f", Global.LapDataList.get(lapDataIndex).getAverageRPM()));
            LapSpeed.setText(String.format(Locale.ENGLISH, "%.2f", Global.LapDataList.get(lapDataIndex).getAverageSpeed()));
        }
    }

}
