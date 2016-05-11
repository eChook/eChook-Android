package com.ben.drivenbluetooth.util;

import android.content.Context;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;
import com.ben.drivenbluetooth.drivenbluetooth.R;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

/**
 * Created by BNAGY4 on 15/09/2015.
 */
public final class GraphData {

	private static void GraphData() {
		// empty constructor
	}

    public static void InitializeGraphDataSets() {
        Context context = MainActivity.getAppContext();
        LineData dataSets[] = new LineData[] {
                Global.ThrottleHistory,
                Global.VoltsHistory,
                Global.AmpsHistory,
                Global.MotorRPMHistory,
                Global.SpeedHistory,
                Global.TempC1History
        };

        String legends[] = new String[]{
                "Throttle",
                "Volts",
                "Amps",
                "RPM",
                "Speed",
                "Temp"
        };

        int colors[] = new int[] {
                context.getResources().getColor(R.color.throttle),
                context.getResources().getColor(R.color.volts),
                context.getResources().getColor(R.color.amps),
                context.getResources().getColor(R.color.rpm),
                context.getResources().getColor(R.color.speed),
                context.getResources().getColor(R.color.temperature)
        };

        for (int i = 0; i < dataSets.length; i++) {
            LineDataSet set = new LineDataSet(null, legends[i]);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setDrawCircles(false);
            set.setLineWidth(5);
            set.setDrawValues(false);
            set.setColor(colors[i]);
            dataSets[i].addDataSet(set);
        }
    }

	public synchronized static void AddVolts(final double rawVolts) {
		Global.VoltsHistory.addXValue(String.format("%d", Global.VoltsHistory.getXValCount()));
		Global.VoltsHistory.addEntry(new Entry((float) rawVolts, Global.VoltsHistory.getXValCount() - 1), 0);

		if (Global.VoltsHistory.getXValCount() > Global.MAX_GRAPH_DATA_POINTS) {
			LineDataSet set = (LineDataSet) Global.VoltsHistory.getDataSetByIndex(0);
			Global.VoltsHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public synchronized static void AddAmps(final double rawAmps) {
		Global.AmpsHistory.addXValue("0");
		Global.AmpsHistory.addEntry(new Entry((float) rawAmps, Global.AmpsHistory.getXValCount() - 1), 0);

		if (Global.AmpsHistory.getXValCount() > Global.MAX_GRAPH_DATA_POINTS) {
			LineDataSet set = (LineDataSet) Global.AmpsHistory.getDataSetByIndex(0);
			Global.AmpsHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public synchronized static void AddInputThrottle(final double rawThrottle) {
		Global.ThrottleHistory.addXValue("0");
		Global.ThrottleHistory.addEntry(new Entry((float) rawThrottle, Global.ThrottleHistory.getXValCount() - 1), 0);

		if (Global.ThrottleHistory.getXValCount() > Global.MAX_GRAPH_DATA_POINTS) {
			LineDataSet set = (LineDataSet) Global.ThrottleHistory.getDataSetByIndex(0);
			Global.ThrottleHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public synchronized static void AddSpeed(final double rawSpeedMPS) {
        //Global.SpeedHistory.addXValue(String.format("%d", Global.SpeedHistory.getXValCount()));
        Global.SpeedHistory.addXValue("0");
		if (Global.Unit == Global.UNIT.MPH) {
			Global.SpeedHistory.addEntry(new Entry((float) (rawSpeedMPS * 2.2), Global.SpeedHistory.getXValCount() - 1), 0);
		} else if (Global.Unit == Global.UNIT.KPH) {
			Global.SpeedHistory.addEntry(new Entry((float) (rawSpeedMPS * 3.6), Global.SpeedHistory.getXValCount() - 1), 0);
		}

		if (Global.SpeedHistory.getXValCount() > Global.MAX_GRAPH_DATA_POINTS) {
			LineDataSet set = (LineDataSet) Global.SpeedHistory.getDataSetByIndex(0);
			Global.SpeedHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public synchronized static void AddMotorRPM(final double rawMotorRPM) {
		Global.MotorRPMHistory.addXValue("0");
		Global.MotorRPMHistory.addEntry(new Entry((float) rawMotorRPM, Global.MotorRPMHistory.getXValCount() - 1), 0);

		if (Global.MotorRPMHistory.getXValCount() > Global.MAX_GRAPH_DATA_POINTS) {
			LineDataSet set = (LineDataSet) Global.MotorRPMHistory.getDataSetByIndex(0);
			Global.MotorRPMHistory.getXVals().remove(0);
			set.removeEntry(0);

			for (Entry e : set.getYVals()) {
				e.setXIndex(e.getXIndex() - 1);
			}
		}
	}

	public synchronized static void AddTemperature(double rawTemp, int sensorId) {
		switch (sensorId) {
			case 1:
				Global.TempC1History.addXValue("0");
				Global.TempC1History.addEntry(new Entry((float) rawTemp, Global.TempC1History.getXValCount() - 1), 0);

				if (Global.TempC1History.getXValCount() > Global.MAX_GRAPH_DATA_POINTS) {
					LineDataSet set = (LineDataSet) Global.TempC1History.getDataSetByIndex(0);
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
