package org.servalproject.sensors;

import android.hardware.Sensor;

/**
 * Created by ejfett4 on 2/14/2016.
 */
public class SensorCollector {
    private static SensorCollector self;

    private SensorCollector()
    {

    }

    public synchronized  static SensorCollector getInstance()
    {
        if(self != null)
            return self;
        else
        {
            self = new SensorCollector();
            return self;
        }
    }
}
