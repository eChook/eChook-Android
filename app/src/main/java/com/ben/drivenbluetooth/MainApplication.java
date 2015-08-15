package com.ben.drivenbluetooth;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(
		httpMethod = HttpSender.Method.PUT,
		reportType = HttpSender.Type.JSON,
		formUri = "http://exantas.homenet.org:5984/acra-drivenbluetooth/_design/acra-storage/_update/report",
		formUriBasicAuthLogin = "driven-acra-user",
		formUriBasicAuthPassword = "herpsicle"
)

public class MainApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);
	}
}
