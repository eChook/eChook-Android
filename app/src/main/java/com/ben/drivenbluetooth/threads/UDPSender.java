package com.ben.drivenbluetooth.threads;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import org.acra.ACRA;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPSender extends Thread {
	public Handler mHandler;

    private static InetAddress IPAddress;
	private static DatagramSocket mUDPSocket;
	private static boolean mUDPSocketValid = false;
	private static int socketCounter = 0;

	@Override
	public void run() {
		Looper.prepare();

        OpenUDPSocket();

		mHandler = new Handler (new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				byte[] packet = (byte[]) msg.obj;
                sendUDPPacket(packet);
				return true;
			}
		});
        Looper.loop();
	}

    private void OpenUDPSocket() {
        try {
            MainActivity.showMessage("Connecting to node.js server...");
            IPAddress = InetAddress.getByName(Global.SOCKETADDRESS);
            mUDPSocket = new DatagramSocket(Global.SOCKETPORT);
            mUDPSocketValid = true;
            MainActivity.showMessage("Successfully connected node.js server");
        } catch (Exception e) {
            MainActivity.showMessage("Could not connect to node.js server", Toast.LENGTH_LONG);
            e.printStackTrace();
            mUDPSocketValid = false;
        }
    }

    private boolean sendUDPPacket(byte[] data) {
        boolean success = false;
        if (socketCounter > 3) {
            try {
                mUDPSocket.send(new DatagramPacket(data, 5, IPAddress, Global.SOCKETPORT));
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            socketCounter = 0;
        }
        socketCounter++;
        return success;
    }
}
