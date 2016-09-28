package org.servalproject.sensors;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.servalproject.Main;
import org.servalproject.ServalBatPhoneApplication;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Brad on 2/17/2016.
 */
public class RecordClick extends Service implements SurfaceHolder.Callback {
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    boolean recorded = false;
    SurfaceView cameraView;

    private String sid;
    private final ServalBatPhoneApplication app;
    public static final String RECORDING_FINISHED="org.servalproject.recordclick.FINISHED";

    public RecordClick(ServalBatPhoneApplication app) {
        this.app = app;
        Log.d("RecordClickObject", "Created");
        initRecorder();
        cameraView = Main.getCameraSurface();
        holder = cameraView.getHolder();
        holder.addCallback(this);
    }

    private void initRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        recorder.setVideoEncodingBitRate(3000000);
        recorder.setOrientationHint(90);

       /* CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);*/
        Log.d("Camera", "Initialized");
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

    public void onClick(String sid) {
        Log.d("OnClick","Pressed");
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                if (recording) {
                    Log.d("Camera", "Stopping");
                    recorder.stop();
                    recorder.reset();
                    recording = false;

                    //Release recorder resources - we'll make a new MediaRecorder if we need to record again
                    recorder.release();

                    Log.d("Camera","Stopped");
                    recorded = true;

                    app.sendBroadcast(new Intent(RECORDING_FINISHED));
                }
            }
        };
        if(recorded){
            initRecorder();
            prepareRecorder();
            recorded = false;
        }
        if(!recording) {
            initRecorder();
            Log.d("Camera", Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + sid + ".mp4");
            recorder.setOutputFile(Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_DCIM + File.separator + sid + ".mp4");
            recording = true;
            prepareRecorder();

            recorder.start();
            Timer myTimer = new Timer();

            myTimer.schedule(t, 5000);

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //cameraView = Main.getCameraSurface();
        //holder = cameraView.getHolder();
        //holder.addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("Camera","Surface changed");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        recorder = new MediaRecorder();
        initRecorder();
        cameraView = Main.getCameraSurface();
        holder = cameraView.getHolder();
        holder.addCallback(this);
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
    public void onPause(){
        //cameraView = null;
    }

    @Override
    public void onDestroy() {
        recorder.stop();
        recording = false;

        super.onDestroy();
    }


}

