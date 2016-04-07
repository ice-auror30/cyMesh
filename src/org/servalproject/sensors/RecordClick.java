package org.servalproject.sensors;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.servalproject.sensors.WiFiDirect.WiFiDirectActivity;

import java.io.File;
import java.io.IOException;

/**
 * Created by Brad on 2/17/2016.
 */
public class RecordClick extends Service implements SurfaceHolder.Callback {
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    SurfaceView cameraView;

    public RecordClick() {

        recorder = new MediaRecorder();
        initRecorder();

        cameraView = WiFiDirectActivity.dummySurface;

        holder = cameraView.getHolder();
        holder.addCallback(this);
    }

    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
  //      recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        recorder.setOrientationHint(90);

       /* CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);*/


        recorder.setOutputFile(Environment.getExternalStorageDirectory() + File.separator
                + Environment.DIRECTORY_DCIM + File.separator + "remoteVideo" + System.currentTimeMillis()+".mp4");
        recorder.setMaxDuration(5000); // 5 seconds
    }

    private void prepareRecorder() {
        //Use to not view preview
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);
        Surface sv = new Surface(surfaceTexture);
        recorder.setPreviewDisplay(sv);

        //Use to view preview
        //recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClick() {
        if (recording) {
            recorder.stop();
            recorder.reset();
            recording = false;

            recorder.release();
            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            recorder.start();
        }
    }


    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (recording == false)
            recorder.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        recorder.stop();
        recording = false;

        super.onDestroy();
    }


}

