package saleem.elec3607_pong;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

/**
 * Created by saleem on 24/05/17.
 */

public class SensorThread implements Runnable, SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private Activity activity;
    private TextView statusText;
    private float sensorValue;

    public SensorThread(Activity activity){
        this.activity = activity;
        statusText = (TextView) activity.findViewById(R.id.status_text);
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (sensor == null) {
            // device does not support sensor
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusText.setText("This device does not support accelerometer");
                }
            });
            return;
        }
    }

    @Override
    public void run(){
        registerSensor();
    }

    public void registerSensor(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this){
            this.sensorValue = event.values[0];
        }
    }

    public float readSensorValue(){
        float t;
        synchronized (this){
            t = sensorValue;
        }
        return t;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
