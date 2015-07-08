package com.ben.drivenbluetooth.util;

import android.graphics.Color;

public final class ColorHelper {
	private static final int AMPS_LOW 		= 10;
	private static final int AMPS_ECO 		= 25;
	private static final int AMPS_HIGH 		= 30;

	private static final int VOLTAGE_UPPER 	= 24;
	private static final int VOLTAGE_LOWER 	= 15;

	private static final int RPM_LOW 		= 1400;
	private static final int RPM_ECO 		= 1800;
	private static final int RPM_HIGH 		= 3000;

	private static final float GOOD 		= 120; 	// 120 deg of Hue = Green
	private static final float BAD 			= 0; 	// 0 deg of Hue = Red
	private static final float VALUE 		= 0.7f; // slightly dark
	private static final float SATURATION	= 0.5f; // somewhat washed-out

	public static int GetAmpsColor(Double amps) {
		float hue;
		if (amps >= AMPS_HIGH || amps <= AMPS_LOW) {
			hue = BAD;
		} else if (amps == AMPS_ECO) {
			hue = GOOD;
		} else if (amps < AMPS_ECO) {
			hue = (amps.floatValue() - AMPS_LOW) / (AMPS_ECO - AMPS_LOW) * (GOOD - BAD) + BAD;
		} else {
			hue = (amps.floatValue() - AMPS_ECO) / (AMPS_HIGH - AMPS_ECO) * (GOOD - BAD) + BAD;
		}
		return Color.HSVToColor(new float[]{hue, SATURATION, VALUE});
	}

	public static int GetVoltsColor(Double volts) {
		float hue;
		if (volts >= VOLTAGE_UPPER) {
			hue = BAD;
		} else if (volts <= VOLTAGE_LOWER) {
			hue = GOOD;
		} else {
			hue = (volts.floatValue() - VOLTAGE_LOWER) / (VOLTAGE_UPPER - VOLTAGE_LOWER) * (GOOD - BAD) + BAD;
		}
		return Color.HSVToColor(new float[]{hue, SATURATION, VALUE});
	}

	public static int GetRPMColor(Double rpm) {
		float hue;
		if (rpm >= RPM_HIGH || rpm <= RPM_LOW) {
			hue = BAD;
		} else if (rpm == RPM_ECO) {
			hue = GOOD;
		} else if (rpm < RPM_ECO) {
			hue = (rpm.floatValue() - RPM_LOW) / (RPM_ECO - RPM_LOW) * (GOOD - BAD) + BAD;
		} else {
			hue = (rpm.floatValue() - RPM_ECO) / (RPM_HIGH - RPM_ECO) * (GOOD - BAD) + BAD;
		}
		return Color.HSVToColor(new float[]{hue, SATURATION, VALUE});
	}
}
