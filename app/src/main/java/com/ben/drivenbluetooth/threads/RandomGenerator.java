package com.ben.drivenbluetooth.threads;

import com.ben.drivenbluetooth.Global;

import org.acra.ACRA;

public class RandomGenerator extends Thread {
	private volatile boolean stopWorker = false;

    private final int   NUM_STEPS   = 50;
    private final float THRT_STEP   = 100f  / NUM_STEPS;
    private final float AMPS_STEP   = 30f   / NUM_STEPS;
    private final float VOLTS_STEP  = 26.5f / NUM_STEPS;
    private final float TEMP1_STEP  = 50f   / NUM_STEPS;
    private final float RPM_STEP    = 3000  / NUM_STEPS;
    private final float SPEED_STEP  = 20f   / NUM_STEPS;
    private final float THRA_STEP   = 100f  / NUM_STEPS;

    private int counter = 0;

	public void run() {
		this.stopWorker = false;

		while(!this.stopWorker){
			byte[] Message = new byte[5];

            float[] vals = new float[7];
            vals[0] = THRT_STEP  * counter;
            vals[1] = AMPS_STEP  * counter;
            vals[2] = VOLTS_STEP * counter;
            vals[3] = TEMP1_STEP * counter;
            vals[4] = RPM_STEP   * counter;
            vals[5] = SPEED_STEP * counter;
            vals[6] = THRA_STEP  * counter;

			byte[] IDS = new byte[7];
			IDS[0] = Global.THR_INPUT_ID;
			IDS[1] = Global.AMPS_ID;
			IDS[2] = Global.VOLTS_ID;
			IDS[3] = Global.TEMP1ID;
			IDS[4] = Global.MOTOR_RPM_ID;
			IDS[5] = Global.SPEED_MPH_ID;
			IDS[6] = Global.THR_ACTUAL_ID;

			for (int i = 0; i < IDS.length; i++) {
				// fill with random shit
				// rnd.nextBytes(Message);

				if (vals[i] == 0) {
					Message[2] = (byte) 0xff;
					Message[3] = (byte) 0xff;

				} else if (vals[i] <= 127) {

					int integer;
					int decimal;
					float tempDecimal;

					integer = (int) vals[i];
					tempDecimal = (vals[i] - (float) integer) * 100;
					decimal = (int) tempDecimal;

					Message[2] = (byte) integer;
					Message[3] = (byte) decimal;

					if (decimal == 0) {
						Message[3] = (byte) 0xff;
					}

					if (integer == 0) {
						Message[2] = (byte) 0xff;
					}

				} else {
					int tens;
					int hundreds;

					hundreds = (int)(vals[i] / 100);
					tens = (int) (vals[i] - hundreds * 100);

					Message[2] = (byte) hundreds;
					//dataByte1 = dataByte1 || 0x10000000; //flag for integer send value
					Message[2]+= 128;
					Message[3]= (byte) tens;

					if (tens == 0) {
						Message[3] = (byte) 0xff;
					}

					if (hundreds == 0) {
						Message[2] = (byte) 0xff;
					}
				}

				// organise key bytes
				Message[0] = Global.STARTBYTE;
				Message[1] = IDS[i];
				//Message[2] = (byte) (IDS[i] == Global.MOTOR_RPM_ID ? ((int) vals[i] / 100) + 128 : (int) vals[i]); // blegh
				//Message[3] = (byte) ((vals[i] - (int) vals[i]) * 100);
				Message[4] = Global.STOPBYTE;

				// push to queue
				Global.BTStreamQueue.add(Message);

				// send message to BTDataParser
				try {
					BTDataParser.mHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					e.printStackTrace();
					ACRA.getErrorReporter().handleException(e);
				}

				try {
					Thread.sleep(10); // this needs to be here otherwise the queue gets overloaded
				} catch (Exception ignored) {}
			}

            if (++counter > NUM_STEPS) counter = 0;

			// wait 250 milliseconds
			try {
				Thread.sleep(250);
			} catch (Exception ignored) {}
		}
	}

	public void cancel() {
		this.stopWorker = true;
	}
}
