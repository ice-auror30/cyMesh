package org.servalproject.sensors.Graphs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.api.NetworkAPI;
import org.servalproject.sensors.SensorData;

import java.util.ArrayList;

public class GravityGraphActivity extends Activity implements
        OnChartValueSelectedListener,
        SensorEventListener {

    ArrayList<LineChartItem> list;

    protected Typeface mTfLight;
    boolean run;
    float xValue;
    float yValue;
    float zValue;

    float beginTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gravity_graph);
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        list = new ArrayList<LineChartItem>();
        beginTime = ((int) System.currentTimeMillis()) / 100;

        Description dx = new Description();
        dx.setText("X Axis Gravity");
        LineChartItem x = new LineChartItem(getApplicationContext(), findViewById(R.id.chart1), dx, 0);
        x.mChart.setOnChartValueSelectedListener(this);
        list.add(x);

        Description dy = new Description();
        dy.setText("Y Axis Gravity");
        LineChartItem y = new LineChartItem(getApplicationContext(), findViewById(R.id.chart2), dy, 1);
        y.mChart.setOnChartValueSelectedListener(this);
        list.add(y);

        Description dz = new Description();
        dz.setText("Z Axis Gravity");
        LineChartItem z = new LineChartItem(getApplicationContext(), findViewById(R.id.chart3), dz, 2);
        z.mChart.setOnChartValueSelectedListener(this);
        list.add(z);

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
                list.get(0).addGravityEntry(timeDiff, (float) sensorData.gravityData[0]);
                list.get(1).addGravityEntry(timeDiff, (float) sensorData.gravityData[1]);
                list.get(2).addGravityEntry(timeDiff, (float) sensorData.gravityData[2]);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

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
            case R.id.actionFeedMultiple: {
                for(LineChartItem l : list) {
                    feedMultiple(l);
                }
                break;
            }
            case R.id.actionUseGravity: {
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


    private Thread thread;

    private void feedMultiple(LineChartItem l) {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                l.addRandomEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    private void catchGravity(LineChartItem l) {

        beginTime = ((int) System.currentTimeMillis()) / 100;
        run = true;
        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                float timeDiff = ((int) System.currentTimeMillis()) / 100 - beginTime;
                timeDiff = timeDiff / 10;
                switch(l.axis){
                    case LineChartItem.X_AXIS:
                        l.addGravityEntry(timeDiff, xValue);
                        break;
                    case LineChartItem.Y_AXIS:
                        l.addGravityEntry(timeDiff, yValue);
                        break;
                    case LineChartItem.Z_AXIS:
                        l.addGravityEntry(timeDiff, zValue);
                        break;
                }
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(run){

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }



    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xValue = event.values[0];
        yValue = event.values[1];
        zValue = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}