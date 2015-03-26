package com.driven.rowan.drivenbluetooth;

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


public class MainActivity extends ActionBarActivity {

    static TextView myLabel;
    static TextView myTextbox;
    static TextView myIncoming;
    static EditText mySpeed;
    static EditText myCurrent;
    static EditText myVoltage;
	private Thread Gen = new Thread(new RandomGenerator());
	private Thread Parser = new Thread(new BTDataParser());
	private Thread UIUpdater = new Thread(new UIUpdate());

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

        final BluetoothManager myBluetoothManager = new BluetoothManager();

        //Open Button
		openButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Gen.start();
					//Parser.start();
					//UIUpdater.start();
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
				Gen.stop();
				Parser.stop();
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
