package com.ben.echookcompanion.util;

public class MovingAverage {
	private final int window;
	private Double average;

	public MovingAverage(int windowSize) {
		window = windowSize;
	}

	public Double add(int num) {
		average -= average / window;
		return average += num / window;
	}

	public Double add(float num) {
		average -= average / window;
		return average += num / window;
	}

	public Double add(long num) {
		average -= average / window;
		return average += num / window;
	}

	public Double add(Double num) {
		average -= average / window;
		return average += num / window;
	}

	public double get() {
		return average;
	}
}
