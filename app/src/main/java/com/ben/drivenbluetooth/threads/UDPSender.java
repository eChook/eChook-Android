package com.ben.drivenbluetooth.threads;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.net.InetSocketAddress;

public class UDPSender extends Thread {
	public Handler PacketHandler;
    public Handler ConnectivityChangeHandler;

    private static InetAddress IPAddress;
	private static DatagramSocket mUDPSocket;
    private static int socketCounter = 0;
    private static int sendFailCount = 0;
    private static boolean mUDPSocketOpen = false;

	@Override
	public void run() {
		Looper.prepare();

        PacketHandler = new Handler (new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (mUDPSocketOpen) {
                    byte[] packet = (byte[]) msg.obj;
                    if (!sendUDPPacket(packet)){
                        sendFailCount++;
                    }

                    if (sendFailCount > 10) {
                        sendFailCount = 0;
                        mUDPSocketOpen = OpenUDPSocket();
                    }
                }
                return true;
            }
        });

        ConnectivityChangeHandler = new Handler (new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                // if we receive a message to this handler, the connectivity has changed
                // we must only run this if the socket is not open and we do have a network connection
                if (!mUDPSocketOpen && isNetworkAvailable()) {
                    mUDPSocketOpen = OpenUDPSocket();
                }
                return true;
            }
        });

        int nAttempts = 1;

        while ( !(mUDPSocketOpen = OpenUDPSocket()) && nAttempts < 10) {
            MainActivity.showMessage("Connecting to node.js server [" + Integer.toString(nAttempts) + "]");
            try {
                sleep(500);
            } catch (InterruptedException e) {
                break;
            }
            nAttempts ++;
        }

        if (mUDPSocketOpen) {
            MainActivity.showMessage("Successfully connected to node.js server", Toast.LENGTH_LONG);
        } else {
            MainActivity.showMessage("Could not connect to node.js server", Toast.LENGTH_LONG);
        }

        Looper.loop();
	}

    private boolean OpenUDPSocket() {
        boolean success;
        try {
            IPAddress = InetAddress.getByName(Global.SOCKETADDRESS);
            mUDPSocket = new DatagramSocket(null);
            mUDPSocket.setReuseAddress(true);
            mUDPSocket.bind(new InetSocketAddress(Global.SOCKETPORT));
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            ACRA.getErrorReporter().handleException(e);
        }
        return success;
    }

    private boolean sendUDPPacket(byte[] data) {
        boolean success;
        if (socketCounter > 3) {
            try {
                mUDPSocket.send(new DatagramPacket(data, 5, IPAddress, Global.SOCKETPORT));
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            socketCounter = 0;
        } else {
            success = true;
        }
        socketCounter++;
        return success;
    }

    private boolean sendUDPLocation(Location location) {
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) MainActivity.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
