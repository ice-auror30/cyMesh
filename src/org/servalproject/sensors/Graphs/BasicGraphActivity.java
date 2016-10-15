package org.servalproject.sensors.Graphs;

import android.app.Activity;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.servalproject.R;

import java.util.ArrayList;
/**
 * Created by banson on 10/14/2016.
 */
public class BasicGraphActivity extends Activity implements
        OnChartValueSelectedListener,
        SensorEventListener {

    ArrayList<LineChartItem> list;

    protected Typeface mTfLight;
    boolean run;
    float xValue;
    float yValue;
    float zValue;
    float beginTime;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.three_graph);
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        list = new ArrayList<LineChartItem>();
        beginTime = ((int) System.currentTimeMillis()) / 100;

        Description dx = new Description();
        dx.setText("X Axis");
        LineChartItem x = new LineChartItem(getApplicationContext(), findViewById(R.id.chart1), dx, 0);
        x.mChart.setOnChartValueSelectedListener(this);
        list.add(x);

        Description dy = new Description();
        dy.setText("Y Axis");
        LineChartItem y = new LineChartItem(getApplicationContext(), findViewById(R.id.chart2), dy, 1);
        y.mChart.setOnChartValueSelectedListener(this);
        list.add(y);

        Description dz = new Description();
        dz.setText("Z Axis");
        LineChartItem z = new LineChartItem(getApplicationContext(), findViewById(R.id.chart3), dz, 2);
        z.mChart.setOnChartValueSelectedListener(this);
        list.add(z);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

    public void captureLocalSensors(LineChartItem l, String sensorType) {

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
                        l.addSensorEntry(timeDiff, xValue, sensorType);
                        break;
                    case LineChartItem.Y_AXIS:
                        l.addSensorEntry(timeDiff, yValue, sensorType);
                        break;
                    case LineChartItem.Z_AXIS:
                        l.addSensorEntry(timeDiff, zValue, sensorType);
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
    public void onSensorChanged(SensorEvent event) {
        xValue = event.values[0];
        yValue = event.values[1];
        zValue = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }
}