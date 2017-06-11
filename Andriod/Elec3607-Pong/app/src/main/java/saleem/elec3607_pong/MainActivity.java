package saleem.elec3607_pong;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
    static String DEVICE_NAME = "HC-06"; // name of the bluetooth device
    static int REQUEST_ENABLE_BT = 1;

    public Menu menu; // action bar menu
    private SQLiteDatabase db; // reference to the database
    private MatchHistoryAdapter adapter; // reference to the recycler view (to display the match histories)
    public RecyclerView recyclerView; // the view that displays all the match histories
    private BluetoothTransmission bluetoothTransmission;
    public SensorThread sensorThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // view has been created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the database object
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getWritableDatabase();

        // create the query
        // select p1_score, p2_score, date from matches order by date desc
        Cursor cursor = db.query("matches",
                new String[]{"p1_score", "p2_score", "date"},
                null, null, null, null, "date desc");

        // create the recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.match_history_recycler_view);
        adapter = new MatchHistoryAdapter();

        // for each row, add it to our recycler view
        while (cursor.moveToNext()) {
            adapter.add(new MatchHistory(cursor.getInt(0), cursor.getInt(1), cursor.getLong(2)));
        }
        cursor.close();

        // display it one by one
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // start the sensor thread
        sensorThread = new SensorThread(this);
        new Thread(sensorThread).start();

        // create the bluetooth transmission thread
        bluetoothTransmission = new BluetoothTransmission(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (bluetoothTransmission == null){
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            // volume-up button is pressed
            bluetoothTransmission.incrementDelay();
        }
        else if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            // volume-down button is pressed
            bluetoothTransmission.decrementDelay();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_connect_button: {
                // when the user clicks the connect button, start this thread
                Thread thread = new Thread(new BluetoothHandler(this, adapter, bluetoothTransmission));
                thread.start();
                return true;
            }
            case R.id.clear_match_history_button: {
                // when the delete all button has been cleared, start this async task
                new BluetoothAsyncDeleteTask().execute();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // create the actionbar menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private class BluetoothAsyncDeleteTask extends AsyncTask<Void, Void, String> {
        // this gets executed when the user tries to delete all match histories from the device
        private ProgressDialog dialog;

        protected void onPreExecute() {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // show the loading dialog
                    dialog = ProgressDialog.show(MainActivity.this, "",
                            "Deleting all match histories", true);
                }
            });
        }

        protected String doInBackground(Void... params) {
            try {
                // delete all rows from the database
                db.delete("matches", null, null);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        // clear all entries in the recycler view
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
                return null;
            } catch (Exception e) {
                // something went wrong, return the message
                return e.getMessage();
            }
        }

        protected void onPostExecute(final String str) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    TextView textView = (TextView) findViewById(R.id.status_text);
                    if (str == null) {
                        // successfully deleted all messages
                        textView.setText("Successfully deleted all match histories!");
                    } else {
                        // if something went wrong, display the error mesasge in the status view text
                        textView.setText(str);
                    }
                    // hide the loading dialog
                    dialog.dismiss();
                }
            });
        }
    }
}
