//package com.ben.drivenbluetooth.util;
//
////import com.github.mikephil.charting.components.AxisBase;
////import com.github.mikephil.charting.formatter.IAxisValueFormatter;
//
//import java.text.DecimalFormat;
//
//public class CustomLabelFormatter implements IAxisValueFormatter {
//	private final DecimalFormat mFormat;
//	private final String _prefix;
//	private final String _suffix;
//
//	public CustomLabelFormatter(String prefix, String format, String suffix) {
//		mFormat = new DecimalFormat(format);
//		_prefix = prefix;
//		_suffix = suffix;
//	}
//
//	@Override
//	public String getFormattedValue(float value, AxisBase axis) {
//		return _prefix + mFormat.format(value) + _suffix;
//	}
//
//	@Override
//	public int getDecimalDigits() {
//		return 0;
//	}
//}
