package org.servalproject.sensors.Graphs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.api.NetworkAPI;
import org.servalproject.sensors.SensorData;
/**
 * Created by banson on 10/14/2016.
 */
public class GravityGraphActivity extends BasicGraphActivity implements
        OnChartValueSelectedListener,
        SensorEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NetworkAPI.MESH_SENSORS);
        this.registerReceiver(sensorReceiver, filter);
    }

    BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(NetworkAPI.CMD_SRC)) {
                // get sender of sensors
                String sender = intent.getStringExtra(NetworkAPI.CMD_SRC);
                String thisSID = "";
                try {
                    thisSID = ServalBatPhoneApplication.context.server.getIdentity().sid.toString();
                } catch (Exception E) {
                    E.printStackTrace();
                }
                if (sender.equals(thisSID)) {
                    return;
                }

                SensorData sensorData = new SensorData();
                sensorData.fromJSONString(new String(intent.getByteArrayExtra(NetworkAPI.CMD_DATA)));
                float timeDiff = ((int) System.currentTimeMillis()) / 100 - beginTime;
                timeDiff = timeDiff / 10;
                list.get(0).addSensorEntry(timeDiff, (float) sensorData.gravityData[0], "Gravity");
                list.get(1).addSensorEntry(timeDiff, (float) sensorData.gravityData[1], "Gravity");
                list.get(2).addSensorEntry(timeDiff, (float) sensorData.gravityData[2], "Gravity");
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionAdd: {
                for(LineChartItem l : list) {
                    l.mChart.setData(l.generateDataLine(15));
                    l.mChart.animateX(750);
                }
                break;
            }
            case R.id.actionClear: {
                run = false;
                for(LineChartItem l: list) {
                    l.mChart.clearValues();
                    Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
                }

                break;
            }
            case R.id.actionUseLocal: {
                for(LineChartItem l : list) {
                    catchGravity(l);
                }
                break;
            }
            case R.id.actionSaveGraph: {
                for(LineChartItem l : list) {
                    l.saveToGallery("GravityGraph_" + l.axis, 0);
                }
                break;
            }
        }
        return true;
    }


    private void catchGravity(LineChartItem l) {
        super.captureLocalSensors(l, "Gravity");
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(sensorReceiver);
    }

    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NetworkAPI.MESH_SENSORS);
        this.registerReceiver(sensorReceiver, filter);
    }

}