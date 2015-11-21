package com.ben.drivenbluetooth.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.net.DatagramPacket;

public class UDPSender extends Thread {
	public Handler mHandler;

	@Override
	public void run() {
		Looper.prepare();

		mHandler = new Handler (new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				DatagramPacket packet = (DatagramPacket) msg.obj;

				return true;
			}
		});
	}
}
