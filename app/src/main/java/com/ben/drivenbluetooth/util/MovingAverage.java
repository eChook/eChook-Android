package com.ben.drivenbluetooth.util;

/**
 * Created by BNAGY4 on 02/07/2015.
 */
public class MovingAverage {
	private int window;
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
