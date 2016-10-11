package com.ben.drivenbluetooth.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.ben.drivenbluetooth.Global;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AppUpgradeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /* package has been updated. We need to do the following:
         * 1. Backup arduino.csv (rename to arduino.csv.backup[date]
         */

        BackupDataFile();
    }

    private void BackupDataFile() {
        File previousFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Global.DATA_FILE);

        if (previousFile.exists()) {
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            File backupFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), String.format("%s.%s.backup", Global.DATA_FILE, simpleDateFormat.format(currentDate)));
            previousFile.renameTo(backupFile);
        }
    }
}
