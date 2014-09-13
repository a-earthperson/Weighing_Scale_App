package com.example.arjun.bluetoothdiscover;

/**
 * Created by arjun on 9/5/14.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * This class implements the Bluetooth connection. It has the code for Bluetooth Server,
 * Client, upload, and download data with a remote BT device. The main structure is referenced
 * to the Android Bluetooth Example and Bluetooth API.
 * http://developer.android.com/guide/topics/wireless/bluetooth.html
 * http://developer.android.com/reference/android/bluetooth/package-summary.html
 * In WANDA project, because all the medical devices act as Master, the phone is
 * always a slave or server. It creates a socket waiting for the connection request
 * from a specific device. To avoid infinite waiting, the socket automatically
 * closes after 30 seconds if not being connected. There are three main threads
 * in this class, AcceptThread, ConnectThread, ConnectedThread. AcceptThread is used
 * for accepting connection from a remote client device, and ConnectThread is used
 * to initiate a connection with remote server device. Upon the connection is created,
 * the Bluetooth socket is passed to ConnectedThread and the ConnectedThread is
 * responsible for receiving and sending data. AcceptThread and ConnectThread will be
 * nullified once the ConnectedThread is started.
 *
 */

public class BTService {
    private int deviceNumber = 0;
    private BluetoothAdapter mBluetoothAdapter;

    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;
    private BluetoothDevice device;

    public static final int CONN_ESTABLISHED = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int CONNECTION_LOST = 4;

    //private final Handler mHandler;

    // Name for the SDP record when creating server socket
    private String NAME;
    // Unique UUID for this application
    private UUID MY_UUID = UUID.fromString(Constants.BT_SPP);

    private String ADDRESS;
    private boolean continueAccept;
    private boolean killReconnect = false;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
  //   * @param context
     *            The UI Activity Context
  //   * @param handler
     *            A Handler to send messages back to the UI Activity
     * //@param devNum
     *            The device Number. BP = 0, Weight = 1
     *
     */
    public BTService(/*Context context, Handler handler,*/ String devMAC) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            NAME = Constants.RFCOMM_SERVICE_NAME;
            ADDRESS = devMAC;
            device = mBluetoothAdapter.getRemoteDevice(ADDRESS);
            /*mHandler = handler;*/
        }



    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        this.stop();
        continueAccept = true;

        if (mConnectThread == null) {
            mConnectThread = new ConnectThread(device);
            // Only the MGH can initiate the BT Connection
            // Phone need to initiate the connection for the very first time as well
            //if(deviceNumber==Constants.DEVICE_TYPE_MGH || deviceNumber==Constants.DEVICE_TYPE_OXIMETER)
            mConnectThread.start();
        }

        // Start the thread to connect with the given device
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /* Call this from the main Activity to send data to the remote device */
    public void write(byte[] bytes) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(bytes);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private boolean socketConnected = false;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server
                // code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");

            // Cancel discovery because it will slow down the connection
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.v("JAY", "Creating Socket With Remote Device: "
                        + deviceNumber);
//				byte[] tmp = new byte[1];
//				tmp[0]=Byte.parseByte("00", 16);
//				mmSocket.getOutputStream().write(tmp);
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    if (socketConnected)
                        mmSocket.close();
                    Log.e("JAY", "Can't Connect to Remote Device: "
                            + deviceNumber);
                } catch (IOException closeException) {
                }
                // Start the service over to restart listening mode
                // BTService.this.start();
                return;
            }
            Log.v("JAY", "Socket with Device " + deviceNumber + " Established");
            socketConnected = true;

            // Start the connected thread. Probably Not Necessary In Our
            // Project!!
            // BTService.this.stop();
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();

            synchronized (BTService.this) {
                mConnectThread = null;
            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                if (socketConnected)
                    mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client
                // code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        NAME, MY_UUID);

            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (continueAccept) {
                try {
                    Log.v("JAY", "Waiting Accept Device " + deviceNumber);
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e("JAY", "accept() failed");
                    if(!killReconnect)
                        BTService.this.start();
                    break;
                }

                // If a connection is accepted
                if (socket != null) {
               /*     String remoteDevice=socket.getRemoteDevice().getName();
                    int connectedDev=-1;
                    if(remoteDevice.indexOf("TANITA")>=0)
                        connectedDev=Constants.DEVICE_TYPE_SCALE;
                    else if(remoteDevice.indexOf("AND")>=0 || remoteDevice.indexOf("FireFly")>=0)
                        connectedDev=Constants.DEVICE_TYPE_BP;
                    else if(remoteDevice.indexOf("myglucohealth")>=0)
                        connectedDev=Constants.DEVICE_TYPE_MGH;
                    else if(remoteDevice.indexOf("Nonin_Medical_Inc")>=0)
                        connectedDev=Constants.DEVICE_TYPE_OXIMETER;

                    if(connectedDev != deviceNumber){
                        Log.v("JAY", "Please turn on the correct device.");
                        socket=null;
                        this.cancel();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }*/

                    Log.v("JAY", "Decive " + deviceNumber
                            + " Accepted Connection!!");
                    mConnectedThread = new ConnectedThread(socket);
                    mConnectedThread.start();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                synchronized (BTService.this) {
                    mAcceptThread = null;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean socketConnected = false;
        private String remoteDevice="";

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
                socketConnected = true;
            } catch (IOException e) {
                socketConnected = false;
            }
            // Double check the connected device
            remoteDevice=mmSocket.getRemoteDevice().getName();
           /* if(remoteDevice.indexOf("TANITA")>=0)
            {
                deviceNumber=Constants.DEVICE_TYPE_SCALE;
//				byte[] tmp = new byte[1];
//				tmp[0]=Byte.parseByte("00", 16);
//				write(tmp);
            }
            else if(remoteDevice.indexOf("AND")>=0 || remoteDevice.indexOf("FireFly")>=0)
                deviceNumber=Constants.DEVICE_TYPE_BP;
            else if(remoteDevice.indexOf("myglucohealth")>=0)
                deviceNumber=Constants.DEVICE_TYPE_MGH;*/
            Log.v("JAY", "Remote Device Name: " + remoteDevice + " Num: " + deviceNumber);

          //  mHandler.obtainMessage(BTService.CONN_ESTABLISHED, deviceNumber).sendToTarget();

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    //String str = new String(buffer, 0, bytes);
                    //Log.v("JAY", "Thanks God!" + str);
                    // Send the obtained bytes to the UI Activity.

            //     mHandler.obtainMessage(BTService.MESSAGE_READ, bytes, deviceNumber,
             //               buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e("JAY", "Connection with Device " + deviceNumber
                            + " Lost");
                    Log.e("JAY", e.toString());
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer
         *            The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
         //       mHandler.obtainMessage(BTService.MESSAGE_WRITE, -1,
          //              deviceNumber, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("JAY", "Exception during write", e);
            }
        }

        /* Call this from the main Activity to shutdown the connection */
        public void cancel() {
            try {
                if (socketConnected)
                    mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        continueAccept = false;
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;

        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private void connectionLost() {
        // Send a failure message back to the Activity
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.v("JAY", "Cancel Discovery after disconnection");
        }
      //  Message msg = mHandler.obtainMessage(BTService.CONNECTION_LOST);
     //   mHandler.sendMessage(msg);

        if(!killReconnect){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.start();
        }
    }

    /**
     * Avoid automatically reconnect
     */
    public void killReconnect(){
        killReconnect = true;
    }
}
