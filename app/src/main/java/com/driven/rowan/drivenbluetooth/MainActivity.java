package com.driven.rowan.drivenbluetooth;

import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    static TextView myLabel;
    static TextView myTextbox;
    static TextView myIncoming;
    static EditText mySpeed;
    static EditText myCurrent;
    static EditText myVoltage;

	static TextView mySpeedDataCount;
	static TextView myVoltsDataCount;
	static TextView myAmpsDataCount;

	// Threads
	private RandomGenerator Gen = new RandomGenerator();
	private BTDataParser Parser = new BTDataParser();

	// UI Update Timer
	private Timer UIUpdateTimer; // don't initialize because we have to do it below

	// UI Update Handler
	private final Handler UIUpdateHandler = new Handler();

	// UI Update TimerTask
	private TimerTask UIUpdateTask; // initialized later on

    boolean deviceConnected = false;
    boolean matchingDeviceFound = false;


    String TAG = "DBDebug";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (TextView)findViewById(R.id.entry);
        myIncoming = (TextView)findViewById(R.id.incoming);
        mySpeed = (EditText)findViewById(R.id.speed);
        myCurrent = (EditText)findViewById(R.id.current);
        myVoltage = (EditText)findViewById(R.id.voltage);

		mySpeedDataCount = (TextView) findViewById(R.id.SpeedDataCount);
		myVoltsDataCount = (TextView) findViewById(R.id.VoltsDataCount);
		myAmpsDataCount = (TextView) findViewById(R.id.AmpsDataCount);


        final BluetoothManager myBluetoothManager = new BluetoothManager();

        //Open Button
		openButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if (Gen.getState() == Thread.State.TERMINATED) { Gen = new RandomGenerator();}

					if (Parser.getState() == Thread.State.TERMINATED) {	Parser = new BTDataParser();}

					Gen.start();
					Parser.start();

					// UI Updater TODO clean this up to make it neater - perhaps subclass it
					UIUpdateTask = new TimerTask() {
						public void run() {
							UIUpdateHandler.post(new UIUpdate());
						}
					};
					UIUpdateTimer = new Timer();
					UIUpdateTimer.schedule(UIUpdateTask, 250, 250);

				} catch (Exception e) {
					showMessage(e.getMessage().toString());
				}
			}
		});
        /*openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    myLabel.setText("Bluetooth Device Found");
                    deviceConnected = false;
                    myBluetoothManager.closeBT();
                    myBluetoothManager.findBT();
                    myBluetoothManager.openBT();
                    if(deviceConnected)
                        showMessage("Bluetooth Successfully Connected");
                    else
                        showMessage("Failed to Connect, Try Again");
                }
                catch (IOException ex) {
                    showMessage("Failed to Connect, Try Again");
                }
            }
        });*/

        //Send Button
		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Gen.cancel();
				Parser.cancel();
				UIUpdateTimer.cancel();
				UIUpdateTimer.purge();
			}
		});
        /*sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    myBluetoothManager.sendData();
                }
                catch (IOException ex) {
                    showMessage("SEND FAILED");
                }
            }
        });*/

        //Close button
		closeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				runOnUiThread(new UIUpdate());
			}
		});
        /*closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    myBluetoothManager.closeBT();
                }
                catch (IOException ex) { }
            }
        });*/
    }


    private void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(),
                theMsg, (Toast.LENGTH_LONG));
        msg.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
