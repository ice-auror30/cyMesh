package org.servalproject.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.sensors.SensorCollector;
import org.servalproject.servaldna.SubscriberId;

/**
 * Created by banson on 10/14/2016.
 */
public class SensorService extends Service{
    public static final String TAG = SensorCollector.class.getName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SensorCollector sc = new SensorCollector(this);
        sc.registerSensors();
        final Service thisService = this;

        HandlerThread mHandlerThread = new HandlerThread("sensor-handler");
        mHandlerThread.start();
        final Handler handler = new Handler(mHandlerThread.getLooper());

        final Runnable starter = new Runnable() {
            @Override
            public void run() {
                if (sc.getSensorData() != null) {
                    ServalBatPhoneApplication.context.netAPI.sendSensors(
                            SubscriberId.broadcastSid,
                            sc.getSensorData().getBytes());
                }
                handler.postDelayed(this, 100);
            }
        };

        Runnable stopper = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Time is up. Unregistering Sensors");
                sc.unregisterSensors();
                handler.removeCallbacks(starter);
                thisService.stopSelf();
            }
        };
        handler.postDelayed(starter, 1000);
        handler.postDelayed(stopper, 31000);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}