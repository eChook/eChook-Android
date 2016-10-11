package com.ben.drivenbluetooth.threads;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import org.acra.ACRA;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
    public Handler OpenUDPSocketHandler;

    public UDPSender() {
        UDPEnabled = Global.UDPEnabled;
    }

    private TimerTask udpTask = new TimerTask() {
        @Override
        public void run() {
            if (PacketHandler != null) {
                PacketHandler.sendEmptyMessage(0);
            }
        }
    };

    private Timer udpTimer = new Timer();

	@Override
	public void run() {
		Looper.prepare();

        udpTimer.schedule(udpTask, 0, 500);

        PacketHandler = new Handler (new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (UDPEnabled && mUDPSocketOpen) {
                    if (!sendUDPData()) sendFailCount++;
                    socketCounter = 0;

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

        OpenUDPSocketHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                return mUDPSocketOpen = OpenUDPSocket();
            }
        });

        if (UDPEnabled) {
            int nAttempts = 1;

            while (!(mUDPSocketOpen = OpenUDPSocket()) && nAttempts < 3) {
                MainActivity.showMessage("Connecting to node.js server [" + Integer.toString(nAttempts) + "]");
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                nAttempts++;
            }

            if (mUDPSocketOpen) {
                MainActivity.showMessage("Successfully connected to node.js server");
            } else {
                MainActivity.showMessage("Could not connect to node.js server");
            }
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
            MainActivity.showMessage("Successfully connected to node.js server");
        } catch (Exception e) {
            MainActivity.showMessage("Could not connect to node.js server");
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

    private boolean sendUDPData() {
        boolean success;
        byte[] data = getDataToSend();
        try {
            mUDPSocket.send(new DatagramPacket(data, data.length, IPAddress, Global.SOCKETPORT));
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    @NonNull
    private byte[] getDataToSend() {
        return String.format("{" +
                "\"v\":%.1f," +
                "\"a\":%.1f," +
                "\"m\":%.0f," +
                "\"s\":%.1f," +
                "\"gr\":%.2f," +
                "\"ah\":%.2f," +
                "\"w\":%.2f," +
                "\"t1\":%.1f," +
                "\"t2\":%.1f," +
                "\"n\":\"%s\"," +
                "\"p\":\"%s\""+
                "}",
                Global.Volts,
                Global.Amps,
                Global.MotorRPM,
                Global.SpeedMPS,
                Global.GearRatio,
                Global.AmpHours,
                Global.WattHoursPerMeter * 1000,
                Global.TempC1,
                Global.TempC2,
                Global.CarName,
                Global.UDPPassword).getBytes(Charset.defaultCharset());
    }

    private boolean sendUDPLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        JSONObject locJSON = new JSONObject();
        try {
            locJSON.put("lat", lat);
            locJSON.put("lon", lon);
            locJSON.put("n", Global.CarName);
            locJSON.put("p", Global.UDPPassword);
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
