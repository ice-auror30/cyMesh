package org.servalproject.sensors;

import java.util.Queue;

/**
 * Created by ejfett4 on 2/14/2016.
 */
public class SensorCollector {
    private static SensorCollector self;
    String returnAudioPath;
    private Queue queue;
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



    public void getAudio(String FileName, int Duration)
    {

        audioRecorder audio = new audioRecorder();
        audio.Record(FileName, Duration);
    }


}
