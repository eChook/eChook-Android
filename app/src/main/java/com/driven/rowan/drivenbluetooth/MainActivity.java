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
    EditText myIncoming;
    EditText mySpeed;
    EditText myCurrent;
    EditText myVoltage;
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
    boolean deviceConnected = false;
    boolean matchingDeviceFound = false;

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
        myIncoming = (EditText)findViewById(R.id.incoming);
        mySpeed = (EditText)findViewById(R.id.speed);
        myCurrent = (EditText)findViewById(R.id.current);
        myVoltage = (EditText)findViewById(R.id.voltage);

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    deviceConnected = false;
                    closeBT();
                    findBT();
                    openBT();
                    if(deviceConnected)
                        showMessage("Bluetooth Successfully Connected");
                    else
                        showMessage("Failed to Connect, Try Again");

                }
                catch (IOException ex) {
                    showMessage("Failed to Connect, Try Again");
                }
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

        stopWorker = true;
        Log.d( TAG, "Entering FindBT");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "FindBT before If");

        mBluetoothAdapter.cancelDiscovery();

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
                    matchingDeviceFound = true;
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.d(TAG, "Bluetooth Device Found");
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException {
        if(matchingDeviceFound) {
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
            deviceConnected = true;
            myLabel.setText("Bluetooth Opened");
        }else {
            Log.d(TAG, "No matching device found to open");
        }


    }
       /*
    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 92; //10 is the ASCII code for a newline character, 92 is '\'

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[2048];
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
                                byte id = packetBytes[i++];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    Log.d(TAG,"Incoming Data. id= "+id+", Data= "+data);

                                    if(id == 's') {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                mySpeed.setText(data + "mph");
                                            }
                                        });
                                    }else if(id == 'v') {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myVoltage.setText(data + "V");
                                            }
                                        });
                                    }else if(id == 'i') {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myCurrent.setText(data + "A");
                                            }
                                        });
                                    }else {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myIncoming.append(data + "\n");
                                            }
                                        });
                                    }
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
    }*/

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 92; //10 is the ASCII code for a newline character, 92 is '\'

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[2048];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        int j;
                        int lpc=0;
                        if(bytesAvailable > 4) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    Log.d(TAG,"Bytes available = "+bytesAvailable);
                                    Log.d(TAG,"Delimiter Position = "+i);
                                    for(j = bytesAvailable-1; j>0; j-- ) {

                                        if (packetBytes[j] == 37){
                                            Log.d(TAG,"Start Position = "+j);
                                            break;// exits leaving j as the position of the start byte
                                        }

                                    }

                                    if(i-j-2 <= 0) { //safety case for creating the following array         %s1234\%v1234\
                                        lpc = lpc+1;
                                        //final int lpc2 = lpc;
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myIncoming.append("Lost a Packet \n");
                                            }
                                        });
                                        Log.d(TAG,"Breaking. Tried to create an array of "+(i-j-2) +" count = "+lpc);
                                        break;
                                    }

                                    byte[] encodedBytes = new byte[i-j-2];//length of incoming data, excluding start and end delimiters

                                    Log.d(TAG,"Creating encodedBytes array of " + encodedBytes.length);
                                    //Log.d(TAG,"Creating encodedBytes array of " + encodedBytes.length);
                                    //byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, j+2, encodedBytes, 0, encodedBytes.length); //encoded bytes should now be along the lines of "%s24.5\"
                                    char id = (char) readBuffer[j+1];
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    Log.d(TAG,"Incoming String = "+data);
                                    readBufferPosition = 0;

                                    //char id = (char) encodedBytes[1];

                                    Log.d(TAG,"Incoming Data. b= "+b+", id= "+id+", Data= "+data);

                                    if(id == 's') {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                mySpeed.setText(data + " mph");
                                            }
                                        });
                                    }else if(id == 'v') {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myVoltage.setText(data + " V");
                                            }
                                        });
                                    }else if(id == 'i') {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myCurrent.setText(data + " A");
                                            }
                                        });
                                    }else {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myIncoming.append(data + "\n");
                                            }
                                        });
                                    }
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

        if (deviceConnected) {
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
            deviceConnected = false;
        }
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
