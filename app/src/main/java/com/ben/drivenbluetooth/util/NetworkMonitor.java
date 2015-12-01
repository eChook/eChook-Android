package com.ben.drivenbluetooth.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ben.drivenbluetooth.MainActivity;

public class NetworkMonitor extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.mUDPSender != null) {
            MainActivity.mUDPSender.ConnectivityChangeHandler.sendEmptyMessage(0);
        }
    }
}
