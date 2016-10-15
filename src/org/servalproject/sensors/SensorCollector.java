package org.servalproject.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by ejfett4 on 2/14/2016.
 * Adapted for CyMesh by banson on 10/14/2016.
 */
public class SensorCollector implements SensorEventListener {
    public static final String SENSOR_BROADCAST = "org.servalproject.batphone.sensors_broadcast";
    public static final String SENSOR_DATA = "data";

    private static final String TAG = SensorCollector.class.getName();
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLight;
    private Sensor mGravity;
    private Sensor mGyro;

    private boolean mAccelerometerReady;
    private boolean mLightReady;
    private boolean mGravityReady;
    private boolean mGyroReady;
    private boolean registered;

    private SensorData data;

    public SensorCollector(Context givenContext)
    {
        data = new SensorData();
        mSensorManager= (SensorManager) givenContext.getSystemService(Activity.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public SensorCollector()
    {
    }

    public void registerSensors(){
        mAccelerometerReady = false;
        mLightReady = false;
        mGravityReady = false;
        mGyroReady = false;
        registered = true;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensors(){
        registered = false;
        mSensorManager.unregisterListener(this);
    }

    public String getSensorData() {
        if(registered && mAccelerometerReady && mLightReady && mGravityReady && mGyroReady) {
            return data.toJSONString();
        }
        else { return null; }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerReady = true;
                data.accelerometerData[0] = event.values[0];
                data.accelerometerData[1] = event.values[1];
                data.accelerometerData[2] = event.values[2];
                break;
            case Sensor.TYPE_LIGHT:
                mLightReady = true;
                data.lightData = event.values[0];
                break;
            case Sensor.TYPE_GRAVITY:
                mGravityReady = true;
                data.gravityData[0] = event.values[0];
                data.gravityData[1] = event.values[1];
                data.gravityData[2] = event.values[2];
                break;
            case Sensor.TYPE_GYROSCOPE:
                mGyroReady = true;
                data.gyroData[0] = event.values[0];
                data.gyroData[1] = event.values[1];
                data.gyroData[2] = event.values[2];
                break;
            default:
                Log.d(TAG, "Unexpected Sensor Type Found");
        }
    }
}