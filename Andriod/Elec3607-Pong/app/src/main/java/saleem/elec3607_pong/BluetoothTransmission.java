package saleem.elec3607_pong;

import android.util.Log;
import android.widget.TextView;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by saleem on 24/05/17.
 */

public class BluetoothTransmission implements Runnable {
    private PrintStream bluetoothWriter;
    private long milliseconds = 500;
    private boolean isRunning;
    private MainActivity activity;
    private TextView statusText;

    public BluetoothTransmission(MainActivity activity) {
        this.activity = activity;
        statusText = (TextView) activity.findViewById(R.id.status_text);
        isRunning = true;
    }

    public void run() {
        while (isRunning) {
            // send to bluetooth code here
            sendData();
            sleep();
        }
        try {
            bluetoothWriter.close();
        }
        catch (Exception e){
            displayText(e.getMessage());
        }
    }
    private void sendData(){
        if (bluetoothWriter == null){
            return;
        }
        int data = -1 * Math.round(activity.sensorThread.readSensorValue());
        String s = "{" + data  + "}";
        try {
            bluetoothWriter.println(s);
        }
        catch (Exception e){
            displayText(e.getMessage());
            isRunning = false;
        }
    }
    public void attach(OutputStream outputStream){
        try {
            bluetoothWriter = new PrintStream(outputStream);
        }
        catch (Exception e){
            bluetoothWriter = null;
            displayText(e.getMessage());
        }
    }
    public void stop() {
        isRunning = false;
    }

    public void incrementDelay() {
        // increase the delay
        milliseconds += 10;
        displayNewDelay();
    }

    public void decrementDelay() {
        // decrease the delay
        if (milliseconds > 10) {
            milliseconds -= 10;
        }
        displayNewDelay();
    }

    public void displayText(final String string){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(string);
            }
        });
    }
    private void displayNewDelay() {
        displayText("Bluetooth transmission rate changed to " + milliseconds + "ms");
    }

    public void sleep(long ms) {
        // sleeps this thread for "ms" milliseconds
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            return;
        }
    }

    public void sleep(){
        sleep(this.milliseconds);
    }


}
