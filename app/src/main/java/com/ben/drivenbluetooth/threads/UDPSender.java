package com.ben.drivenbluetooth.threads;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.MainActivity;

import org.acra.ACRA;
import org.apache.commons.math3.stat.regression.ModelSpecificationException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class UDPSender extends Thread {
    public Handler PacketHandler;
    public Handler ConnectivityChangeHandler;
    public Handler LocationHandler;
    public Handler OpenUDPSocketHandler;
    private InetAddress IPAddress;
    private DatagramSocket mUDPSocket;
    private int socketCounter = 0;
    private int sendFailCount = 0;
    private boolean mUDPSocketOpen = false;
    private boolean UDPEnabled = false;
    private TimerTask udpTask = new TimerTask() {
        @Override
        public void run() {
            if (PacketHandler != null) {
                PacketHandler.sendEmptyMessage(0);
            }
        }
    };
    private Timer udpTimer = new Timer();

    public UDPSender() {
        UDPEnabled = Global.UDPEnabled;
    }

        @Override
        public void run() {
            Looper.prepare();

            udpTimer.schedule(sendJsonTask, 0, 1100);

//            if (UDPEnabled) {
//
//
//                }

            Looper.loop();
        }

        private TimerTask sendJsonTask = new TimerTask(){
            @Override
            public void run() {
                try {
                    Log.d("SendData","About to JSON Data Timer");
                    Boolean success = sendJSONData();
                    //MainActivity.showMessage("Sent JSON Data");
                    //Log.d("SendData","Sent JSON Data");
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };




    private JSONObject getJson()
    {
        JSONObject dataJSON = new JSONObject();
        DecimalFormat format = new DecimalFormat("#.##");
        try{
            dataJSON.put("Vtotal", format.format(Global.Volts));
            dataJSON.put("Vlower", format.format(Global.VoltsAux));
            dataJSON.put("Amps", format.format(Global.Amps));
            dataJSON.put("RPM", format.format(Global.MotorRPM));
            dataJSON.put("Throttle", format.format(Global.InputThrottle));
            dataJSON.put("Lat", Global.Latitude);
            dataJSON.put("Lon", Global.Longitude);
            dataJSON.put("AmpH", format.format(Global.AmpHours));

        }catch (JSONException e) {
            e.printStackTrace();
        }

        return dataJSON;
    }


    private boolean sendJSONData()  throws IOException {


        HttpURLConnection urlConnection = null;
        try {
            URL url;
            URLConnection urlConn;
            DataOutputStream printout;
            DataInputStream input;

            url = new URL("https://dweet.io/dweet/for/"+Global.UDPPassword+"?");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("content-type","application/json");


            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(getJson().toString().getBytes());
//            Log.d("SendData", "Sending: "+getJson().toString());
//            Log.d("SendData", "To: "+url.toString());

            out.flush();


            StringBuilder sb = new StringBuilder();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                Log.d("SendData", "HTTP Response OK");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                System.out.println(urlConnection.getResponseMessage());
            }

            urlConnection.disconnect();

        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void Enable() {
        UDPEnabled = true;
        Global.UDPEnabled = UDPEnabled;

    }

    public void Disable() {
        UDPEnabled = false;
        Global.UDPEnabled = UDPEnabled;
    }

    public void pause() {
        UDPEnabled = false;
    }

    public void restart() {
        if(Global.UDPEnabled == UDPEnabled)
            UDPEnabled = true;
    }
}
