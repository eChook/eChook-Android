package com.ben.drivenbluetooth.util;

import com.ben.drivenbluetooth.Global;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

/**
 * Created by BNAGY4 on 15/09/2015.
 */
public final class GraphData {

	private static void GraphData() {
		// empty constructor
	}

	public static void AddVolts(final double rawVolts) {
		Global.VoltsHistory.addXValue(String.format("%d", Global.VoltsHistory.getXValCount()));
		Global.VoltsHistory.addEntry(new Entry((float) rawVolts, Global.VoltsHistory.getXValCount() - 1), 0);

		if (Global.VoltsHistory.getXValCount() > Global.maxGraphDataPoints) {
			LineDataSet set = Global.VoltsHistory.getDataSetByIndex(0);
			Global.VoltsHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public static void AddAmps(final double rawAmps) {
		Global.AmpsHistory.addXValue("0");
		Global.AmpsHistory.addEntry(new Entry((float) rawAmps, Global.AmpsHistory.getXValCount() - 1), 0);

		if (Global.AmpsHistory.getXValCount() > Global.maxGraphDataPoints) {
			LineDataSet set = Global.AmpsHistory.getDataSetByIndex(0);
			Global.AmpsHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public static void AddInputThrottle(final double rawThrottle) {
		/* WIP new method

		int curIndex = -1;


		if (Global.ThrottleHistory.getXValCount() == 0) {
			Global.ThrottleHistory.addXValue("0");
		} else {
			curIndex = Integer.parseInt(Global.ThrottleHistory.getXVals().get(Global.ThrottleHistory.getXValCount() - 1));
			Global.ThrottleHistory.addXValue(Integer.toString(curIndex + 1));
		}

		Global.ThrottleHistory.addEntry(new Entry((float) rawThrottle, curIndex + 1), 0);

		if (Global.ThrottleHistory.getXValCount() > Global.maxGraphDataPoints) {
			Global.ThrottleHistory.getDataSetByIndex(0).removeEntry(0);
			Global.ThrottleHistory.getXVals().remove(0);
		}
		*/

		Global.ThrottleHistory.addXValue("0");
		Global.ThrottleHistory.addEntry(new Entry((float) rawThrottle, Global.ThrottleHistory.getXValCount() - 1), 0);

		if (Global.ThrottleHistory.getXValCount() > Global.maxGraphDataPoints) {
			LineDataSet set = Global.ThrottleHistory.getDataSetByIndex(0);
			Global.ThrottleHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public static void AddSpeed(final double rawSpeedMPH) {
		if (Global.Unit == Global.UNIT.MPH) {
			Global.SpeedHistory.addXValue("0");
			Global.SpeedHistory.addEntry(new Entry((float) rawSpeedMPH, Global.SpeedHistory.getXValCount() - 1), 0);
		} else if (Global.Unit == Global.UNIT.KPH) {
			Global.SpeedHistory.addXValue(String.format("%d", Global.SpeedHistory.getXValCount()));
			Global.SpeedHistory.addEntry(new Entry(Global.SpeedKPH.floatValue(), Global.SpeedHistory.getXValCount() - 1), 0);
		}

		if (Global.SpeedHistory.getXValCount() > Global.maxGraphDataPoints) {
			LineDataSet set = Global.SpeedHistory.getDataSetByIndex(0);
			Global.SpeedHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public static void AddMotorRPM(final double rawMotorRPM) {
		Global.MotorRPMHistory.addXValue("0");
		Global.MotorRPMHistory.addEntry(new Entry((float) rawMotorRPM, Global.MotorRPMHistory.getXValCount() - 1), 0);

		if (Global.MotorRPMHistory.getXValCount() > Global.maxGraphDataPoints) {
			LineDataSet set = Global.MotorRPMHistory.getDataSetByIndex(0);
			Global.MotorRPMHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public static void AddTemperature(double rawTemp, int sensorId) {
		switch (sensorId) {
			case 1:
				Global.TempC1History.addXValue("0");
				Global.TempC1History.addEntry(new Entry((float) rawTemp, Global.TempC1History.getXValCount() - 1), 0);

				if (Global.TempC1History.getXValCount() > Global.maxGraphDataPoints) {
					LineDataSet set = Global.TempC1History.getDataSetByIndex(0);
					Global.TempC1History.getXVals().remove(0);
					set.removeEntry(0);

					for (Entry e : set.getYVals()) {
						e.setXIndex(e.getXIndex() - 1);
					}
				}
				break;
			case 2:
				Global.TempC2 = rawTemp;
				break;
			case 3:
				Global.TempC3 = rawTemp;
				break;
			default:
				break;
		}
	}
}
