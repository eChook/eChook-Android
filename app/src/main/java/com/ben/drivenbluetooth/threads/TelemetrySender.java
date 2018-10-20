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
import java.util.Timer;
import java.util.TimerTask;

//import java.util.Iterator;

public class TelemetrySender extends Thread {
private boolean telEnabled = false;
private Timer telUpdateTimer = new Timer();
private String echookID = "";
private boolean waitingForLogin = false;
private int currentLap = 0;
private boolean firstRun = true;



public TelemetrySender() {
        telEnabled = (Global.dweetEnabled || Global.eChookLiveEnabled);
        //Log.d("eChook","TelemetrySender: telEnabled = " + telEnabled );
}


@Override
public void run() {
        Looper.prepare();
        telUpdateTimer.schedule(sendJsonTask, 0, 1500);

        boolean echookIdReceived;

        if (telEnabled && Global.eChookLiveEnabled && (!Global.eChookCarName.equals("")) && !Global.eChookPassword.equals("")) {     //If echook live is selected and login details aren't empty
                Log.d("eChook", "Attempting to get eChook Live ID");
                echookIdReceived = getEchookId();
                if(!echookIdReceived) {
                    EventBus.getDefault().post(new DialogEvent("eChook Login Failed", ""));
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
                                Log.d("eChook", "About to trigger JSON Data");
                                Boolean success = sendJSONData();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
        }
};

private boolean getEchookId()
{
        Log.d("eChook", "Getting eChook Live Login");
        boolean success;
        HttpURLConnection urlConnection;
        try {
                URL url;

                url = new URL("https://data.echook.uk/api/getid");
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
                        Log.d("eChook", "HTTP Response OK to Login Request");
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                        String line;
                        while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                        }
                        br.close();

                        Log.d("eChook", "ID request response:"+sb.toString());

                        try {
                                JSONObject receivedJson = new JSONObject(sb.toString());
                                echookID = receivedJson.getString("id");
                        }catch (JSONException e)
                        {
                                e.printStackTrace();
                                Log.d("eChook", "ID not Found - JSON exception");
                        }


                        success = true;

                        Log.d("eChook", "Received ID:"+ echookID);
                        EventBus.getDefault().post(new DialogEvent("eChook Login Successful", ""));


                } else {
                        System.out.println(urlConnection.getResponseMessage());
                        success = false;
                        EventBus.getDefault().post(new DialogEvent("eChook Login Failed", "Bad Response from Server"));
                        Log.d("eChook", "Non OK response to eChook Live login request");
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
        try{
                loginJson.put("username", Global.eChookCarName);
                loginJson.put("password", Global.eChookPassword);

        }catch (JSONException e) {
                e.printStackTrace();
        }

        return loginJson;

}

private JSONObject getDataJson(boolean location)
{
        JSONObject dataJSON = new JSONObject();
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


                if(location) {
                        dataJSON.put("Lat", Global.Latitude);
                        dataJSON.put("Lon", Global.Longitude);
                }

                if(currentLap != Global.Lap && Global.Lap > 0) {
                        currentLap = Global.Lap;
                        dataJSON.put("LL_V", format.format(Global.LapDataList.get(currentLap-1).getAverageVolts()));
                        dataJSON.put("LL_I", format.format(Global.LapDataList.get(currentLap-1).getAverageAmps()));
                        dataJSON.put("LL_RPM", format.format(Global.LapDataList.get(currentLap-1).getAverageRPM()));
                        dataJSON.put("LL_Spd", format.format(Global.LapDataList.get(currentLap-1).getAverageSpeedMPS()));
                        dataJSON.put("LL_Ah", format.format(Global.LapDataList.get(currentLap-1).getAmpHours()));
                        dataJSON.put("LL_Time", format.format(Global.LapDataList.get(currentLap-1).getLapTimeString()));
                        dataJSON.put("LL_Eff", format.format(Global.LapDataList.get(currentLap-1).getWattHoursPerKM()));
                }

                if(firstRun) {
                        firstRun = false;
                        dataJSON.put("LL_V", "0");
                        dataJSON.put("LL_I", "0");
                        dataJSON.put("LL_RPM", "0");
                        dataJSON.put("LL_Spd", "0");
                        dataJSON.put("LL_Ah", "0");
                        dataJSON.put("LL_Time", "0");
                        dataJSON.put("LL_Eff", "0");
                }

        }catch (JSONException e) {
                e.printStackTrace();
        }

        return dataJSON;
}


        private boolean sendJSONData() {

        if(Global.dweetEnabled) {

                Log.d("eChook", "Entering Send Dweet Data");

                HttpURLConnection urlConnection;
                try {
                        URL url;
                        url = new URL("https://dweet.io/dweet/for/" + Global.dweetThingName + "?");
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setDoOutput(true);
                        urlConnection.setChunkedStreamingMode(0);
                        urlConnection.setRequestProperty("content-type", "application/json");
                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                        out.write(getDataJson(false).toString().getBytes());
                        out.flush();


                        StringBuilder sb = new StringBuilder();
                        int HttpResult = urlConnection.getResponseCode();
                        if (HttpResult == HttpURLConnection.HTTP_OK) {
                                Log.d("SendData", "HTTP Response OK");
                                BufferedReader br = new BufferedReader(
                                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                                String line;
                                while ((line = br.readLine()) != null) {
                                        sb.append(line).append("\n");
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

        if(Global.eChookLiveEnabled) {
                Log.d("eChook", "Entering eChook Live SendJSON data, ID = " + echookID);
                if (Global.eChookLiveEnabled && !waitingForLogin && echookID.equals("")) //Catches the usecase when someone enables eChook live data once the thread is started and a login is needed.
                {
                        Log.d("eChook", "eChook Live enabled but no ID. ID = " + echookID);
                        waitingForLogin = true;
                        getEchookId();
                        return true;
                } else {
                        HttpURLConnection urlConnection;
                        try {
                                URL url = new URL("https://data.echook.uk/api/send/" + echookID);
                                urlConnection = (HttpURLConnection) url.openConnection();
                                urlConnection.setDoOutput(true);
                                urlConnection.setChunkedStreamingMode(0);
                                urlConnection.setRequestProperty("content-type", "application/json");
                                if (Global.enableDweetPro)
                                        urlConnection.setRequestProperty("X-DWEET-AUTH", echookID);

                                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                                out.write(getDataJson(true).toString().getBytes());
                                out.flush();


                                StringBuilder sb = new StringBuilder();
                                int HttpResult = urlConnection.getResponseCode();
                                if (HttpResult == HttpURLConnection.HTTP_OK) {
                                        Log.d("SendData", "HTTP Response OK");
                                        BufferedReader br = new BufferedReader(
                                                new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                                        String line;
                                        while ((line = br.readLine()) != null) {
                                                sb.append(line).append("\n");
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
        return false;
}


public void Enable() {
        telEnabled = true;
}

public void Disable() {
        telEnabled = false;
}

public void pause() {
        telEnabled = false;
}

public void restart() {
        telEnabled = Global.dweetEnabled || Global.eChookLiveEnabled;

}
}
