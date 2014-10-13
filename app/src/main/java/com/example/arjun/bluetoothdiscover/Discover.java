package com.example.arjun.bluetoothdiscover;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.io.IOException;

/*
*
* Main Activity file. Class Discover is used to setup the initial interface, discover new devices
* and open a server socket connection with a selected device.
*
*/

public class Discover extends Activity {
    public ProgressDialog dialog = null;
    public TextView console = null;
    private static final String UI=null;

    String MAC = Constants.MAC;
    public BluetoothDevice BT_Device = null;
    public BluetoothAdapter BT_Adapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        if (BT_Adapter == null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("This device does not support Bluetooth");
            alertDialog.show();
        }
        console = (TextView) findViewById(R.id.console);
    }
/* console_dump for debugging */

    public void console_dump(String text)
    {
        console.append('\n' + text);
        Log.d(UI, text);

    }

/* Check if BT_Adapter is enabled. Currently, only fetches already paired device from OS. */
    public void discovery(View v)
    {

        if (BT_Adapter.isEnabled()) {
            console_dump("Discovering Devices.....");
            BT_Device=BT_Adapter.getRemoteDevice(MAC);
            console_dump("Found device with MAC address "+BT_Device.getAddress());
            console_dump(BT_Device.getName());
            console_dump("Class : "+BT_Device.getBluetoothClass());
        }

        else {
            console_dump("Bluetooth not enabled. Requesting enable...");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

/* openSocket() upon connect() event. */
    public void connect(View view){
        console_dump("Attempting to connect to " + BT_Device.getName());
        console_dump("Going discoverable");
        openSocket(BT_Device);
    }

/* init ProgressDialog, open serversocket. close dialog on successful connection. */
     private void openSocket(BluetoothDevice device) {
        console_dump("Opening Socket...");

        try {

            dialog = new ProgressDialog(this);
            final SocketListener connector = new SocketListener(BT_Adapter,device, dialog);
            dialog.show(this, "Establishing Connection", "Connecting to device " + device.getName() + " : " + device.getAddress(),
                    true, true,
                    new DialogInterface.OnCancelListener() {

                        public void onCancel(DialogInterface dialog) {
                            connector.cancel();
                        }
                    });

            new Thread(connector).start();

        } catch (IOException ex) {
            console_dump("Could not open bluetooth socket");
        }
    }
/*
    private void closeSocket(BluetoothSocket openSocket) {
        try {
            openSocket.close();
        } catch (IOException ex) {
            console_dump("Could not close existing socket");
        }
    }
*/



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.discover, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


