package com.ben.echookcompanion.util;

public class RunningAverage {
	private long count;
	private Double average;
	private final int acc;

	public RunningAverage(int accuracy) {
		count = 0;
		average = 0.0;
		acc = accuracy;
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

	public Double getAverage() {
		return average;
	}

	@Override
	public String toString() {
		return String.format("%." + acc + "f", average);
	}
}
