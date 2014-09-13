package com.example.arjun.bluetoothdiscover;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

class ConnectRunnable implements Runnable {

    private boolean STATE = false;
    private final ProgressDialog dialog;
    private BluetoothSocket socket,socket_tmp=null;
    private final BluetoothAdapter BT_Adapter;
    private final byte[] data ="{0,16,~0,1,~1,1,~2,1CRLF".getBytes();
    private final byte data2 ='0';
    private static final String Connect_Thread=null;
    private static final UUID mUUID = UUID.fromString(Constants.BT_SPP);

    public ConnectRunnable(BluetoothAdapter adapter,BluetoothDevice device, ProgressDialog dialog2) throws IOException {

        try{
            socket_tmp = device.createInsecureRfcommSocketToServiceRecord(mUUID);
            Log.d(Connect_Thread,"Socket device created");
        }
        catch (IOException e)
        {
            Log.d(Connect_Thread,"Unable to create socket device");
        }
        socket = socket_tmp;
        BT_Adapter = adapter;
        dialog = dialog2;

    }
    public void beginTransmission(BluetoothSocket socket){
    final ConnectedThread transmission = new ConnectedThread(socket);
      transmission.start();
    }

    public void run() {
        try {
            BT_Adapter.cancelDiscovery();
            Log.d(Connect_Thread,"Attempting to connect to socket..");
            socket.connect();
            Log.d(Connect_Thread,"Socket opened successfully.");
        } catch (IOException connectException) {
            Log.d(Connect_Thread,"Connection failed");
            try {
                socket.close();
                Log.d(Connect_Thread,"Socket closed");
            } catch (IOException ex) {
                Log.d(Connect_Thread,"Unable to close socket");
            }
            return;
        }
        dialog.dismiss();
        while(true) {
            if (socket.isConnected())
                Log.d(Connect_Thread, "Socket is connected");
            else
                break;
        }


        Log.d(Connect_Thread,"Socket got disconnected");
        //beginTransmission(socket);


    }
    public void cancel() {
        Log.d(Connect_Thread,"Received cancel signal...");
        try {
            Log.d(Connect_Thread,"Socket closed");
            socket.close();
        } catch (IOException e) {
            Log.d(Connect_Thread,"Unable to close socket");
        }
    }
}