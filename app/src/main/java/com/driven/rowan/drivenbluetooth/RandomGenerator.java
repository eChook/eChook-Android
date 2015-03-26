package com.driven.rowan.drivenbluetooth;

import java.util.Random;

/**
 * Created by BNAGY4 on 26/03/2015.
 */
public class RandomGenerator implements Runnable {
	Random rnd = new Random();
	private volatile boolean stopWorker = false;

	public void run() {
		while(!Thread.currentThread().isInterrupted() && !this.stopWorker){
			byte[] Message = new byte[5];
			// { x y z }

			// fill with random shit
			rnd.nextBytes(Message);

			// organise key bytes
			Message[0] = Global.STARTBYTE;
			Message[4] = Global.STOPBYTE;
			Message[1] = Global.VOLTID;

			// push to queue
			Global.BTStreamQueue.add(Message);

			// wait 250 milliseconds
			try {
				Thread.sleep(250);
			} catch (Exception e) {
				// ??
			}
		}
	}
}
