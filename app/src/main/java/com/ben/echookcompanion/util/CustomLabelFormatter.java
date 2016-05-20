package com.ben.echookcompanion.util;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;

public class CustomLabelFormatter implements YAxisValueFormatter {
	private final DecimalFormat mFormat;
	private final String _prefix;
	private final String _suffix;

	public CustomLabelFormatter(String prefix, String format, String suffix) {
		mFormat = new DecimalFormat(format);
		_prefix = prefix;
		_suffix = suffix;
	}

	@Override
	public String getFormattedValue(float value, YAxis yAxis) {
		return _prefix + mFormat.format(value) + _suffix;
	}
}
