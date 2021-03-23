package com.ben.drivenbluetooth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
