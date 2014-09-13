package com.example.arjun.bluetoothdiscover;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by arjun on 9/4/14.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private static final String TAG = null;

    public ConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "create ConnectedThread");
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            Log.d(TAG, "Temp Sockets created");
        } catch (IOException e) {
            Log.d(TAG, "temp sockets not created");
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }
    public void parse(char[] data,int index){
    TANITA_Parser dataset= new TANITA_Parser(data,index);
    dataset.print();
    }
    public void run() {
        Log.d(TAG, "BEGIN mConnectedThread");
        char[] bytesAvailable = new char[168];
        char[] data;
        int index = -1;
        byte[] buffer = new byte[1024];
        // Keep listening to the InputStream while connected


        while (true) {
            try {
                // Read from the InputStream
                index++;
                bytesAvailable[index] = (char) mmInStream.read();
                if(bytesAvailable[index]=='\r')
                {
                    Log.d(TAG, "EOF");
                    data=bytesAvailable;
                    parse(data,index);
                    index=0;
                }
            } catch (IOException e) {
                Log.d(TAG, "disconnected");
                break;
            }
        }
    }
}

