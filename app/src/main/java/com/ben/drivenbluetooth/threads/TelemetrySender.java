package com.ben.drivenbluetooth.threads;


import android.os.Looper;
import android.util.Log;

import com.ben.drivenbluetooth.Global;
import com.ben.drivenbluetooth.events.DialogEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class TelemetrySender extends Thread {
    private boolean telEnabled = false;
    private Timer telUpdateTimer = new Timer();
    private String dweetProToken = "";
    private boolean waitingForLogin = false;



    public TelemetrySender() {
        telEnabled = Global.telemetryEnabled;
    }

        @Override
        public void run() {
            Looper.prepare();

            telUpdateTimer.schedule(sendJsonTask, 0, 1500);

            boolean tokenRecieved = false;

            if (telEnabled && Global.enableDweetPro && (!Global.dweetProUsername.equals("")) && !Global.dweetProPassword.equals("") && Global.dweetMasterKey.equals("")) {
                Log.d("eChook", "Attempting to get dweet Auth Token");
                tokenRecieved = getDweetProToken();
                if(!tokenRecieved){
                    dweetLoginFailed();
                    //Global.enableDweetPro = false;
                }
                waitingForLogin = false;
            }

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


    private void dweetLoginFailed()
    {
        EventBus.getDefault().post(new DialogEvent("Dweet Login Failed", "Please check login details and internet connection"));
        Global.enableDweetPro = false;
    }

    private boolean getDweetProToken()
    {
        boolean success;
        HttpURLConnection urlConnection;
        try {
            URL url;

            url = new URL("https://dweetpro.io:443/v2/users/login");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("content-type","application/json");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(getLoginJson().toString().getBytes());
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

                Log.d("eChook", "Token request response:"+sb.toString());

                //Get token from response:

//                try {
//                    JSONObject receivedJson = new JSONObject(sb.toString());
//                    dweetProToken = receivedJson.getString("");
//                }catch (JSONException e)
//                {
//                    e.printStackTrace();
//                    Log.d("eChook", "Token not Found - JSON exception");
//                    success = false;
//                }

                try {
                    JSONObject receivedJson = new JSONObject(sb.toString());
                    Iterator iterator = receivedJson.keys();
                    String key = "";
                    while(iterator.hasNext())
                    {
                        key = (String)iterator.next();
                        Log.d("eChook", "Keys Found " + key);
                    }
                    Log.d("eChook", "Opening object from key");
                    JSONObject nestedJson = receivedJson.getJSONObject(key);
                    Log.d("eChook", "Looking in object:" + receivedJson.toString());
                    dweetProToken = nestedJson.getString("token");
                }catch (JSONException e)
                {
                    e.printStackTrace();
                    Log.d("eChook", "Token not Found - JSON exception");
                    success = false;
                }


                success = true;

                Log.d("eChook", "Received Token:"+dweetProToken);

                //System.out.println("" + sb.toString());
            } else {
                System.out.println(urlConnection.getResponseMessage());
                success = false;
                Log.d("eChook", "Non OK response to dweet login request");
            }
            urlConnection.disconnect();

        }catch (IOException e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }


    private JSONObject getLoginJson()
    {
        JSONObject loginJson = new JSONObject();
        DecimalFormat format = new DecimalFormat("#.##");
        try{
            loginJson.put("username", Global.dweetProUsername);
            loginJson.put("password", Global.dweetProPassword);
            
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return loginJson;
        
    }
    
    private JSONObject getDataJson()
    {
        JSONObject dataJSON = new JSONObject();
        JSONObject proJSON = new JSONObject();
        DecimalFormat format = new DecimalFormat("#.##");
        try{
            dataJSON.put("Vt", format.format(Global.Volts));
            dataJSON.put("V1", format.format(Global.VoltsAux));
            dataJSON.put("A", format.format(Global.Amps));
            dataJSON.put("RPM", format.format(Global.MotorRPM));
            dataJSON.put("Spd", format.format(Global.SpeedMPS));
            dataJSON.put("Thrtl", format.format(Global.InputThrottle));
            dataJSON.put("AH", format.format(Global.AmpHours));
            dataJSON.put("Lap", format.format(Global.Lap));
            dataJSON.put("Tmp1", format.format(Global.TempC1));
            dataJSON.put("Tmp2", format.format(Global.TempC2));
            dataJSON.put("Brk", format.format(Global.Brake));
            dataJSON.put("Gear", format.format(Global.Gear));


            if(Global.enableDweetPro && Global.enableLocationUpload) {
                dataJSON.put("Lat", Global.Latitude);
                dataJSON.put("Lon", Global.Longitude);
            }

            if(Global.enableDweetPro){
                proJSON.put("thing", Global.dweetThingName);
                proJSON.put("key", Global.dweetMasterKey);
                proJSON.put("content", dataJSON);
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }

        if(Global.enableDweetPro) {
            Log.d("eChook", "Compiled JSON pro = " + proJSON.toString());
            return proJSON;
        }
        else
            return dataJSON;
    }


    private boolean sendJSONData()  throws IOException {

        Log.d("eChook","Entering SendJSON data Token= "+ dweetProToken);
        Log.d("eChook","waiting for login = "+ waitingForLogin);
        if(Global.enableDweetPro && !waitingForLogin && dweetProToken.equals("")) //Catches the usecase when someone enables dweet pro once the thread is started and a login is needed.
        {
            Log.d("eChook","PRo enabled but no token. Token = "+ dweetProToken);
            waitingForLogin = true;
            getDweetProToken();
            return true;
        }else {
            HttpURLConnection urlConnection;
            try {
                URL url;

                if (Global.enableDweetPro) {
                    url = new URL("https://dweetpro.io:443/v2/dweets");
                } else {
                    url = new URL("https://dweet.io/dweet/for/" + Global.dweetThingName + "?");
                }
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("content-type", "application/json");
                if (Global.enableDweetPro)
                    urlConnection.setRequestProperty("X-DWEET-AUTH", dweetProToken);

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                out.write(getDataJson().toString().getBytes());
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

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }


    public void Enable() {
        telEnabled = true;
        Global.telemetryEnabled = true;

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
