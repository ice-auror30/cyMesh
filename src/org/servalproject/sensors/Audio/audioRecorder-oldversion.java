package org.servalproject.sensors;

import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class audioRecorder
{

    private static final String LOG_TAG = "AudioRecordTest";
    private String mFileName = null;
    private MediaRecorder mRecorder = null;
    private Handler handler;
    public audioRecorder(){

    }

    public void setPath() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

    }


    public void Record(String fileName, int duration) {
        mFileName = fileName;
        setPath(); //Set the path of the recorder using the given fileName
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();

        Runnable runnable  = new Runnable() {
            @Override
            public void run() {
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();

                handler.postDelayed(this, 1500);

            }
        };
    }
}
















