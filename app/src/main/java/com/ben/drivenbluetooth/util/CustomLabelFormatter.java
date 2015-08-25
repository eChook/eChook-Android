package com.ben.drivenbluetooth.util;

import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;

public class CustomLabelFormatter implements ValueFormatter {
	private DecimalFormat mFormat;
	private String _prefix;
	private String _suffix;

	public CustomLabelFormatter(String prefix, String format, String suffix) {
		mFormat = new DecimalFormat(format);
		_prefix = prefix;
		_suffix = suffix;
	}

	@Override
	public String getFormattedValue(float value) {
		return _prefix + mFormat.format(value) + _suffix;
	}
}
