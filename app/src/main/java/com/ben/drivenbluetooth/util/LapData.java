package com.ben.drivenbluetooth.util;

public class LapData {
	private RunningAverage AmpsAvg;
	private RunningAverage VoltsAvg;
	private RunningAverage RPMAvg;
	private RunningAverage SpeedAvg;
	public String lapTime;

	public LapData() {
		AmpsAvg 	= new RunningAverage(1);
		VoltsAvg 	= new RunningAverage(1);
		RPMAvg 		= new RunningAverage(0);
		SpeedAvg 	= new RunningAverage(1);
		lapTime		= "";
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
}
