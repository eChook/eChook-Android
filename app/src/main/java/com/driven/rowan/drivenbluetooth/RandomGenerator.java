package com.driven.rowan.drivenbluetooth;

import java.util.Random;

/**
 * Created by Ben on 26/03/2015.
 */
public class RandomGenerator extends Thread {
	Random rnd = new Random();
	private volatile boolean stopWorker = false;

	public void run() {
		this.stopWorker = false;
		while(!this.stopWorker){
			byte[] Message = new byte[5];
			// { x y z }

			byte[] IDS = new byte[3];
			IDS[0] = Global.WHEELRPMID;
			IDS[1] = Global.AMPID;
			IDS[2] = Global.VOLTID;

			for (int i = 0; i < IDS.length; i++) {
				// fill with random shit
				rnd.nextBytes(Message);

				// organise key bytes
				Message[0] = Global.STARTBYTE;
				Message[4] = Global.STOPBYTE;
				Message[1] = IDS[i];

				// push to queue
				Global.BTStreamQueue.add(Message);
				try{
					Thread.sleep(10); // this needs to be here otherwise the queue gets overloaded
				} catch (Exception e) {

				}
			}

			// wait 250 milliseconds
			try {
				Thread.sleep(250);
			} catch (Exception e) {
				// ??
			}
		}
	}

	public void cancel() {
		this.stopWorker = true;
	}
}
