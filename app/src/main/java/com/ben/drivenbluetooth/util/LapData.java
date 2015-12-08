package com.ben.drivenbluetooth.util;

import java.util.concurrent.TimeUnit;

public class LapData {
	private RunningAverage AmpsAvg;
	private RunningAverage VoltsAvg;
	private RunningAverage RPMAvg;
	private RunningAverage SpeedAvg;

	private Double AmpHours;

    private long lapMillis;

	public LapData() {
		AmpsAvg 	= new RunningAverage(1);
		VoltsAvg 	= new RunningAverage(1);
		RPMAvg 		= new RunningAverage(0);
		SpeedAvg 	= new RunningAverage(1);
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

	public void AddSpeed(Double speed) {
		SpeedAvg.add(speed);
	}

	public void AddAmpHours(Double ah) { AmpHours += ah; }

    public void setLapTime(long millis) {
        lapMillis = millis;
    }

	public Double getAmps() {
		return AmpsAvg.getAverage();
	}

	public Double getVolts() {
		return VoltsAvg.getAverage();
	}

	public Double getRPM() {
		return RPMAvg.getAverage();
	}

	public Double getSpeedMPH() {
		return SpeedAvg.getAverage();
	}

	public Double getSpeedKPH() {
		return SpeedAvg.getAverage() * 1.61;
	}

	public Double getAmpHours() { return AmpHours; }

    public long getLapTimeMillis() {
        return lapMillis;
    }

    public String getLapTime() {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(lapMillis),
                TimeUnit.MILLISECONDS.toSeconds(lapMillis) - TimeUnit.MINUTES.toSeconds(lapMillis));
    }
}
