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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class AllDataFragment extends Fragment {

    private TextView Throttle;
    private TextView Amps;
    private TextView Volts;
    private TextView VoltsB1;
    private TextView VoltsB2;
    private TextView Temp1;
    private TextView Temp2;
    private TextView RPM;
    private TextView Speed;
    private TextView AmpHours;
    private TextView WattHours;

    /*===================*/
	/* SIXGRAPHSBARS
	/*===================*/
    public AllDataFragment() {
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
    }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateAmps() {
        try {
            Amps.setText(String.format("%.1f", Global.Amps));
            Amps.setTextColor(ColorHelper.GetVoltsColor(Global.Amps));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateWattHours() {
        WattHours.setText(String.format("%.2f", Global.WattHoursPerMeter * 1000));
    }

}
