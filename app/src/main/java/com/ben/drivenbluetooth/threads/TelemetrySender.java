package com.ben.drivenbluetooth.threads;

import android.icu.text.DecimalFormat;
import android.os.Looper;
import android.util.Log;

import com.ben.drivenbluetooth.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class TelemetrySender extends Thread {
    private boolean telEnabled = false;
    private Timer telUpdateTimer = new Timer();



    public TelemetrySender() {
        telEnabled = Global.telemetryEnabled;
    }

        @Override
        public void run() {
            Looper.prepare();

            telUpdateTimer.schedule(sendJsonTask, 0, 1100);

//            if (telEnabled) {
//
//
//                }

            Looper.loop();
        }

        private TimerTask sendJsonTask = new TimerTask(){
            @Override
            public void run() {
                if (telEnabled) {
                    try {
                        Log.d("SendData", "About to JSON Data Timer");
                        Boolean success = sendJSONData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };




    private JSONObject getJson()
    {
        JSONObject dataJSON = new JSONObject();
        DecimalFormat format = new DecimalFormat("#.##");
        try{
            dataJSON.put("Vt", format.format(Global.Volts));
            dataJSON.put("V1", format.format(Global.VoltsAux));
            dataJSON.put("V2", format.format(Global.Volts - Global.VoltsAux));
            dataJSON.put("A", format.format(Global.Amps));
            dataJSON.put("RPM", format.format(Global.MotorRPM));
            dataJSON.put("Spd", format.format(Global.MotorRPM));
            dataJSON.put("Thrtl", format.format(Global.InputThrottle));
            dataJSON.put("AH", format.format(Global.AmpHours));
            dataJSON.put("Lap", format.format(Global.Lap));
            dataJSON.put("Tmp1", format.format(Global.TempC1));
            dataJSON.put("Tmp2", format.format(Global.TempC2));
            dataJSON.put("Brk", format.format(Global.Brake));

            if(Global.enableLocationUpload) {
                dataJSON.put("Lat", Global.Latitude);
                dataJSON.put("Lon", Global.Longitude);
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }

        return dataJSON;
    }


    private boolean sendJSONData()  throws IOException {


        HttpURLConnection urlConnection;
        try {
            URL url;

            url = new URL("https://dweet.io/dweet/for/"+Global.UDPPassword+"?");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("content-type","application/json");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            if(Global.enableDweetPro)
                out.write("key="+Global.dweetProMasterKey+"&"getJson().toString().getBytes());
            else
                out.write(getJson().toString().getBytes());
            out.flush();


            StringBuilder sb = new StringBuilder();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                Log.d("SendData", "HTTP Response OK");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line;
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
        telEnabled = true;
        Global.telemetryEnabled = telEnabled;

    }

    public void Disable() {
        telEnabled = false;
        //Global.telEnabled = telEnabled;
    }

    public void pause() {
        telEnabled = false;
    }

    public void restart() {
        telEnabled = Global.telemetryEnabled;

    }
}
