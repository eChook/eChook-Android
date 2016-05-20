package com.ben.echookcompanion.util;

import android.support.v4.app.Fragment;

public abstract class UpdateFragment extends Fragment {
	public abstract void UpdateFragmentUI();

	public abstract void UpdateVolts();

	public abstract void UpdateAmps();

	public abstract void UpdateAmpHours();

	public abstract void UpdateThrottle();

	public abstract void UpdateSpeed();

	public abstract void UpdateTemp();

	public abstract void UpdateMotorRPM();

    public abstract void UpdateWattHours();
}
