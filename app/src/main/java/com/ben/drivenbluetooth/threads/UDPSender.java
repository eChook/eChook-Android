package com.ben.drivenbluetooth.threads;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import org.acra.ACRA;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPSender extends Thread {
    private InetAddress IPAddress;
    private DatagramSocket mUDPSocket;
    private int socketCounter = 0;
    private int sendFailCount = 0;
    private boolean mUDPSocketOpen = false;
    private boolean UDPEnabled = false;
    public Handler PacketHandler;
    public Handler ConnectivityChangeHandler;
    public Handler LocationHandler;

    public UDPSender() {
        UDPEnabled = Global.UDPEnabled;
    }

	@Override
	public void run() {
		Looper.prepare();

        PacketHandler = new Handler (new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (UDPEnabled && mUDPSocketOpen) {
                    byte[] packet = (byte[]) msg.obj;
                    if (packet[1] == Global.VOLTS_ID
                            | packet[1] == Global.AMPS_ID
                            | packet[1] == Global.MOTOR_RPM_ID
                            | packet[1] == Global.SPEED_MPH_ID) {
                        if (!sendUDPPacket(packet, packet.length)) sendFailCount++;
                        socketCounter = 0;
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
                if (UDPEnabled && !mUDPSocketOpen && isNetworkAvailable()) {
                    mUDPSocketOpen = OpenUDPSocket();
                }
                return true;
            }
        });

        LocationHandler = new Handler (new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                if (UDPEnabled) {
                    Location loc = (Location) msg.obj;
                    sendUDPLocation(loc);
                }
                return true;
            }
        });

        if (UDPEnabled) {
            int nAttempts = 1;

            while (!(mUDPSocketOpen = OpenUDPSocket()) && nAttempts < 10) {
                MainActivity.showMessage("Connecting to node.js server [" + Integer.toString(nAttempts) + "]");
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                nAttempts++;
            }

            if (mUDPSocketOpen) {
                MainActivity.showMessage("Successfully connected to node.js server", Toast.LENGTH_LONG);
            } else {
                MainActivity.showMessage("Could not connect to node.js server", Toast.LENGTH_LONG);
            }
        }
        Looper.loop();
	}

    public boolean OpenUDPSocket() {
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

    private boolean sendUDPPacket(byte[] data, int length) {
        boolean success;
        try {
            mUDPSocket.send(new DatagramPacket(data, length, IPAddress, Global.SOCKETPORT));
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    private boolean sendUDPLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        JSONObject locJSON = new JSONObject();
        try {
            locJSON.put("lat", lat);
            locJSON.put("lon", lon);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        byte[] locPacket = locJSON.toString().getBytes();
        sendUDPPacket(locPacket, locPacket.length);
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) MainActivity.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void Enable() {
        UDPEnabled = true;
        Global.UDPEnabled = UDPEnabled;
        OpenUDPSocket();
    }

    public void Disable() {
        UDPEnabled = false;
        Global.UDPEnabled = UDPEnabled;
    }
}
