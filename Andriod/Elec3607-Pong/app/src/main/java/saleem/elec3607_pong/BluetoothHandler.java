package saleem.elec3607_pong;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static saleem.elec3607_pong.MainActivity.REQUEST_ENABLE_BT;

/**
 * Created by saleem on 18/05/17.
 */

public class BluetoothHandler implements Runnable {
    boolean exitCondition = false; // set this to true if you want to stop reading the data from the bluetooth device
    private TextChanger textChanger; // used to display messages on the status text
    private MainActivity activity; // reference to the main activity class; this is used to update the UI
    private BluetoothAdapter bluetoothAdapter;  // the built in adapter to communicate with bluetooth devices
    private SQLiteDatabase db; // database that stores our match histories
    private MatchHistoryAdapter adapter; // recycler view to display a list of our match histories
    private BluetoothTransmission bluetoothTransmission;
    public BluetoothHandler(MainActivity activity, MatchHistoryAdapter adapter, BluetoothTransmission bluetoothTransmission) {
        this.adapter = adapter;
        this.activity = activity;
        this.bluetoothTransmission = bluetoothTransmission;

        // the "connect bluetooth" button (used for enabling and disabling the button)
        MenuItem bluetoothItem = activity.menu.findItem(R.id.bluetooth_connect_button);

        // the status text
        TextView textView = (TextView) activity.findViewById(R.id.status_text);

        // create the new textChanger object
        textChanger = new TextChanger(textView, bluetoothItem);
        // set the status text to this, and set isConnected to false
        setText("Started Bluetooth Handler Thread!", false);

        // create the bluetooth adapter
        bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // if  the device doesn't support bluetooth
            setText("This device doesn't support Bluetooth!", false);
            return;
        }

        // if bluetooth is not enabled, ask to enable it
        if (!bluetoothAdapter.isEnabled()) {
            setText("Bluetooth is not enabled!", false);
            Intent enableBtIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // create the database reference object
        DatabaseHelper databaseHelper = new DatabaseHelper(activity);
        db = databaseHelper.getWritableDatabase();
    }

    @Override
    public void run() {
        if (bluetoothAdapter == null) {
            return;
        }

        // used to check whether we found HC-06 device or not
        String deviceName, deviceHardwareAddress;

        // check the paired devices
        setText("Checking paired devices", false);

        // get a list of all paired devices for this device
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        setText("Found " + pairedDevices.size() + " device(s).", false);

        // stop trying to find devices (make it go faster)
        bluetoothAdapter.cancelDiscovery();

        // loop through paired devices until we find the arduino devices
        BluetoothDevice arduinoShield = null;
        for (BluetoothDevice device : pairedDevices) {
            deviceName = device.getName(); // name of the device
            deviceHardwareAddress = device.getAddress(); // MAC address
            if (deviceName.equals(MainActivity.DEVICE_NAME)) {
                setText("Found device " + deviceName + " (" + deviceHardwareAddress + ")", false);
                arduinoShield = device;
                break;
            }
        }

        // not found arduino shield
        if (arduinoShield == null) {
            setText("Cannot find device " + MainActivity.DEVICE_NAME, false);
            return;
        }

        // create the socket to send and receive data
        BluetoothSocket bluetoothSocket = createSocket(arduinoShield);
        if (bluetoothSocket == null) {
            setText("Cannot create socket", false);
            return;
        }

        // handle the incoming and outgoing messages
        manageMyConnectedSocket(bluetoothSocket);
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {
        // handle the incoming and outgoing messages
        String message;
        try {
            // input reader
            InputStream inputStream = socket.getInputStream();

            // attach the output stream to the BluetoothTransmission thread and start it
            bluetoothTransmission.attach(socket.getOutputStream());
            new Thread(bluetoothTransmission).start();

            // an object to make reading the stream easier
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // keep trying to look for messages unless !exitCondition
            while (!exitCondition) {
                if (bufferedReader.ready()) {
                    // received a new message. Run the BluetoothAsyncTask Thread.
                    if ((message = bufferedReader.readLine()) != null) {
                        handleMessage(message);
                        setText("New message \"" + message + "\"", true);
                    }
                }
            }
            // once while loop exits, it means we've disconnected the device
            setText("Disconnected", false);
        } catch (Exception e) {
            // something went wrong
            exitCondition = true;
            setText(e.getMessage(), false);
            try {
                // close the socket
                socket.close();
            } catch (Exception e2) {
                setText(e2.getMessage(), false);
            }
        }
    }

    private void handleMessage(String message) {
        // basic error checking
        if (message == null || message.length() != 2) {
            return;
        }
        final int allyScore, aiScore;
        final String milliseconds; // the current date
        // convert it to integers
        allyScore = message.charAt(0) - '0';
        aiScore = message.charAt(1) - '0';
        milliseconds = String.valueOf(new Date().getTime());
        // basic error checking
        if (allyScore < 0 || allyScore > 9 || aiScore < 0 || aiScore > 9 || aiScore == allyScore) {
            return;
        }
        try {
            // write to the database
            ContentValues contentValues = new ContentValues();
            contentValues.put("p1_score", allyScore);
            contentValues.put("p2_score", aiScore);
            contentValues.put("date", milliseconds);

            SQLiteDatabase db = new DatabaseHelper(activity)
                    .getWritableDatabase();
            db.insert("matches", null, contentValues);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    // tell our recycler view that we have new data
                    long s = Long.parseLong(milliseconds);
                    adapter.addFirst(new MatchHistory(allyScore, aiScore, s));
                    adapter.notifyItemInserted(0);
                    // scroll up when new message is added
                    activity.recyclerView.smoothScrollToPosition(0);
                }
            });
        } catch (Exception e) {
            // something went wrong while trying to write to the database, send error message back
            return;
        }
    }

    private BluetoothSocket createSocket(BluetoothDevice device) {
        setText("Initialising socket", false);
        // create the socket
        BluetoothSocket socket;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            // bluetooth serial port service
            UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);

        } catch (IOException e) {
            // something went wrong with  creating the socket
            setText("Failed to create a socket " + e.getMessage(), false);
            return null;
        }
        setText("Successfully created socket!", false);
        try {
            // try connect to the bluetooth device
            setText("Trying to connect to bluetooth device.", false);
            socket.connect();
            setText("Connected!", true);
            return socket;

        } catch (IOException f) {
            // something went wrong whle trying to connect to the bluetooth device
            setText("Failed to connect! " + f.getMessage(), false);
            try {
                // try connecting in fallback mode
                setText("Trying fallback mode", false);
                socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                socket.connect();
                setText("Connected!", true);
                return socket;
            } catch (Exception e2) {
                // something went wrong while trying to connect to the device
                setText(e2.getMessage(), false);
                try {
                    // close the socket
                    setText("Closing socket.", false);
                    socket.close();
                    return null;
                } catch (Exception e3) {
                    setText("Failed to close socket! " + e3.getMessage(), false);
                    return null;
                }
            }
        }
    }

    void setText(String s, boolean isConnected) {
        // change the text of the statusTextView
        textChanger.setText(s);
        textChanger.setConnected(isConnected);
        // update the UI
        this.activity.runOnUiThread(textChanger);
    }
}



