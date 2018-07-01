package com.ben.drivenbluetooth.util;

import android.content.Context;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.R;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

/**
 * Created by BNAGY4 on 15/09/2015.
 */
public final class GraphData {

	private GraphData() {
		// empty constructor
	}

    public static void InitializeGraphDataSets(Context context) {
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

	public static void AddToHistory(double rawVal, LineData lineData) {
		lineData.addEntry(new Entry((float) rawVal, lineData.getEntryCount()), 0);

		if (lineData.getEntryCount() > Global.MAX_GRAPH_DATA_POINTS) {
			LineDataSet set = (LineDataSet) lineData.getDataSetByIndex(0);
			set.removeFirst();
		}
	}
}
