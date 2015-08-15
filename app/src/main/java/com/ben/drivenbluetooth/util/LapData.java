package com.ben.drivenbluetooth.util;

public class LapData {
	private RunningAverage AmpsAvg;
	private RunningAverage VoltsAvg;
	private RunningAverage RPMAvg;
	private RunningAverage SpeedAvg;

	private Double AmpHours;

	public String lapTime;

	public LapData() {
		AmpsAvg 	= new RunningAverage(1);
		VoltsAvg 	= new RunningAverage(1);
		RPMAvg 		= new RunningAverage(0);
		SpeedAvg 	= new RunningAverage(1);
		AmpHours	= 0.0;
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

	public void AddAmpHours(Double ah) { AmpHours += ah; }

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
}
