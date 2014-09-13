package com.example.arjun.bluetoothdiscover;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

class SocketListener implements Runnable {

    private final ProgressDialog dialog;
    private BluetoothServerSocket server_socket,server_socket_tmp=null;
    private BluetoothSocket socket;
    private final BluetoothAdapter BT_Adapter;

    private static final String Connect_Thread=null;

    private static final UUID mUUID = UUID.fromString(Constants.BT_SPP);

    public SocketListener(BluetoothAdapter adapter,BluetoothDevice device, ProgressDialog dialog2) throws IOException {

        try{
            server_socket_tmp = adapter.listenUsingRfcommWithServiceRecord(Constants.RFCOMM_SERVICE_NAME,mUUID);
            Log.d(Connect_Thread,"Opened serverSocket");
        }
        catch (IOException e)
        {
            Log.d(Connect_Thread,"Unable to open ServerSocket");
        }
        server_socket = server_socket_tmp;
        BT_Adapter = adapter;
        dialog = dialog2;
        while(server_socket==null);

    }
    public void beginTransmission(BluetoothSocket socket){
        final ConnectedThread transmission = new ConnectedThread(socket);
        transmission.start();
    }

    public void run() {


        Log.d(Connect_Thread,"Waiting for BT device");
        socket = null;
        try {

            socket = server_socket.accept();
            Log.d(Connect_Thread,"Socket accepted");

        } catch (IOException connectException) {
            Log.d(Connect_Thread,"Socket did not get passed from serverSocket");
        }
        Log.d(Connect_Thread,"please print something");
        if(socket!=null) {

            try {
                server_socket.close();
                Log.d(Connect_Thread, "Socket is now open");
            } catch (IOException ex) {
                Log.d(Connect_Thread, "Accepted connection by unable to open socket");
            }
//        dialog.dismiss();
            beginTransmission(socket);
        }

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