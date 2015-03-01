package com.driven.rowan.drivenbluetooth;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;




public class MainActivity extends ActionBarActivity {

    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    String TAG = "DBDebug";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (EditText)findViewById(R.id.entry);

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });

        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();
                }
                catch (IOException ex) {
                    showMessage("SEND FAILED");
                }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
    }



    void findBT() {
        Log.d( TAG, "Entering FindBT");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "FindBT before If");
        if(mBluetoothAdapter == null) {
            Log.d(TAG, "Entering BT Adapter == null");
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled()) {

            Log.d(TAG, "Attempting to enable BT Adapter");
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Log.d(TAG, "Attempting to get bonded devices");

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        Log.d(TAG, "Got bonded devices");

        Log.d(TAG, "Returned " + pairedDevices.size() + " Devices");

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {

                Log.d(TAG, "Checking Name "+ device.getName());

                if(device.getName().equals("RNBT-62D8")) { //TODO Handle scenario where device is not paired to phone

                    Log.d(TAG, "Device Matching Name Found");

                    mmDevice = device;
                    break;
                }
            }
        }
        Log.d(TAG, "Bluetooth Device Found");
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException {

        Log.d(TAG, "Attempting to open BT Device");

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard //SerialPortService ID
        Log.d(TAG, "Connecting Socket Service to device");
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        Log.d(TAG, "Connecting to socket ");
        mmSocket.connect(); //TODO set timer on mmSocket.Connect - else blocks thread when failing to connect
        Log.d(TAG, "Setting Output Stream");
        mmOutputStream = mmSocket.getOutputStream();
        Log.d(TAG, "Setting Input Stream");
        mmInputStream = mmSocket.getInputStream();
        Log.d(TAG, "Starting to Listen");
        beginListenForData();
        myLabel.setText("Bluetooth Opened");
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException {
        String msg = myTextbox.getText().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        //mmOutputStream.write('A');
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }

    private void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(),
                theMsg, (Toast.LENGTH_LONG)/160);
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
