package com.ben.drivenbluetooth.util;

import java.util.concurrent.TimeUnit;

public class LapData {
	private RunningAverage AmpsAvg;
	private RunningAverage VoltsAvg;
	private RunningAverage RPMAvg;
    private RunningAverage SpeedKPHAvg;
    //private RunningAverage WattHoursAvg;

    private Double DistanceKM;
    private Double WattHours;
    private Double AmpHours;

    private long lapMillis;

	public LapData() {
		AmpsAvg 	= new RunningAverage(1);
		VoltsAvg 	= new RunningAverage(1);
		RPMAvg 		= new RunningAverage(0);
        SpeedKPHAvg = new RunningAverage(1);
        DistanceKM = 0.0;
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

	public void AddSpeed(Double speed) {
        SpeedKPHAvg.add(speed);
    }

	public void AddAmpHours(Double ah) { AmpHours += ah; }

    public void AddWattHours(Double wh) {
        WattHours += wh;
    }

    public void AddDistanceKM(Double km) {
        DistanceKM += km;
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
        return SpeedKPHAvg.getAverage() / 1.61;
    }

	public Double getSpeedKPH() {
        return SpeedKPHAvg.getAverage();
    }

	public Double getAmpHours() { return AmpHours; }

    public Double getWattHoursPerKM() {
        return WattHours / DistanceKM;
    }

    public long getLapTimeMillis() {
        return lapMillis;
    }

    public String getLapTime() {
        long sec1 = TimeUnit.MILLISECONDS.toSeconds(lapMillis);
        long sec2 = TimeUnit.MINUTES.toSeconds(lapMillis);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(lapMillis),
                TimeUnit.MILLISECONDS.toSeconds(lapMillis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public void setLapTime(long millis) {
        lapMillis = millis;
    }
}
