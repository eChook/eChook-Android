package com.ben.drivenbluetooth.util;

public class RunningAverage {
	private long count;
	private Double average;

	public RunningAverage() {
		count = 0;
		average = 0.0;
	}

	public Double add(int num) {
		return average += (num - average) / ++count;
	}

	public Double add(float num) {
		return average += (num - average) / ++count;
	}

	public Double add(long num) {
		return average += (num - average) / ++count;
	}

	public Double add(Double num) {
		return average += (num - average) / ++count;
	}

	public void reset() {
		count = 0;
		average = 0d;
	}
}
