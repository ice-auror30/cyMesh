

/**
 * Created by Adit
 */
package org.servalproject.sensors;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import java.util.Timer;
import java.util.TimerTask;

public class audioRecorder extends Service {

    //Media recorder used to capture audio
    MediaRecorder mRecorder;

    //State of recorder
    boolean recording = false;
    boolean recorded = false;

    //File Names
    static String mFileName;

    //Set singleton Instance to null
    private static audioRecorder mInstance = null;

    private audioRecorder() {

        //Intialize file path.
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audioTestFile.3gp";

        prepareAudioRecorder();

    }

    /**
     * Singleton Format
     * @return This object
     */
    public static synchronized audioRecorder getInstance() {
        if (mInstance == null) {
            mInstance = new audioRecorder();
        }
        return mInstance;
    }

    /**
     * This method prepares the media Recorder and sets the appropriate values.
     */
    private void prepareAudioRecorder() {

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method captures Audio for the given duration in seconds
     * @param Duration in Secs
     */
    public void captureAudio(int Duration) {

        //Task that will stop recording after the given duration
        TimerTask stopRecording = new TimerTask() {
            @Override
            public void run() {
                if (recording) {
                    mRecorder.stop();
                    mRecorder.release();

                    //Reset the state of the recorder.
                    recording = false;
                    recorded = true;
                }
            }
        };

        //Need to reinstantiate the recorder after previous recording has completed.
        if (recorded) {
            prepareAudioRecorder();
            recorded = false;
        }

        //Start recording
        if (!recording) {
            recording = true;
            mRecorder.start();
            Timer myTimer = new Timer();

        //Set a timer to stop recording after given duration
            myTimer.schedule(stopRecording, Duration*1000);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (recording == false)
            mRecorder.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        recording = false;
        super.onDestroy();
    }

}
