package saleem.elec3607_pong;

import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by saleem on 21/05/17.
 * This class is used to modify the status text as well as enabling/disabling the connect button
 */

public class TextChanger implements Runnable {
    private String text; // the text we're trying to change
    private TextView statusText; // the textView object that we're trying to check
    private MenuItem bluetoothItem; // the "connect" button
    private boolean isConnected; // true when we're connected to the slave device

    public TextChanger(TextView statusText, MenuItem bluetoothItem) {
        this.statusText = statusText;
        this.bluetoothItem = bluetoothItem;
        text = "";
    }

    public void setText(String s) {
        this.text = s;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public void run() {
        // set the text
        statusText.setText(text);
        // if we're connected to the slave, disable the "connect" button. otherwise, enable it
        bluetoothItem.setEnabled(!isConnected);
    }
}