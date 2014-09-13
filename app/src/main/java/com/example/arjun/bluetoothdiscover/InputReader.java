package com.example.arjun.bluetoothdiscover;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputReader implements Runnable {

    private final BluetoothSocket socket;
    private static final String InputFromBT=null;
    public InputReader(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            final InputStream input = socket.getInputStream();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "ASCII"));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                Log.d(InputFromBT, line);
            }
        } catch (IOException ex) {
            Log.d(InputFromBT, "Unable to fetch input stream.");
        }
    }

    public void cancel() {
        try {
            socket.close();

        } catch (IOException e) {
        }
    }
}