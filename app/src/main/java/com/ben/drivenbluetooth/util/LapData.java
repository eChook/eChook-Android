package com.ben.drivenbluetooth.util;

import java.util.concurrent.TimeUnit;

public class LapData {
	private final RunningAverage AmpsAvg;
	private final RunningAverage VoltsAvg;
	private final RunningAverage RPMAvg;
    private final RunningAverage SpeedMPSAvg;

    private Double DistanceMeters;
    private Double WattHours;
    private Double AmpHours;

    private long lapMillis;

	public LapData() {
		AmpsAvg 	= new RunningAverage(1);
		VoltsAvg 	= new RunningAverage(1);
		RPMAvg 		= new RunningAverage(0);
        SpeedMPSAvg = new RunningAverage(1);
        DistanceMeters = 0.0;
        WattHours = 0.0;
        AmpHours	= 0.0;
	}

	public void AddAmps(Double amps) {
		AmpsAvg.add(amps);
	}

	public void AddVolts(Double volts) {
		VoltsAvg.add(volts);
	}

	public void AddRPM(Double rpm) {
		RPMAvg.add(rpm);
	}

	public void AddSpeedMPS(Double speedMPS) {
        SpeedMPSAvg.add(speedMPS);
    }

	public void AddAmpHours(Double ah) { AmpHours += ah; }

    public void AddWattHours(Double wh) {
        WattHours += wh;
    }

    public void AddDistanceMeters(Double meters) {
        DistanceMeters += meters;
    }

	public Double getAverageAmps() {
		return AmpsAvg.getAverage();
	}

	public Double getAverageVolts() {
		return VoltsAvg.getAverage();
	}

	public Double getAverageRPM() {
		return RPMAvg.getAverage();
	}

	public Double getAverageSpeedMPH() {
        return SpeedMPSAvg.getAverage() * 2.2;
    }

	public Double getAverageSpeedKPH() {
        return SpeedMPSAvg.getAverage() * 3.6;
    }

    public Double getAverageSpeedMPS() {
        return SpeedMPSAvg.getAverage();
    }

	public Double getAmpHours() { return AmpHours; }

    public Double getWattHoursPerKM() {
        return WattHours / DistanceMeters / 1000;
    }

    public Double getDistanceMeters() { return DistanceMeters; }

    public Double getDistanceKM() { return DistanceMeters / 1000; }

    public long getLapTimeMillis() {
        return lapMillis;
    }

    public long getLapTimeSeconds() { return lapMillis / 1000; }

    public String getLapTimeString() {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(lapMillis),
                TimeUnit.MILLISECONDS.toSeconds(lapMillis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public void setLapTime(long millis) {
        lapMillis = millis;
    }
}
