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
		byte i0 = 0;
		byte i1 = 0;

		while(!this.stopWorker){
			byte[] Message = new byte[5];

			byte[] IDS = new byte[6];
			IDS[0] = Global.THROTTLEID;
			IDS[1] = Global.AMPID;
			IDS[2] = Global.VOLTID;
			IDS[3] = Global.TEMP1ID;
			IDS[4] = Global.MOTORRPMID;
			IDS[5] = Global.SPEEDMPHID;

			for (int i = 0; i < IDS.length; i++) {
				// fill with random shit
				rnd.nextBytes(Message);

				// organise key bytes
				Message[0] = Global.STARTBYTE;
				Message[1] = IDS[i];
				//Message[2] = i0;
				//Message[3] = i1;
				Message[4] = Global.STOPBYTE;

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

			i0 += 10;
			i1 += 50;

			if (i0 > 100) i0 = 0;
			if (i1 > 50) i1 = 0;
		}
	}

	public void cancel() {
		this.stopWorker = true;
	}
}
